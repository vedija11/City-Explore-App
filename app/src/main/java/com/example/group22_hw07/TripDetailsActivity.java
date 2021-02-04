package com.example.group22_hw07;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class TripDetailsActivity extends AppCompatActivity {

    TextView tv_trip_name, tv_created_by, tv_trip_desc, tv_tripUsers, tv_tripLocations;
    Button button_join_trip, button_chatroom, button_unfollow, button_cancel;
    ImageView iv_viewTripPhoto;
    ImageButton ib_EditTrip,imageButton_DeleteTrip;

    ArrayList<User> tripUsers = new ArrayList<>();
    String CurrentTripID = "";
    User CurrentUser;
    TripData newTripData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);
        setTitle("Trip Details");

        tv_trip_name = findViewById(R.id.tv_trip_name);
        tv_tripUsers = findViewById(R.id.tv_tripUsers);
        tv_tripLocations = findViewById(R.id.tv_tripLocations);
        tv_created_by = findViewById(R.id.tv_created_by);
        tv_trip_desc = findViewById(R.id.tv_trip_desc);
        button_join_trip = findViewById(R.id.button_join_trip);
        button_chatroom = findViewById(R.id.button_chatroom);
        button_unfollow = findViewById(R.id.button_unfollow);
        iv_viewTripPhoto = findViewById(R.id.iv_viewTripPhoto);
        button_cancel = findViewById(R.id.button_cancel);
        ib_EditTrip = findViewById(R.id.ib_EditTripDetails);
        imageButton_DeleteTrip = findViewById(R.id.imageButton_DeleteTrip);

        ib_EditTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoEditTrip = new Intent(TripDetailsActivity.this, EditTripActivity.class);
                gotoEditTrip.putExtra("TripData", newTripData);
                startActivity(gotoEditTrip);
            }
        });
        button_chatroom.setEnabled(false);
        button_unfollow.setEnabled(false);
        ib_EditTrip.setEnabled(false);
        imageButton_DeleteTrip.setEnabled(false);
        tv_tripUsers.setText("");
        tv_tripLocations.setText("");

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final User[] newUser = new User[1];

        Intent getTrip = getIntent();
        newTripData = (TripData) getTrip.getSerializableExtra("TripData");
        imageButton_DeleteTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("Trips").document(newTripData.TripID).delete();
                Intent gotoViewTrips =new Intent(TripDetailsActivity.this,ViewTripsActivity.class);
                startActivity(gotoViewTrips);
                finish();
            }
        });
        db.collection("Trips").whereEqualTo("TripName", newTripData.TripName).whereEqualTo("CreatedBy", newTripData.CreatedBy).whereEqualTo("TripDescription", newTripData.TripDescription).whereEqualTo("Location", newTripData.Location).whereEqualTo("UIDs", newTripData.UIDs).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d("demo", document.getId() + " => " + document.getData());
                        CurrentTripID = document.getId();
                    }
                } else {
                    Log.d("demo", "Error getting documents: ", task.getException());
                }
            }
        });
        Log.d("TripData", "onCreate: " + newTripData.toString());
        final String CurrentUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DocumentReference currentUserRef = db.collection("Users").document(CurrentUID);
        currentUserRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                CurrentUser = new User(documentSnapshot.getData());
                if (newTripData.UIDs.contains(documentSnapshot.getId())) {
                    button_chatroom.setEnabled(true);
                    button_unfollow.setEnabled(true);
                    ib_EditTrip.setEnabled(true);
                    button_join_trip.setEnabled(false);
                } else {
                    button_chatroom.setEnabled(false);
                    button_unfollow.setEnabled(false);
                    ib_EditTrip.setEnabled(false);
                    button_join_trip.setEnabled(true);
                }
                if (CurrentUID.equals(newTripData.CreatedBy))
                {
                    imageButton_DeleteTrip.setEnabled(true);
                }
            }
        });

        for (String UID : newTripData.UIDs) {
            final DocumentReference documentReference = db.collection("Users").document(UID);
            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    User tmp = new User(documentSnapshot.getData());
                    tv_tripUsers.setText(tv_tripUsers.getText() + tmp.first_name + " " + tmp.last_name + "\n");
                }
            });
        }

        for (String each : newTripData.Location) {
            tv_tripLocations.setText(tv_tripLocations.getText() + each + "\n");
        }

        final String userID = newTripData.CreatedBy;
        final DocumentReference documentReference = db.collection("Users").document(userID);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                newUser[0] = new User(documentSnapshot.getData());
                tv_created_by.setText(newUser[0].first_name + " " + newUser[0].last_name);
            }
        });

        tv_trip_name.setText(newTripData.TripName);
        tv_trip_desc.setText(newTripData.TripDescription);
        Picasso.get().load(newTripData.PhotoURL).into(iv_viewTripPhoto);

        button_join_trip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button_chatroom.setEnabled(true);
                button_unfollow.setEnabled(true);
                ib_EditTrip.setEnabled(true);
                Log.d("TripID", "onClick: " + CurrentTripID);
                db.collection("Trips").document(CurrentTripID).update("UIDs", FieldValue.arrayUnion(CurrentUID));
                String tmp = tv_tripUsers.getText() + "\n" + CurrentUser.first_name + " " + CurrentUser.last_name;
                tv_tripUsers.setText(tmp.trim());
                tripUsers.add(newUser[0]);
                Toast.makeText(TripDetailsActivity.this, newUser[0].first_name + " is now going on this trip", Toast.LENGTH_SHORT).show();
                button_join_trip.setEnabled(false);
            }
        });

        button_unfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tripUsers.remove(newUser[0]);
                db.collection("Trips").document(CurrentTripID).update("UIDs", FieldValue.arrayRemove(CurrentUID));
                String tmp = tv_tripUsers.getText().toString();
                tmp = tmp.replace(CurrentUser.first_name + " " + CurrentUser.last_name, "");
                tv_tripUsers.setText(tmp.trim());
                Toast.makeText(TripDetailsActivity.this, newUser[0].first_name + " is not going on this trip", Toast.LENGTH_SHORT).show();
                button_join_trip.setEnabled(true);
                button_chatroom.setEnabled(false);
                button_unfollow.setEnabled(false);
                ib_EditTrip.setEnabled(false);
            }
        });

        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cancelIntent = new Intent(TripDetailsActivity.this, ViewTripsActivity.class);
                startActivity(cancelIntent);
                finish();
            }
        });
        button_chatroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ChatIntent = new Intent(TripDetailsActivity.this, ChatRoomActivity.class);
                ChatIntent.putExtra("TripData", newTripData);
                startActivity(ChatIntent);
                finish();
            }
        });
    }
}
