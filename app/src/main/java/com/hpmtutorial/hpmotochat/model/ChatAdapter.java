package com.hpmtutorial.hpmotochat.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hpmtutorial.hpmotochat.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Chat> mChats;
    private LayoutInflater mInflater;
    private String emailCurrentUser;
    public ChatAdapter(Context context, List<Chat> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mChats = data;
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

        ViewHolder(View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.chat_message);
            usernameTextView = itemView.findViewById(R.id.chat_username);
        }

        public void bind(Chat chat){
            message.setText(chat.getMessage());
            usernameTextView.setText(chat.getSender());
        }
    }


}