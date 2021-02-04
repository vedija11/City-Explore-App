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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import com.google.android.libraries.places.api.model.Place;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CreateTripActivity extends AppCompatActivity {

    EditText et_tripName, et_tripDesc;
    ImageButton ib_tripPhoto;
    Button button_create_trip, button_cancel_trip, button_searchUser;
    RecyclerView LocationRecyclerView;
    TextView tv_addUser;
    String tripName, tripDesc, tripPhotoURL;
    Bitmap coverPhoto = null;
    static final int REQUEST_COVER_IMAGE_CAPTURE = 1;
    static final int REQ_USER_LIST = 2;

    FirebaseAuth firebaseAuth;
    String apiKey = "AIzaSyConURnCAQKSBhAixpvUUzHRCBz-tYUoWo";
    ArrayList<String> Locations = new ArrayList<>();
    ArrayList<String> FinalUIDs = new ArrayList<>();
    static LocationAdapter locationAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_trip);
        setTitle("Create a Trip");
        LocationRecyclerView = findViewById(R.id.LocationRecyclerView);
        locationAdapter = new LocationAdapter(Locations);
        LocationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        LocationRecyclerView.setAdapter(locationAdapter);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

        // Create a new Places client instance.
        final PlacesClient placesClient = Places.createClient(this);
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_autocomplete);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME)).setTypeFilter(TypeFilter.CITIES);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i("Test", "Place: " + place.getName() + ", " + place.getId());
                Locations.add(place.getName());
                locationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Status status) {
                Log.i("Test", "An error occurred: " + status);
            }
        });

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        et_tripName = findViewById(R.id.et_tripName);
        et_tripDesc = findViewById(R.id.et_tripDesc);
        ib_tripPhoto = findViewById(R.id.ib_tripPhoto);
        button_create_trip = findViewById(R.id.button_create_trip);
        button_cancel_trip = findViewById(R.id.button_cancel_trip);
        button_searchUser = findViewById(R.id.button_searchUser);
        tv_addUser = findViewById(R.id.tv_addUser);

        ib_tripPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        button_searchUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent searchIntent = new Intent(CreateTripActivity.this, ListUsersActivity.class);
                startActivityForResult(searchIntent, REQ_USER_LIST);
            }
        });

        button_create_trip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tripName = et_tripName.getText().toString();
                tripDesc = et_tripDesc.getText().toString();

                if (TextUtils.isEmpty(et_tripName.getText()) || TextUtils.isEmpty(et_tripDesc.getText())) {
                    if (TextUtils.isEmpty(et_tripName.getText())) {
                        Toast.makeText(CreateTripActivity.this, "Enter Trip Name", Toast.LENGTH_SHORT).show();
                        et_tripName.requestFocus();
                    } else if (TextUtils.isEmpty(et_tripDesc.getText())) {
                        Toast.makeText(CreateTripActivity.this, "Enter Trip Description", Toast.LENGTH_SHORT).show();
                        et_tripDesc.requestFocus();
                    }
                } else {
                    String UID = firebaseAuth.getCurrentUser().getUid();
                    final TripData tripData = new TripData();
                    tripData.setTripName(tripName);
                    tripData.setTripDescription(tripDesc);
                    tripData.setCreatedBy(firebaseAuth.getCurrentUser().getUid());
                    tripData.setPhotoURL(tripPhotoURL);
                    tripData.setLocation(Locations);
                    tripData.UIDs = FinalUIDs;
                    tripData.MessageCollectionID = UUID.randomUUID().toString();
                    Map<String, Object> tripMap = tripData.toHashMap();
                    db.collection("Trips").add(tripMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                db.collection("Trips").document(task.getResult().getId()).update("TripID", task.getResult().getId());
                                Toast.makeText(CreateTripActivity.this, "Trip created successfully!", Toast.LENGTH_SHORT).show();
                                Intent tripIntent = new Intent(CreateTripActivity.this, ViewTripsActivity.class);
                                startActivity(tripIntent);
                                finish();
                            } else {
                                Toast.makeText(CreateTripActivity.this, "Error while creating trip", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("CreateTripActivity", e.toString());
                            Toast.makeText(CreateTripActivity.this, "onFailure: Exception", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        button_cancel_trip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cancelIntent = new Intent(CreateTripActivity.this, ViewTripsActivity.class);
                startActivity(cancelIntent);
                finish();
            }
        });
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
                    tripPhotoURL = task.getResult().toString();
                    Log.d("CreateTripActivity", "onSuccess: " + task.getResult());
                    Picasso.get().load(tripPhotoURL).into(ib_tripPhoto);
                } else {
                    Log.d("CreateTripActivity", "Image not uploaded!" + task.getException());
                }
            }
        });
    }

    //    TAKE PHOTO USING CAMERA...
    private void dispatchTakePictureIntent() {
//        Intent takePictureIntent = new Intent(MediaStore.);
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(takePictureIntent, REQUEST_COVER_IMAGE_CAPTURE);
//        }

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_COVER_IMAGE_CAPTURE);
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
            ib_tripPhoto.setImageBitmap(bitmap);

            coverPhoto = bitmap;

            uploadImage(coverPhoto);
        } else if (requestCode == REQ_USER_LIST && resultCode == RESULT_OK) {
            //User newUser = (User) data.getSerializableExtra("ListUser");
            List<String> fn = data.getStringArrayListExtra("ListUser");
            FinalUIDs = data.getStringArrayListExtra("UIDs");
            Log.d("newUser", fn.toString());
            Log.d("UIDs", FinalUIDs.toString());
            tv_addUser.setText("");
            for (String name : fn) {
                tv_addUser.setText(tv_addUser.getText() + name + "\n");
            }
        }
    }
}
