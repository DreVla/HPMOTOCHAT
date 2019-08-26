package com.hpmtutorial.hpmotochat.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.hpmtutorial.hpmotochat.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Chat> mChats;
    private LayoutInflater mInflater;
    private String emailCurrentUser;
    private static int TYPE_SENDER = 1, TYPE_RECEIVER=2;

    public ChatAdapter(Context context, List<Chat> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mChats = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_RECEIVER) {
            view = mInflater.inflate(R.layout.item_chat_other, parent, false);
            return new ReceiverViewHolder(view);
        } else { // for email layout
            view = mInflater.inflate(R.layout.item_chat_mine, parent, false);
            return new SenderViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        String message = mChats.get(position).getMessage();
        String username = mChats.get(position).getSender();
        if (getItemViewType(position) == TYPE_RECEIVER) {
            ((ReceiverViewHolder) holder).message.setText(message);
            ((ReceiverViewHolder) holder).usernameTextView.setText(username);
        } else {
            ((SenderViewHolder) holder).message.setText(message);
            ((SenderViewHolder) holder).usernameTextView.setText(username);
        }
    }


    @Override
    public int getItemViewType(int position) {
        if (emailCurrentUser.equals(mChats.get(position).getSender())) {
            return TYPE_SENDER;
        } else {
            return TYPE_RECEIVER;
        }
    }

    @Override
    public int getItemCount() {
        if(mChats==null) {
            return 0;
        } else return mChats.size();
    }

    public Chat getItem(int position) {
        return mChats.get(position);
    }

    public void setEmailCurrentUser(String emailCurrentUser) {
        this.emailCurrentUser = emailCurrentUser;
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {
        TextView message;
        TextView usernameTextView;

        ReceiverViewHolder(View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.chat_other_message);
            usernameTextView = itemView.findViewById(R.id.chat_other_username);
        }
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView message;
        TextView usernameTextView;

        SenderViewHolder(View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.chat_mine_message);
            usernameTextView = itemView.findViewById(R.id.chat_mine_username);
        }
    }

}