package com.hpmtutorial.hpmotochat.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hpmtutorial.hpmotochat.R;
import com.hpmtutorial.hpmotochat.model.Group;
import com.hpmtutorial.hpmotochat.model.User;
import com.hpmtutorial.hpmotochat.view.adapters.CreateUsersAdapter;
import com.hpmtutorial.hpmotochat.view.adapters.UsersAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CreateGroupActivity extends AppCompatActivity {

    private EditText titleEditText;
    private RecyclerView usersRecyclerView;
    private FloatingActionButton doneFab;
    private CreateUsersAdapter adapter;
    private List<User> users = new ArrayList<>();
    private List<User> usersToAdd = new ArrayList<>();
    private FirebaseAuth mAuth;
    private static final String TAG = "CreateGroup";
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        titleEditText = findViewById(R.id.editText_create_group);
        usersRecyclerView = findViewById(R.id.rv_create_group_users);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adapter = new CreateUsersAdapter(getApplicationContext(), users, new CreateUsersAdapter.OnItemCheckListener() {
            @Override
            public void onItemCheck(User user) {
                usersToAdd.add(user);
            }

            @Override
            public void onItemUncheck(User user) {
                usersToAdd.remove(user);
            }
        });
        usersRecyclerView.setAdapter(adapter);

        final DatabaseReference usersdRef = mDatabase.child("Users");
        final ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    user.setUid(ds.getKey());
                    Log.d("UserName", user.getEmail());
                    if(!mAuth.getUid().equals(ds.getKey()))
                        users.add(user);
                    else usersToAdd.add(user);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        usersdRef.addListenerForSingleValueEvent(eventListener);

        doneFab = findViewById(R.id.execute_create_new_group);

        doneFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(titleEditText.getText().toString().equals("")){
                    Toast.makeText(CreateGroupActivity.this, "Fill Title!", Toast.LENGTH_SHORT).show();
                } else if(usersToAdd.size()<3) {
                    Toast.makeText(CreateGroupActivity.this, "Add at least 2 members!", Toast.LENGTH_SHORT).show();
                } else {
                    Group group = new Group();
                    group.setUsers(usersToAdd);
                    group.setTitle(titleEditText.getText().toString());
                    DatabaseReference groupsRef = mDatabase.child("Groups");
                    String groupKey = groupsRef.push().getKey();
                    group.setId(groupKey);
                    groupsRef.child(groupKey).setValue(group);

                    setResult(RESULT_OK);
                    finish();
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }
}
