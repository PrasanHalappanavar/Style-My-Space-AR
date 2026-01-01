package com.example.uploadingdata;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText resetEmail;
    private Button btnResetPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        resetEmail = findViewById(R.id.resetEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        mAuth = FirebaseAuth.getInstance();

        btnResetPassword.setOnClickListener(v -> {
            String email = resetEmail.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                resetEmail.setError("Please enter your email");
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(ForgotPasswordActivity.this,
                                    "Reset link sent to your email", Toast.LENGTH_LONG).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(ForgotPasswordActivity.this,
                                    "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
        });
    }
}
