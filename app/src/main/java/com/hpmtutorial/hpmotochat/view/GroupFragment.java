package com.hpmtutorial.hpmotochat.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
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
import com.hpmtutorial.hpmotochat.view.adapters.GroupsAdapter;
import com.hpmtutorial.hpmotochat.view.adapters.UsersAdapter;

import java.util.ArrayList;
import java.util.List;

public class GroupFragment extends Fragment {

    private RecyclerView groupsRecyclerView;
    private FirebaseAuth mAuth;
    private static final String TAG = "GroupsList";
    private DatabaseReference databaseReference;
    private List<Group> groups = new ArrayList<>();
    private GroupsAdapter groupsAdapter;
    private String groupId;
    private FloatingActionButton createGroup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_group, container, false);
        mAuth = FirebaseAuth.getInstance();

        createGroup = root.findViewById(R.id.groups_create_new_group);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        groupsRecyclerView = root.findViewById(R.id.groups_recycler_view);
        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        groupsAdapter = new GroupsAdapter(getContext(), groups, new GroupsAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String groupId = groupsAdapter.getItem(position).getId();
                Intent groupChatIntent = new Intent(getContext(),GroupchatActivity.class);
                groupChatIntent.putExtra("group_id", groupId);
                groupChatIntent.putExtra("sender_email", mAuth.getCurrentUser().getEmail());
                startActivity(groupChatIntent);
            }
        });
        groupsRecyclerView.setAdapter(groupsAdapter);

        final DatabaseReference usersdRef = databaseReference.child("Groups");
        final ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Group group = ds.getValue(Group.class);
                    for(User u : group.getUsers()){
                        if(u.getUid().equals(mAuth.getUid())){
                            Log.d("GroupTitle", group.getTitle());
                            groups.add(group);
                        }
                    }
                    groupsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        usersdRef.addListenerForSingleValueEvent(eventListener);

        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewGroup();
            }
        });

        return root;
    }

    public void createNewGroup(){
        Intent createNewGroupActivity = new Intent(getContext(), CreateGroupActivity.class);
        startActivityForResult(createNewGroupActivity,1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==-1) {
            
            groupsAdapter.notifyDataSetChanged();
            groupsRecyclerView.setAdapter(groupsAdapter);
            Toast.makeText(getContext(), "Group Created", Toast.LENGTH_SHORT).show();
        } else Toast.makeText(getContext(), "No group added", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        groupsAdapter.notifyDataSetChanged();
        groupsRecyclerView.setAdapter(groupsAdapter);
    }
}
