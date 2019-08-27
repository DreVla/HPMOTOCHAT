package com.hpmtutorial.hpmotochat.view;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hpmtutorial.hpmotochat.R;
import com.hpmtutorial.hpmotochat.model.Chat;
import com.hpmtutorial.hpmotochat.model.GlideApp;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Chat> mChats;
    private LayoutInflater mInflater;
    private String emailCurrentUser;
    private Context context;

    public ChatAdapter(Context context, List<Chat> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mChats = data;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        view = mInflater.inflate(viewType, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Chat chat = mChats.get(position);
        ((ViewHolder) holder).bind(chat);
    }

    @Override
    public int getItemViewType(int position) {
        if (emailCurrentUser.equals(mChats.get(position).getSender())) {
            return R.layout.item_chat_mine;
        } else {
            return R.layout.item_chat_other;
        }
    }

    @Override
    public int getItemCount() {
        if (mChats == null) {
            return 0;
        } else return mChats.size();
    }

    public Chat getItem(int position) {
        return mChats.get(position);
    }

    public void setEmailCurrentUser(String emailCurrentUser) {
        this.emailCurrentUser = emailCurrentUser;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView message;
        TextView usernameTextView;
        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.chat_message);
            usernameTextView = itemView.findViewById(R.id.chat_username);
            imageView = itemView.findViewById(R.id.chat_image);
        }

        public void bind(Chat chat) {
            message.setText(chat.getMessage());
            usernameTextView.setText(chat.getSender());
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            Log.d("Adapter", "bind: " + chat.getImage());
            GlideApp.with(context)
                    .load(storageReference.child("images/" + chat.getImage()))
                    .into(imageView);
        }
    }


}