package com.example.uploadingdata;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.*;
import java.util.ArrayList;

public class UsersActivity extends AppCompatActivity {

    private RecyclerView usersList;
    private ArrayList<User> userData;
    private UserAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        usersList = findViewById(R.id.usersList);
        usersList.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        userData = new ArrayList<>();
        adapter = new UserAdapter(userData);
        usersList.setAdapter(adapter);

        loadUsers();
    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean loggedIn = getSharedPreferences("auth", MODE_PRIVATE).getBoolean("admin_logged_in", false);
        if (!loggedIn) {
            Intent intent = new Intent(this, AdminLoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void loadUsers() {
        db.collection("users")
                .get()
                .addOnSuccessListener(query -> {
                    userData.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        User user = doc.toObject(User.class);
                        userData.add(user);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading users: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}