package com.example.group22_hw07;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.util.Map;

import static com.example.group22_hw07.MainActivity.firebaseAuth;


public class ViewTripsActivity extends AppCompatActivity {
    FloatingActionButton button_add_trip, button_signout, button_edit_profile;
    TextView tv_userName;

    RecyclerView recyclerView;
    FirestoreRecyclerAdapter<TripData,TripHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_trips);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");
        tv_userName = findViewById(R.id.tv_userName);
        recyclerView = findViewById(R.id.rv_TripsView);

        String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db =FirebaseFirestore.getInstance();
        final DocumentReference documentReference = db.collection("Users").document(UID);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = new User(documentSnapshot.getData());
                Log.d("demo", user.toString());
                tv_userName.setText(user.getFirst_name() + " " + user.getLast_name());
                recyclerView.setLayoutManager(new LinearLayoutManager(ViewTripsActivity.this));
                recyclerView.setHasFixedSize(true);
                fetch();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("error", e.toString());
            }
        });

        button_add_trip = findViewById(R.id.button_add_trip);
        button_add_trip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Add Trip", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                Intent gotoAddTrip = new Intent(ViewTripsActivity.this,CreateTripActivity.class);
                startActivity(gotoAddTrip);
                finish();
            }
        });

        button_signout = findViewById(R.id.button_signout);
        button_signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                MainActivity.mGoogleSignInClient.signOut().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent gotoMainIntent = new Intent(ViewTripsActivity.this, MainActivity.class);
                        startActivity(gotoMainIntent);
                    }
                });
            }
        });

        button_edit_profile = findViewById(R.id.button_edit_profile);
        button_edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Go to Edit Page", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                Intent gotoEditProfile = new Intent(ViewTripsActivity.this, EditProfileActivity.class);
                startActivity(gotoEditProfile);
                finish();
            }
        });
    }

    private void fetch() {

        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        Query query = rootRef.collection("Trips").orderBy("TripName");
        FirestoreRecyclerOptions<TripData> options =
                new FirestoreRecyclerOptions.Builder<TripData>()
                        .setQuery(query, TripData.class)
                        .build();
        Log.d("Q", query.toString());

        adapter = new FirestoreRecyclerAdapter<TripData, TripHolder>(options) {
            public TripHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.trip_recycler_view, parent, false);
                Log.d("View", view.toString());

                return new TripHolder(view);
            }


            @Override
            protected void onBindViewHolder(final TripHolder holder, final int position, final TripData tripData) {
                Log.d("View", tripData.toString());
                Log.d("UIDs", String.valueOf(tripData.UIDs));

                final TextView TripName = holder.tv_TripName;
                final TextView TripDescription = holder.tv_TripDescription;
                final TextView TripCreatedBy = holder.tv_TripCreatedBy;
                final ImageView TripPhoto = holder.iv_TripPhoto;
                holder.newView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent goToViewTrip = new Intent(ViewTripsActivity.this, TripDetailsActivity.class);
                        goToViewTrip.putExtra("TripData", tripData);
                        startActivity(goToViewTrip);
                        finish();
                    }
                });

                String UID = tripData.CreatedBy;
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                final DocumentReference documentReference = db.collection("Users").document(UID);
                documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User user = new User(documentSnapshot.getData());
                        TripName.setText(tripData.TripName);
                        TripDescription.setText(tripData.TripDescription);
                        TripCreatedBy.setText(user.first_name+ " " +user.last_name);
                        Picasso.get().load(tripData.PhotoURL).into(TripPhoto);
                    }
                });
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }
    public class TripHolder extends RecyclerView.ViewHolder {
        TextView tv_TripName;
        TextView tv_TripDescription;
        TextView tv_TripCreatedBy;
        ImageView iv_TripPhoto;
        View newView;

        public TripHolder(View itemView) {
            super(itemView);
            newView = itemView;
            tv_TripName = itemView.findViewById(R.id.tv_TripName);
            tv_TripDescription = itemView.findViewById(R.id.tv_TripDescription);
            tv_TripCreatedBy = itemView.findViewById(R.id.tv_TripCreatedBy);
            iv_TripPhoto = itemView.findViewById(R.id.iv_TripPhoto);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

}
