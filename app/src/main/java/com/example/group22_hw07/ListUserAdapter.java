package com.example.group22_hw07;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class ListUserAdapter extends RecyclerView.Adapter<ListUserAdapter.ViewHolder> {

    List<User> mList;
    private final OnItemClickListener listener;

    public ListUserAdapter(List<User> mList, OnItemClickListener listener) {
        this.mList = mList;
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemCheck(User item);
        void onItemUncheck(User item);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_user_recycler_view, parent, false);
        ViewHolder viewHolder = new ViewHolder(convertView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.bind(mList.get(position), listener);
        final User ulist = mList.get(position);

        holder.tv_username.setText(ulist.first_name + " " + ulist.last_name);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.checkBox.setChecked(!holder.checkBox.isChecked());
                if(holder.checkBox.isChecked()){
                    listener.onItemCheck(ulist);
                } else {
                    listener.onItemUncheck(ulist);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_username;
        CheckBox checkBox;
        View itemView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            tv_username = itemView.findViewById(R.id.tv_username);
            checkBox = itemView.findViewById(R.id.checkBox);
            checkBox.setClickable(false);
        }

        public void bind(final User user, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemCheck(user);
                    listener.onItemUncheck(user);
                }
            });
        }
    }
}
