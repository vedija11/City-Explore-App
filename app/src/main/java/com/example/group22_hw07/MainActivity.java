package com.example.group22_hw07;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    EditText et_email, et_password;
    Button button_login, button_signUp;
    SignInButton sign_in_button;
    String email, password;

    final int RC_SIGN_IN = 200;

    static FirebaseAuth firebaseAuth;
    static GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth.AuthStateListener authStateListener;
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference userRef = db.collection("Users");

    User user = new User();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("request", String.valueOf(requestCode));
        switch (requestCode) {
            case RC_SIGN_IN:
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    Log.d("MainActivity", "test " + data.getExtras().toString());

                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account);
                } catch (ApiException e) {
                    // Google Sign In failed, update UI appropriately
                    Log.d("MainActivity", "Google sign in failed", e);
                }
                break;
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d("MainActivity", "firebaseAuthWithGoogle:" + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            final FirebaseUser user = firebaseAuth.getCurrentUser();
                            String UID = user.getUid();
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            final DocumentReference documentReference = db.collection("Users").document(UID);
                            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (!document.exists()) {
                                            final User dbUser = new User();
                                            dbUser.setFirst_name(user.getDisplayName());
                                            dbUser.setLast_name(user.getDisplayName());
                                            dbUser.setEmailID(user.getEmail());
                                            dbUser.setProfile_pic_URL(String.valueOf(user.getPhotoUrl()));
                                            dbUser.setGender("Other");

                                            Map<String, Object> newMap = dbUser.toHashMap();
                                            userRef.document(firebaseAuth.getCurrentUser().getUid())
                                                    .set(newMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Intent i = new Intent(MainActivity.this, ViewTripsActivity.class);
                                                        startActivity(i);
                                                        Log.d("MainActivity", "signInWithCredential:success");
                                                        Toast.makeText(MainActivity.this, "Welcome " + dbUser.getFirst_name(), Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    } else {
                                                        Toast.makeText(MainActivity.this, "Google user not added", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                        else {
                                            Intent i = new Intent(MainActivity.this, ViewTripsActivity.class);
                                            startActivity(i);
                                            Log.d("MainActivity", "signInWithCredential:success");
                                            finish();
                                        }
                                    } else {
                                        Log.d("test", "get failed with ", task.getException());
                                    }

                                }
                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "signInWithCredential:failure", Toast.LENGTH_SHORT).show();
                            Log.d("MainActivity", "signInWithCredential:failure", task.getException());

                        }

                    }
                });
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Login");

        firebaseAuth = FirebaseAuth.getInstance();

        et_email = findViewById(R.id.et_emailId);
        et_password = findViewById(R.id.et_password);
        button_login = findViewById(R.id.button_login);
        button_signUp = findViewById(R.id.button_signUp);
        sign_in_button = findViewById(R.id.sign_in_button);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null) {
                    Toast.makeText(MainActivity.this, "Logged In", Toast.LENGTH_SHORT).show();

                    Intent tripIntent = new Intent(MainActivity.this, ViewTripsActivity.class);
                    startActivity(tripIntent);
                    finish();
                }
            }
        };

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = et_email.getText().toString();
                password = et_password.getText().toString();

                if (email.isEmpty()) {
                    et_email.setError("Please enter Email ID");
                    et_email.requestFocus();
                } else if (password.isEmpty()) {
                    et_password.setError("Please enter a password");
                    et_password.requestFocus();
                } else if (email.isEmpty() && password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Fields are empty!", Toast.LENGTH_SHORT).show();
                } else if (!(email.isEmpty() && password.isEmpty())) {
                    firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                userRef.document(firebaseAuth.getCurrentUser().getUid()).get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                User user = new User(documentSnapshot.getData());
                                                Log.d("Login user", user.toString());
                                                Toast.makeText(MainActivity.this, "Welcome " + user.first_name, Toast.LENGTH_SHORT).show();

                                                Intent tripIntent = new Intent(MainActivity.this, ViewTripsActivity.class);
                                                startActivity(tripIntent);
                                                finish();
                                            }
                                        });
                            } else {
                                Toast.makeText(MainActivity.this, "Login Unsuccessful!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "Error occurred!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        button_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signUpIntent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(signUpIntent);
                finish();
            }
        });


        sign_in_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }

}
