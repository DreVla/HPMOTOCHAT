package com.hpmtutorial.hpmotochat.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

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
import com.hpmtutorial.hpmotochat.model.UsersAdapter;
import com.hpmtutorial.hpmotochat.model.User;

import java.util.ArrayList;
import java.util.List;

public class AllUsersActivity extends AppCompatActivity {

    private RecyclerView usersRecyclerView;
    private FirebaseAuth mAuth;
    private static final String TAG = "UserList";
    private DatabaseReference mDatabase;
    private List<User> users = new ArrayList<>();
    private UsersAdapter adapter;
    private String receiverEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);
        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        usersRecyclerView = findViewById(R.id.users_recycler_view);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UsersAdapter(this, users, new UsersAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                receiverEmail = adapter.getItem(position).getEmail();
                mDatabase.child("Users")
                        .orderByChild("email")
                        .equalTo(receiverEmail)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                                    String receiverUID = childSnapshot.getKey();
                                    Intent chatIntent = new Intent(getApplicationContext(), ChatActivity.class);
                                    chatIntent.putExtra("current_user_uid", mAuth.getCurrentUser().getUid());
                                    chatIntent.putExtra("receiver_user_uid", receiverUID);
                                    chatIntent.putExtra("sender", mAuth.getCurrentUser().getEmail());
                                    chatIntent.putExtra("receiver", receiverEmail);
                                    startActivity(chatIntent);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.d(TAG, "onCancelled: error");
                            }
                        });

            }
        });
        usersRecyclerView.setAdapter(adapter);

        final DatabaseReference usersdRef = mDatabase.child("Users");
        final ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    Log.d("UserName", user.getEmail());
                    users.add(user);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        usersdRef.addListenerForSingleValueEvent(eventListener);

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.books_log_out:
                logOut();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
