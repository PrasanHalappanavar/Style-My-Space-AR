package com.example.uploadingdata;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class AdminLoginActivity extends AppCompatActivity {

    private EditText adminEmail, adminPassword;
    private Button btnAdminLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        adminEmail = findViewById(R.id.adminEmail);
        adminPassword = findViewById(R.id.adminPassword);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);

        btnAdminLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate(adminEmail.getText().toString(), adminPassword.getText().toString());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isAdminLoggedIn()) {
            Intent intent = new Intent(this, AdminDashboard.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    public void validate(String inputEmail, String password) {

        Log.d("Value", "is email " +inputEmail);
        Log.d("Value", "is password" +password);

        if (inputEmail.equals("psh")){
            if(password.equals("psh")){
                setAdminLoggedIn(true);
                Intent intent = new Intent(AdminLoginActivity.this, AdminDashboard.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                Toast.makeText(AdminLoginActivity.this, "You have successfully logged in", Toast.LENGTH_SHORT).show();
            }}
        else{
            Toast.makeText(AdminLoginActivity.this,"Please enter correct credentials", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isAdminLoggedIn() {
        return getSharedPreferences("auth", MODE_PRIVATE).getBoolean("admin_logged_in", false);
    }

    private void setAdminLoggedIn(boolean value) {
        getSharedPreferences("auth", MODE_PRIVATE).edit().putBoolean("admin_logged_in", value).apply();
    }
}

