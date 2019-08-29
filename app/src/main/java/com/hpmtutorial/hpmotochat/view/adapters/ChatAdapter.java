package com.hpmtutorial.hpmotochat.view.adapters;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hpmtutorial.hpmotochat.R;
import com.hpmtutorial.hpmotochat.model.Chat;
import com.hpmtutorial.hpmotochat.utils.GlideApp;

import java.util.List;

import static android.content.Context.DOWNLOAD_SERVICE;
import static android.content.Context.POWER_SERVICE;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Chat> mChats;
    private LayoutInflater mInflater;
    private String emailCurrentUser;
    private Context context;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

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
        Button downloadButton;
        ImageView seenIcon;

        ViewHolder(View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.chat_message);
            usernameTextView = itemView.findViewById(R.id.chat_username);
            imageView = itemView.findViewById(R.id.chat_image);
            downloadButton = itemView.findViewById(R.id.download_attachments_button);
            seenIcon = itemView.findViewById(R.id.seen_icon);
        }

        public void bind(final Chat chat) {
            message.setText(chat.getMessage());
            usernameTextView.setText(chat.getSender());
            if(chat.isSeen()) seenIcon.setImageResource(R.drawable.ic_done_all_black_24dp);
            else seenIcon.setImageResource(R.drawable.ic_done_black_24dp);
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            Log.d("Adapter", "bind: " + chat.getImage());
            if(chat.getImage()!=null) {
                downloadButton.setVisibility(View.VISIBLE);
                GlideApp.with(context)
                        .load(storageReference.child("files/" + chat.getImage()))
                        .into(imageView);
                downloadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        storageRef.child("files/" + chat.getImage()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Uri downloadUri = Uri.parse(uri.toString());
                                DownloadManager.Request request=new DownloadManager.Request(downloadUri)
                                        .setTitle(chat.getImage())// Title of the Download Notification
                                        .setDescription("Downloading file...")// Description of the Download Notification
                                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)// Visibility of the download Notification
                                        .setAllowedOverMetered(true)// Set if download is allowed on Mobile network
                                        .setAllowedOverRoaming(true);// Set if download is allowed on roaming network
                                DownloadManager downloadManager= (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
                                downloadManager.enqueue(request);// enqueue puts the download request in the queue.
                                Toast.makeText(context, "Succes download", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                                Toast.makeText(context, "Failed Download", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            } else {
                imageView.setImageResource(0);
                downloadButton.setVisibility(View.INVISIBLE);
            }
        }
    }


}