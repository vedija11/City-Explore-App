package com.example.group22_hw07;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.UUID;

import static com.example.group22_hw07.CreateTripActivity.REQUEST_COVER_IMAGE_CAPTURE;

public class ChatRoomActivity extends AppCompatActivity {
    RecyclerView ChatRecyclerView;
    FloatingActionButton btn_Send_message;
    EditText et_Message;
    ImageButton btn_Chat_Photo;
    TripData CurrentTripData;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore database;
    Query query;
    String userId;
    String userName;
    static FirestoreRecyclerAdapter<Message, MessageAdapter.MessageHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        Intent getTrip = getIntent();
        CurrentTripData = (TripData) getTrip.getSerializableExtra("TripData");
        Log.d("Chat", "onCreate: " + CurrentTripData.toString());
        setTitle(CurrentTripData.TripName + "Chatroom");

        ChatRecyclerView = findViewById(R.id.ChatRecyclerView);
        btn_Send_message = findViewById(R.id.btn_Send_message);
        btn_Chat_Photo = findViewById(R.id.btn_Chat_Photo);
        et_Message = findViewById(R.id.et_Message);
        ChatRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        btn_Send_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = et_Message.getText().toString();
                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(ChatRoomActivity.this, "Enter Something", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    Message message1 = new Message(userName, message, userId, CurrentTripData.TripID, CurrentTripData.MessageCollectionID);
                    database.collection(CurrentTripData.MessageCollectionID).add(message1).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            database.collection(CurrentTripData.MessageCollectionID).document(documentReference.getId()).update("messageID", documentReference.getId());
                            et_Message.setText("");
                        }
                    });
                }
            }
        });
        btn_Chat_Photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_COVER_IMAGE_CAPTURE);
            }
        });
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userId = user.getUid();
        FirebaseFirestore.getInstance().collection("Users").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User tmp = new User(documentSnapshot.getData());
                userName = tmp.first_name + " " + tmp.last_name;
            }
        });        database = FirebaseFirestore.getInstance();
        query = database.collection(CurrentTripData.MessageCollectionID).orderBy("message_time");
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                }
            }
        });
        adapter = new MessageAdapter(ChatRoomActivity.this, query, userId);
        ChatRecyclerView.setAdapter(adapter);
    }

    //    Upload Camera Photo to Cloud Storage....
    private void uploadImage(Bitmap photoBitmap) {
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();

        final StorageReference imageRepo = storageReference.child("images/trip/" + UUID.randomUUID().toString() + ".jpeg");

//        Converting the Bitmap into a bytearrayOutputstream....
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        photoBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imageRepo.putBytes(data);
//        uploadTask.addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Log.e(TAG, "onFailure: "+e.getMessage());
//            }
//        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                Log.d(TAG, "onSuccess: "+"Image Uploaded!!!");
//            }
//        });

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
//                return null;
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                return imageRepo.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Log.d("CreateTripActivity", "Image Download URL" + task.getResult());
                    Message message1 = new Message(userName, task.getResult().toString(), userId, CurrentTripData.TripID, CurrentTripData.MessageCollectionID);
                    database.collection(CurrentTripData.MessageCollectionID).add(message1).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            database.collection(CurrentTripData.MessageCollectionID).document(documentReference.getId()).update("messageID", documentReference.getId());
                            et_Message.setText("");
                        }
                    });
                    Log.d("CreateTripActivity", "onSuccess: " + task.getResult());
                    //Picasso.get().load(tripPhotoURL).into(ib_tripPhoto);
                } else {
                    Log.d("CreateTripActivity", "Image not uploaded!" + task.getException());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_COVER_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //Bundle extras = data.getExtras();
//            Bitmap bitmap = (Bitmap) extras.get("data");
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
            uploadImage(bitmap);
        }
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(), "Back", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), ViewTripsActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null)
            adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null)
            adapter.stopListening();
    }
}
