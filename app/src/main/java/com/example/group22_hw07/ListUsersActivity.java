package com.example.group22_hw07;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ListUsersActivity extends AppCompatActivity {

    RecyclerView UsersRecyclerView;
    Button button_submit;
    List<User> newUserList = new ArrayList<>();
    List<User> selectedUsers = new ArrayList<>();
    ListUserAdapter adapter;
    final List<String> UIDs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_users);
        setTitle("Add Users to Trip");

        UsersRecyclerView = findViewById(R.id.UsersRecyclerView);
        button_submit = findViewById(R.id.button_submit);

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(final QueryDocumentSnapshot documentSnapshot: task.getResult()){
                        Log.d("ListUsersActivity", documentSnapshot.getId() + " => " + documentSnapshot.getData());
                        User user = new User(documentSnapshot.getData());
                        newUserList.add(user);

                        UsersRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        adapter = new ListUserAdapter(newUserList, new ListUserAdapter.OnItemClickListener() {
                            @Override
                            public void onItemCheck(User item) {
                                selectedUsers.add(item);
                                FirebaseFirestore.getInstance().collection("Users").whereEqualTo("FirstName",item.first_name).whereEqualTo("LastName",item.last_name).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                Log.d("demo", document.getId() + " => " + document.getData());
                                                UIDs.add(document.getId());
                                            }
                                        } else {
                                            Log.d("demo", "Error getting documents: ", task.getException());
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onItemUncheck(User item) {
                                selectedUsers.remove(item);
                                FirebaseFirestore.getInstance().collection("Users").whereEqualTo("FirstName",item.first_name).whereEqualTo("LastName",item.last_name).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                Log.d("demo", document.getId() + " => " + document.getData());
                                                UIDs.remove(document.getId());
                                            }
                                        } else {
                                            Log.d("demo", "Error getting documents: ", task.getException());
                                        }
                                    }
                                });
                            }
                        });
                        UsersRecyclerView.setAdapter(adapter);
                    }
                }
            }
        });

        button_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final List<String> name = new ArrayList<>();
                for(User u : selectedUsers){
                    name.add(u.first_name + " " + u.last_name);
                }
                Intent returnUser = new Intent(ListUsersActivity.this, CreateTripActivity.class);
                returnUser.putExtra("ListUser", (Serializable) name);
                returnUser.putExtra("UIDs", (Serializable) UIDs);
                setResult(-1, returnUser);
                finish();
            }
        });
    }
}
