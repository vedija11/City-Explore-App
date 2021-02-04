package com.example.group22_hw07;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class SignUpActivity extends AppCompatActivity {

    EditText et_fname, et_lname, et_emailId, et_password;
    RadioGroup radioGroup;
    RadioButton rb_male, rb_female, rb_other;
    ImageButton ib_photo;
    Button button_register, button_cancel;

    String firstName, lastName, emailId, gender, password,PhotoURL=null;
    Bitmap profilePhotoUpload = null;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setTitle("Sign Up");

        final FirebaseFirestore db =FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        ib_photo = findViewById(R.id.ib_photo);
        et_fname = findViewById(R.id.et_fname);
        et_lname = findViewById(R.id.et_lname);
        radioGroup = findViewById(R.id.radioGroup);
        rb_male = findViewById(R.id.rb_male);
        rb_female = findViewById(R.id.rb_female);
        rb_other = findViewById(R.id.rb_other);
        et_emailId = findViewById(R.id.et_emailId);
        et_password = findViewById(R.id.et_password);
        button_register = findViewById(R.id.button_register);
        button_cancel = findViewById(R.id.button_cancel);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.rb_male:
                        gender = "Male";
                        break;
                    case R.id.rb_female:
                        gender = "Female";
                        break;
                    case R.id.rb_other:
                        gender = "Other";
                        break;
                }
            }
        });

        ib_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                firstName = et_fname.getText().toString();
                lastName = et_lname.getText().toString();
                emailId = et_emailId.getText().toString();
                password = et_password.getText().toString();
                PhotoURL=(String)ib_photo.getTag();
                if (TextUtils.isEmpty(et_fname.getText()) || TextUtils.isEmpty(et_lname.getText()) || TextUtils.isEmpty(et_emailId.getText()) || TextUtils.isEmpty(et_password.getText())) {
                    if (TextUtils.isEmpty(et_fname.getText())) {
                        Toast.makeText(SignUpActivity.this, "Enter First Name", Toast.LENGTH_SHORT).show();
                        et_fname.requestFocus();
                    } else if (TextUtils.isEmpty(et_lname.getText())) {
                        Toast.makeText(SignUpActivity.this, "Enter Last Name", Toast.LENGTH_SHORT).show();
                        et_lname.requestFocus();
                    } else if (TextUtils.isEmpty(et_emailId.getText())) {
                        Toast.makeText(SignUpActivity.this, "Enter Email ID", Toast.LENGTH_SHORT).show();
                        et_emailId.requestFocus();
                    } else if (TextUtils.isEmpty(et_password.getText())) {
                        Toast.makeText(SignUpActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
                        et_password.requestFocus();
                    }
                } else {
                    firebaseAuth.createUserWithEmailAndPassword(emailId, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                //uploadImage(profilePhotoUpload);

                                final User user = new User();
                                user.setFirst_name(firstName);
                                user.setLast_name(lastName);
                                user.setGender(gender);
                                user.setEmailID(emailId);
                                user.setPassword(password);
                                user.setProfile_pic_URL(PhotoURL);
                                Log.d("Save Profile", "onSuccess: " + PhotoURL);


                                Map<String, Object> userMap = user.toHashMap();
                                db.collection("Users").document(firebaseAuth.getCurrentUser().getUid())
                                        .set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("SignUpActivity: NewUser", user.toString());
                                            Toast.makeText(SignUpActivity.this, "User created!", Toast.LENGTH_SHORT).show();

                                            Intent i = new Intent(SignUpActivity.this, MainActivity.class);
                                            i.putExtra("User", user);
                                            startActivity(i);
                                            finish();

                                        } else {
                                            Log.e("SignUpActivity", task.getException().toString());
                                            Toast.makeText(SignUpActivity.this, "Error while adding user..." + firebaseAuth.getCurrentUser().getUid(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            } else {
                                Toast.makeText(SignUpActivity.this, "SignUp Unsuccessful!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cancelIntent = new Intent(SignUpActivity.this, MainActivity.class);
                startActivity(cancelIntent);
                finish();
            }
        });

    }

    //    Upload Camera Photo to Cloud Storage....
    private void uploadImage(Bitmap photoBitmap) {
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();

        final StorageReference imageRepo = storageReference.child("images/users/" + UUID.randomUUID().toString() + ".jpeg");

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
                    Picasso.get().load(PhotoURL).into(ib_photo);
                    ib_photo.setTag(PhotoURL);

                } else {
                    Log.d("SignUpActivity", "Image not uploaded!" + task.getException());
                }
            }
        });
    }

    //    TAKE PHOTO USING CAMERA...
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap bitmap = (Bitmap) extras.get("data");
            ib_photo.setImageBitmap(bitmap);

            profilePhotoUpload = bitmap;

            uploadImage(profilePhotoUpload);
        }
    }
}
