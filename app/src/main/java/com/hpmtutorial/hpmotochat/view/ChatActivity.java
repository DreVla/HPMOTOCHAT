package com.hpmtutorial.hpmotochat.view;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hpmtutorial.hpmotochat.R;
import com.hpmtutorial.hpmotochat.model.Chat;
import com.hpmtutorial.hpmotochat.model.ChatAdapter;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    public static final String TAG = "ChatView";
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private RecyclerView chatRecyclerView;
    private String chatId, senderId, receiverId, senderEmail, receiverEmail;
    private ChatAdapter chatAdapter;
    private List<Chat> chatList = new ArrayList<>();
    private EditText editText;


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
        chatAdapter = new ChatAdapter(this, chatList);
        chatRecyclerView.setAdapter(chatAdapter);
        chatAdapter.setEmailCurrentUser(senderEmail);
        loadChat();
        listenToChanges();
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


    public void sendMessage(View view) {
        final DatabaseReference chatsReff = mDatabase.child("Chats");
        Chat senderChat = new Chat();
        senderChat.setMessage(String.valueOf(editText.getText()));
        senderChat.setReceiverUid(receiverId);
        senderChat.setSenderUid(senderId);
        senderChat.setTimeStamp(System.currentTimeMillis() / 1000);
        senderChat.setSender(senderEmail);
        senderChat.setReceiver(receiverEmail);


        Chat receiverChat = new Chat();
        receiverChat.setMessage(String.valueOf(editText.getText()));
        receiverChat.setSenderUid(receiverId);
        receiverChat.setReceiverUid(senderId);
        receiverChat.setTimeStamp(System.currentTimeMillis() / 1000);
        receiverChat.setReceiver(senderEmail);
        receiverChat.setSender(receiverEmail);

        //push to firebase
        String messageKey = chatsReff.push().getKey();

        chatsReff.child(chatId).child(messageKey).setValue(senderChat);

        editText.setText("");
    }
}
