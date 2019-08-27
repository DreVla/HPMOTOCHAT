package com.hpmtutorial.hpmotochat.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private final int PICK_IMAGE_REQUEST = 71;
    public static final String TAG = "ChatView";
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private RecyclerView chatRecyclerView;
    private String chatId, senderId, receiverId, senderEmail, receiverEmail;
    private ChatAdapter chatAdapter;
    private List<Chat> chatList = new ArrayList<>();
    private EditText editText;
    private Uri filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        editText = findViewById(R.id.edit_text_message);
        senderEmail = getIntent().getStringExtra("sender");
        receiverEmail = getIntent().getStringExtra("receiver");
        senderId = getIntent().getStringExtra("current_user_uid");
        receiverId = getIntent().getStringExtra("receiver_user_uid");
        if (senderId.compareTo(receiverId) > 0) {
            chatId = senderId + receiverId;
        } else {
            chatId = receiverId + senderId;
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
        if(chatRecyclerView.getAdapter().getItemCount() >1)
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
        mDatabase.child("Chats").orderByKey().equalTo(chatId).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = new Chat();
                    for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                        chat = chatSnapshot.getValue(Chat.class);
                        Log.d(TAG, "onDataChange: " + chat.getMessage());
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
        });
    }

    public void loadChat() {
        mDatabase.child("Chats").orderByKey().equalTo(chatId).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                        Chat chat = chatSnapshot.getValue(Chat.class);
                        Log.d(TAG, "onDataChange: " + chat.getMessage());
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
        });

    }

    String messageKey;
    String imgId;

    public void sendMessage(View view) {
        final DatabaseReference chatsReff = mDatabase.child("Chats");
        final Chat senderChat = new Chat();
        senderChat.setMessage(String.valueOf(editText.getText()));
        senderChat.setReceiverUid(receiverId);
        senderChat.setSenderUid(senderId);
        senderChat.setTimeStamp(System.currentTimeMillis() / 1000);
        senderChat.setSender(senderEmail);
        senderChat.setReceiver(receiverEmail);

        if(filePath==null){
            messageKey = chatsReff.push().getKey();
        } else {
            senderChat.setImage(imgId);
            filePath=null;
        }

        //push to firebase


        chatsReff.child(chatId).child(messageKey).setValue(senderChat);

        editText.setText("");
    }

    public void addFile(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            final DatabaseReference chatsReff = mDatabase.child("Chats");
            filePath = data.getData();
            imgId = chatId + messageKey;

            messageKey = chatsReff.push().getKey();

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            StorageReference ref = storageReference.child("images/"+ imgId);
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
                            Toast.makeText(ChatActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }
}
