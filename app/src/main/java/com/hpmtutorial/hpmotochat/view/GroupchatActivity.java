package com.hpmtutorial.hpmotochat.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hpmtutorial.hpmotochat.R;
import com.hpmtutorial.hpmotochat.model.Chat;
import com.hpmtutorial.hpmotochat.view.adapters.ChatAdapter;

import java.util.ArrayList;
import java.util.List;

public class GroupchatActivity extends AppCompatActivity {

    private final int PICK_FILE = 123;
    public static final String TAG = "ChatView";
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private RecyclerView chatRecyclerView;
    private String groupChatId, currentUser, receiverId, senderEmail, receiverEmail;
    private ChatAdapter chatAdapter;
    private List<Chat> chatList = new ArrayList<>();
    private EditText editText;
    private Uri filePath;
    ValueEventListener changeListener, chatListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupchat);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        editText = findViewById(R.id.group_message_edit_text);
        groupChatId = getIntent().getStringExtra("group_id");
        chatRecyclerView = findViewById(R.id.group_recycler_view_chat);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(getApplicationContext(), chatList);
        senderEmail = mAuth.getCurrentUser().getEmail();
        chatAdapter.setEmailCurrentUser(mAuth.getCurrentUser().getEmail());
        chatRecyclerView.setAdapter(chatAdapter);
        loadChat();
        listenToChanges();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    private void listenToChanges() {
        changeListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = new Chat();
                    for (final DataSnapshot chatSnapshot : snapshot.getChildren()) {
                        chat = chatSnapshot.getValue(Chat.class);
                    }
                    chatList.add(chat);
                    chatAdapter.notifyDataSetChanged();
                    if (chatList.size() > 1)
                        chatRecyclerView.smoothScrollToPosition(chatList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mDatabase.child("GroupChats").orderByKey().equalTo(groupChatId).addValueEventListener(changeListener);
    }

    private void loadChat() {
        chatListener = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (final DataSnapshot chatSnapshot : snapshot.getChildren()) {
                        Chat chat = chatSnapshot.getValue(Chat.class);
                        chatList.add(chat);
                    }
                    chatList.remove(chatList.size() - 1);
                    chatAdapter.notifyDataSetChanged();
                    if (chatList.size() > 1)
                        chatRecyclerView.smoothScrollToPosition(chatList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mDatabase.child("GroupChats").orderByKey().equalTo(groupChatId).addListenerForSingleValueEvent(chatListener);
    }

    String messageKey, fileId;
    public void sendMessage(View view) {
        final DatabaseReference chatsReff = mDatabase.child("GroupChats");
        final Chat senderChat = new Chat();
        senderChat.setMessage(String.valueOf(editText.getText()));
        senderChat.setSenderUid(currentUser);
        senderChat.setTimeStamp(System.currentTimeMillis() / 1000);
        senderChat.setSender(senderEmail);
        senderChat.setSeen(false);
        if (filePath == null) {
            messageKey = chatsReff.push().getKey();
        } else {
            senderChat.setImage(fileId);
            filePath = null;
        }

        //push to firebase

        chatsReff.child(groupChatId).child(messageKey).setValue(senderChat);

        editText.setText("");
    }

    public void addFile(View view) {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select file"), PICK_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            final DatabaseReference chatsReff = mDatabase.child("GroupChats");
            filePath = data.getData();
            fileId = groupChatId + messageKey;

            messageKey = chatsReff.push().getKey();

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            StorageReference ref = storageReference.child("files/" + fileId);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(GroupchatActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(GroupchatActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        mDatabase.child("GroupChats").orderByKey().equalTo(groupChatId).removeEventListener(changeListener);
//        mDatabase.child("GroupChats").orderByKey().equalTo(groupChatId).removeEventListener(chatListener);
//    }

}
