package com.example.group22_hw07;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.net.HttpURLConnection;
import java.net.URL;

public class MessageAdapter extends FirestoreRecyclerAdapter<Message, MessageAdapter.MessageHolder> {
    Context context;
    String userId;
    int isMsgPhoto = 1;
    private final int MESSAGE_IN_VIEW_TYPE = 1;
    private final int MESSAGE_OUT_VIEW_TYPE = 2;

    public MessageAdapter(@NonNull Context context, Query query, String userID) {
        /*
        Configure recycler adapter options:
        query defines the request made to Firestore
        Message.class instructs the adapter to convert each DocumentSnapshot to a Message object
        */
        super(new FirestoreRecyclerOptions.Builder<Message>()
                .setQuery(query, Message.class)
                .build());
        this.context = context;
        this.userId = userID;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).getMessage_userid().equals(userId)) {
            return MESSAGE_OUT_VIEW_TYPE;
        }
        return MESSAGE_IN_VIEW_TYPE;
    }

    @Override
    protected void onBindViewHolder(@NonNull MessageHolder holder, final int position, @NonNull final Message model) {
        Log.d("Data", "onBindViewHolder: " + model.toString());
        ImageView imageView_ChatPhoto = holder.imageView_ChatPhoto;
        TextView mText = holder.mText;
        TextView mUsername = holder.mUsername;
        TextView mTime = holder.mTime;
        ImageButton btn_delete = holder.btn_delete;
        if (model.message_text.startsWith("https://")) {
            Picasso.get().load(model.message_text).into(imageView_ChatPhoto);
            mText.setVisibility(View.GONE);
        } else {
            Log.d("imageGone", "onBindViewHolder: " + model.toString());
            imageView_ChatPhoto.setVisibility(View.GONE);
        }
        mText.setText(model.message_text);
        mTime.setText(DateFormat.format("dd MMM  (h:mm a)", model.message_time));
        mUsername.setText(model.message_user);
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(model.message_userid)) {
                    FirebaseFirestore.getInstance().collection(model.MessageCollectionID).document(model.messageID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            ChatRoomActivity.adapter.notifyDataSetChanged();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Clicked" + position, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == MESSAGE_IN_VIEW_TYPE) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_in_view_layout, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_out_view_layout, parent, false);
        }
        Log.d("Recycler", "onBindViewHolder: " + view.toString());
        return new MessageHolder(view);
    }

    public class MessageHolder extends RecyclerView.ViewHolder {
        TextView mText;
        TextView mUsername;
        TextView mTime;
        ImageButton btn_delete;
        ImageView imageView_ChatPhoto;
        View view;

        public MessageHolder(View view) {
            super(view);
            this.mText = view.findViewById(R.id.tv_Chat_message);
            this.mUsername = view.findViewById(R.id.tv_Chat_UserName);
            this.mTime = view.findViewById(R.id.tv_Chat_time);
            this.btn_delete = view.findViewById(R.id.btn_Chat_Delete);
            this.imageView_ChatPhoto = view.findViewById(R.id.imageView_ChatPhoto);
            this.view = view;
        }
    }
}
