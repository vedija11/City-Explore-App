package com.example.group22_hw07;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import static com.example.group22_hw07.SignUpActivity.REQUEST_IMAGE_CAPTURE;

public class EditProfileActivity extends AppCompatActivity {

    EditText et_edit_fname, et_edit_lname, et_edit_emailId, et_password;
    RadioGroup radioGroup_edit;
    RadioButton rb_edit_male, rb_edit_female, rb_edit_other;
    ImageButton ib_edit_photo;
    Button button_save, button_edit_cancel;
    String firstName, lastName, emailId, gender, password,PhotoURL;
    Bitmap profilePhotoUpload = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        setTitle("Edit Profile");

        ib_edit_photo = findViewById(R.id.ib_edit_photo);
        et_edit_fname = findViewById(R.id.et_edit_fname);
        et_edit_lname = findViewById(R.id.et_edit_lname);
        radioGroup_edit = findViewById(R.id.radioGroup_edit);
        rb_edit_male = findViewById(R.id.rb_edit_male);
        rb_edit_female = findViewById(R.id.rb_edit_female);
        rb_edit_other = findViewById(R.id.rb_edit_other);
        et_edit_emailId = findViewById(R.id.et_edit_emailId);
        et_password = findViewById(R.id.et_password);
        button_save = findViewById(R.id.button_save);
        button_edit_cancel = findViewById(R.id.button_edit_cancel);

        String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db =FirebaseFirestore.getInstance();
        final DocumentReference documentReference = db.collection("Users").document(UID);

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = new User(documentSnapshot.getData());
                Log.d("EditProfile user......", String.valueOf(user));

                et_edit_fname.setText(user.getFirst_name());
                et_edit_lname.setText(user.getLast_name());
                et_edit_emailId.setText(user.getEmailID());
                et_password.setText(user.getPassword());
                PhotoURL=user.profile_pic_URL;
                Picasso.get().load(user.profile_pic_URL).into(ib_edit_photo);
                ib_edit_photo.setTag(PhotoURL);

                gender = user.getGender();
                if (gender.equals("Male")) {
                    rb_edit_male.setChecked(true);
                    rb_edit_female.setChecked(false);
                    rb_edit_other.setChecked(false);
                } else if (gender.equals("Female")) {
                    rb_edit_female.setChecked(true);
                    rb_edit_male.setChecked(false);
                    rb_edit_other.setChecked(false);
                } else {
                    rb_edit_other.setChecked(true);
                    rb_edit_male.setChecked(false);
                    rb_edit_female.setChecked(false);
                }
            }
        });

        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                firstName = et_edit_fname.getText().toString();
                lastName = et_edit_lname.getText().toString();
                emailId = et_edit_emailId.getText().toString();
                password = et_password.getText().toString();

                switch (radioGroup_edit.getCheckedRadioButtonId()) {
                    case R.id.rb_edit_male:
                        gender = "Male";
                        break;
                    case R.id.rb_edit_female:
                        gender = "Female";
                        break;
                    case R.id.rb_edit_other:
                        gender = "Other";
                        break;
                }

                if (TextUtils.isEmpty(et_edit_fname.getText()) || TextUtils.isEmpty(et_edit_lname.getText()) || TextUtils.isEmpty(et_edit_emailId.getText()) || TextUtils.isEmpty(et_password.getText())) {
                    if (TextUtils.isEmpty(et_edit_fname.getText())) {
                        Toast.makeText(EditProfileActivity.this, "Enter First Name", Toast.LENGTH_SHORT).show();
                        et_edit_fname.requestFocus();
                    } else if (TextUtils.isEmpty(et_edit_lname.getText())) {
                        Toast.makeText(EditProfileActivity.this, "Enter Last Name", Toast.LENGTH_SHORT).show();
                        et_edit_lname.requestFocus();
                    } else if (TextUtils.isEmpty(et_edit_emailId.getText())) {
                        Toast.makeText(EditProfileActivity.this, "Enter Email ID", Toast.LENGTH_SHORT).show();
                        et_edit_emailId.requestFocus();
                    } else if (TextUtils.isEmpty(et_password.getText())) {
                        Toast.makeText(EditProfileActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
                        et_password.requestFocus();
                    }
                } else {

                    final User user = new User();
                    user.setFirst_name(firstName);
                    user.setLast_name(lastName);
                    user.setGender(gender);
                    user.setEmailID(emailId);
                    user.setPassword(password);
                    user.setProfile_pic_URL(PhotoURL);
                    Log.d("Save Profile", "onSuccess: " + PhotoURL);

                    Map<String, Object> userMap = user.toHashMap();

                    documentReference.update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("EditProfile", "Updated!!");
                            Toast.makeText(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                            Intent goToTrip = new Intent(EditProfileActivity.this, ViewTripsActivity.class);
                            startActivity(goToTrip);
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                            Log.d("EditProfile", "Failed to update profile");
                        }
                    });
                }
            }
        });

        button_edit_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cancelIntent = new Intent(EditProfileActivity.this, ViewTripsActivity.class);
                startActivity(cancelIntent);
                finish();
            }
        });

        ib_edit_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
    }

    //    Upload Camera Photo to Cloud Storage....
    private void uploadImage(Bitmap photoBitmap) {
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();

        final StorageReference imageRepo = storageReference.child("images/" + UUID.randomUUID().toString() + ".jpeg");

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
                    Log.d("SignUpActivity", "Image Download URL" + task.getResult());
                    PhotoURL = task.getResult().toString();
                    Log.d("SignUpActivity", "onSuccess: " + task.getResult());
                    Picasso.get().load(PhotoURL).into(ib_edit_photo);
                    ib_edit_photo.setTag(PhotoURL);
                } else {
                    Log.d("SignUpActivity", "Image not uploaded!" + task.getException());
                }
            }
        });
    }

    //    TAKE PHOTO USING CAMERA...
    private void dispatchTakePictureIntent() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }

            ib_edit_photo.setImageBitmap(bitmap);

            profilePhotoUpload = bitmap;

            uploadImage(profilePhotoUpload);
        }
    }
}
