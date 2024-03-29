package com.hpmtutorial.hpmotochat.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class ChatActivity extends AppCompatActivity {
    private final int PICK_FILE = 123;
    public static final String TAG = "ChatView";
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private RecyclerView chatRecyclerView;
    private String chatId, currentUser, receiverId, senderEmail, receiverEmail;
    private ChatAdapter chatAdapter;
    private List<Chat> chatList = new ArrayList<>();
    private EditText editText;
    private Uri filePath;
    ValueEventListener changeListener, chatListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        editText = findViewById(R.id.edit_text_message);
        senderEmail = getIntent().getStringExtra("sender");
        receiverEmail = getIntent().getStringExtra("receiver");
        currentUser = getIntent().getStringExtra("current_user_uid");
        receiverId = getIntent().getStringExtra("receiver_user_uid");
        if (currentUser.compareTo(receiverId) > 0) {
            chatId = currentUser + receiverId;
        } else {
            chatId = receiverId + currentUser;
        }
        Log.d(TAG, "onCreate: " + receiverId);

        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        chatRecyclerView = findViewById(R.id.recycler_view_chat);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(getApplicationContext(), chatList);
        chatRecyclerView.setAdapter(chatAdapter);
        chatAdapter.setEmailCurrentUser(senderEmail);
        loadChat();
        listenToChanges();
        if (chatRecyclerView.getAdapter().getItemCount() > 1)
            chatRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v,
                                           int left, int top, int right, int bottom,
                                           int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (bottom < oldBottom) {
                        chatRecyclerView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                chatRecyclerView.smoothScrollToPosition(
                                        chatRecyclerView.getAdapter().getItemCount() - 1);
                            }
                        }, 100);
                    }
                }
            });
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    private void listenToChanges() {
        changeListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = new Chat();
                    String chatKey = null;
                    for (final DataSnapshot chatSnapshot : snapshot.getChildren()) {
                        chatKey = chatSnapshot.getKey();
                        chat = chatSnapshot.getValue(Chat.class);
                    }
                    if (mAuth.getCurrentUser().getUid().equals(chat.getReceiverUid())) {
//                            Log.d(TAG, "onDataChange: match" + chatSnapshot.getValue(Chat.class).getSender() + chatSnapshot.getValue(Chat.class).getMessage());
                        mDatabase.child("Chats").child(chatId).child(chatKey).child("seen").setValue(true);
                    }
                    if(chatList.get(chatList.size()-1).getTimeStamp()!=chat.getTimeStamp()) chatList.add(chat);
                    else chatList.get(chatList.size()-1).setSeen(true);

                    chatAdapter.notifyDataSetChanged();
                    if (chatList.size() > 1)
                        chatRecyclerView.smoothScrollToPosition(chatList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mDatabase.child("Chats").orderByKey().equalTo(chatId).addValueEventListener(changeListener);
    }

    public void loadChat() {
        chatListener = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (final DataSnapshot chatSnapshot : snapshot.getChildren()) {
                        if (mAuth.getCurrentUser().getUid().equals(chatSnapshot.getValue(Chat.class).getReceiverUid())) {
//                            Log.d(TAG, "onDataChange: match" + chatSnapshot.getValue(Chat.class).getSender() + chatSnapshot.getValue(Chat.class).getMessage());
                            mDatabase.child("Chats").child(chatId).child(chatSnapshot.getKey()).child("seen").setValue(true);
                        }
                        Chat chat = chatSnapshot.getValue(Chat.class);
//                        if(chatList.get(chatList.size()-1).getTimeStamp()!=chat.getTimeStamp())
                            chatList.add(chat);
//                        else chatList.get(chatList.size()-1).setSeen(true);

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
        mDatabase.child("Chats").orderByKey().equalTo(chatId).addListenerForSingleValueEvent(chatListener);

    }

    String messageKey;
    String fileId;

    public void sendMessage(View view) {
        final DatabaseReference chatsReff = mDatabase.child("Chats");
        final Chat senderChat = new Chat();
        senderChat.setMessage(String.valueOf(editText.getText()));
        senderChat.setReceiverUid(receiverId);
        senderChat.setSenderUid(currentUser);
        senderChat.setTimeStamp(System.currentTimeMillis() / 1000);
        senderChat.setSender(senderEmail);
        senderChat.setReceiver(receiverEmail);
        senderChat.setSeen(false);
        if (filePath == null) {
            messageKey = chatsReff.push().getKey();
        } else {
            senderChat.setImage(fileId);
            filePath = null;
        }

        //push to firebase

        chatsReff.child(chatId).child(messageKey).setValue(senderChat);
        chatAdapter.notifyDataSetChanged();
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
            final DatabaseReference chatsReff = mDatabase.child("Chats");
            filePath = data.getData();
            fileId = chatId + messageKey;

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
                            Toast.makeText(ChatActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(ChatActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
//        mDatabase.child("Chats").orderByKey().equalTo(chatId).removeEventListener(changeListener);
//        mDatabase.child("Chats").orderByKey().equalTo(chatId).removeEventListener(chatListener);
//    }

}
