package com.example.uploadingdata;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String userId;

    private TextView txtName, txtEmail, txtPhone, txtVerified;
    private Button btnUpdate, btnChangePassword, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        }

        userId = currentUser.getUid();

        txtName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        txtPhone = findViewById(R.id.txtPhone);
        txtVerified = findViewById(R.id.txtVerified);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLogout = findViewById(R.id.btnLogout);

        loadUserData();

        RecyclerView recyclerOrders = findViewById(R.id.recyclerOrders);
        List<Order> orderList = new ArrayList<>();
        UserOrdersAdapter adapter = new UserOrdersAdapter(this, orderList);
        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerOrders.setAdapter(adapter);

        db.collection("orders")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error loading orders", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    orderList.clear();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Order order = doc.toObject(Order.class);
                            if (order != null) orderList.add(order);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_profile);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, UserHomeActivity.class));
                overridePendingTransition(0,0);
                return true;
            } else if (id == R.id.nav_camera) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0,0);
                return true;
            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(this, CartActivity.class));
                overridePendingTransition(0,0);
                return true;
            } else if (id == R.id.nav_profile) {
                return true;
            }
            return false;
        });

        btnUpdate.setOnClickListener(v -> showUpdateDialog());
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadUserData() {
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                txtName.setText(documentSnapshot.getString("name"));
                txtEmail.setText(documentSnapshot.getString("email"));
                txtPhone.setText(documentSnapshot.getString("phone"));
                Boolean verified = documentSnapshot.getBoolean("verified");
                txtVerified.setText(verified != null && verified ? "Verified ✅" : "Not Verified ❌");
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error fetching profile", Toast.LENGTH_SHORT).show());
    }

    private void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_update_profile, null);

        TextInputEditText edtName = view.findViewById(R.id.edtName);
        TextInputEditText edtPhone = view.findViewById(R.id.edtPhone);

        edtName.setText(txtName.getText().toString());
        edtPhone.setText(txtPhone.getText().toString());

        builder.setView(view)
                .setTitle("Update Profile")
                .setPositiveButton("Update", (dialog, which) -> {
                    String newName = edtName.getText().toString().trim();
                    String newPhone = edtPhone.getText().toString().trim();

                    if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newPhone)) {
                        Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    updateUserDetails(newName, newPhone);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create().show();
    }

    private void updateUserDetails(String name, String phone) {
        DocumentReference docRef = db.collection("users").document(userId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);

        docRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                    loadUserData();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show());
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        TextInputEditText edtOldPassword = view.findViewById(R.id.edtOldPassword);
        TextInputEditText edtNewPassword = view.findViewById(R.id.edtNewPassword);

        builder.setView(view);
        builder.setTitle("Change Password");
        builder.setPositiveButton("Update", (dialog, which) -> {
            String oldPass = edtOldPassword.getText().toString().trim();
            String newPass = edtNewPassword.getText().toString().trim();

            if (TextUtils.isEmpty(oldPass) || TextUtils.isEmpty(newPass)) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isPasswordStrong(newPass)) {
                Toast.makeText(this, "Password must be at least 6 characters and include letters & numbers", Toast.LENGTH_LONG).show();
                return;
            }

            updatePassword(oldPass, newPass);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    /** Password must have at least 6 characters and at least one letter and number */
    private boolean isPasswordStrong(String password) {
        if (password.length() < 6) return false;
        boolean hasLetter = false, hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        return hasLetter && hasDigit;
    }

    private void updatePassword(String oldPass, String newPass) {
        if (currentUser == null || currentUser.getEmail() == null) return;

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Updating password...");
        pd.show();

        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), oldPass);

        currentUser.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> currentUser.updatePassword(newPass)
                        .addOnSuccessListener(a -> {
                            pd.dismiss();
                            Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            pd.dismiss();
                            Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(this, "Incorrect old password", Toast.LENGTH_SHORT).show();
                });
    }
}
