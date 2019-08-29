package com.hpmtutorial.hpmotochat.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hpmtutorial.hpmotochat.R;
import com.hpmtutorial.hpmotochat.model.User;

import java.util.List;

public class CreateUsersAdapter extends RecyclerView.Adapter<CreateUsersAdapter.ViewHolder> {

    private List<User> mData;
    private LayoutInflater mInflater;
    private UsersAdapter.ItemClickListener mClickListener;

    public interface OnItemCheckListener {
        void onItemCheck(User item);
        void onItemUncheck(User item);
    }


    @NonNull
    private OnItemCheckListener onItemCheckListener;

    public CreateUsersAdapter(Context context, List<User> data, @NonNull OnItemCheckListener onItemCheckListener) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.onItemCheckListener = onItemCheckListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.create_group_user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        String email = mData.get(position).getEmail();
        holder.myTextView.setText(email);
        if (holder instanceof ViewHolder) {
            final User currentItem = mData.get(position);
            holder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.checkBox.setChecked(
                            !holder.checkBox.isChecked());
                    if (holder.checkBox.isChecked()) {
                        onItemCheckListener.onItemCheck(currentItem);
                    } else {
                        onItemCheckListener.onItemUncheck(currentItem);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if(mData==null) {
            return 0;
        } else return mData.size();
    }

    public User getItem(int position) {
        return mData.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView myTextView;
        CheckBox checkBox;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.create_group_user_item_name);
            checkBox = itemView.findViewById(R.id.create_group_user_item_checkBox);
            checkBox.setClickable(false);
        }

        public void setOnClickListener(View.OnClickListener onClickListener) {
            itemView.setOnClickListener(onClickListener);
        }
    }
}
