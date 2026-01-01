package com.example.uploadingdata;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.*;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameInput, phoneInput, emailInput, passwordInput;
    private Button btnSendOtp, btnCheckVerification, btnCreateAccount;
    private TextView loginRedirect, verificationStatus;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String email, tempPassword = "TempPass123!"; // Temporary password for verification
    private boolean isEmailVerified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        nameInput = findViewById(R.id.nameInput);
        phoneInput = findViewById(R.id.phoneInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        btnCheckVerification = findViewById(R.id.btnCheckVerification);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        loginRedirect = findViewById(R.id.loginRedirect);
        verificationStatus = findViewById(R.id.verificationStatus);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void setupClickListeners() {
        btnSendOtp.setOnClickListener(v -> sendVerificationEmail());
        btnCheckVerification.setOnClickListener(v -> checkEmailVerification());
        btnCreateAccount.setOnClickListener(v -> createFinalAccount());
        loginRedirect.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));
    }

    private void sendVerificationEmail() {
        email = emailInput.getText().toString().trim();

        // Validate email
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable verify button to prevent multiple clicks
        btnSendOtp.setEnabled(false);
        btnSendOtp.setText("Sending...");

        // Create temporary account and send verification email
        mAuth.createUserWithEmailAndPassword(email, tempPassword)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        user.sendEmailVerification()
                                .addOnSuccessListener(aVoid -> {
                                    // Email sent successfully - TOAST MESSAGE HERE
                                    Toast.makeText(this,
                                            "Verification email sent to " + email + ". Please check your inbox.",
                                            Toast.LENGTH_LONG).show();

                                    // Show verification status and check button
                                    verificationStatus.setText("Verification email sent! Please check your email and click the verification link.");
                                    verificationStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                                    verificationStatus.setVisibility(View.VISIBLE);

                                    btnCheckVerification.setVisibility(View.VISIBLE);
                                    btnSendOtp.setText("Resend Email");
                                    btnSendOtp.setEnabled(true);

                                    // Sign out the temporary account
                                    mAuth.signOut();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this,
                                            "Failed to send verification email: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();

                                    btnSendOtp.setText("Verify");
                                    btnSendOtp.setEnabled(true);

                                    // Delete the temporary account if verification email failed
                                    user.delete();
                                    mAuth.signOut();
                                });
                    } else {
                        btnSendOtp.setText("Verify");
                        btnSendOtp.setEnabled(true);
                        Toast.makeText(this, "Failed to create temporary account", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    btnSendOtp.setText("Verify");
                    btnSendOtp.setEnabled(true);

                    if (e.getMessage().contains("email address is already in use")) {
                        // Email already exists, try to sign in and send verification
                        signInAndCheckVerification();
                    } else {
                        Toast.makeText(this,
                                "Failed to send verification: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInAndCheckVerification() {
        mAuth.signInWithEmailAndPassword(email, tempPassword)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        if (user.isEmailVerified()) {
                            // Email already verified
                            enableRegistrationForm();
                            mAuth.signOut();
                        } else {
                            // Resend verification email
                            user.sendEmailVerification()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this,
                                                "Verification email sent to " + email,
                                                Toast.LENGTH_LONG).show();

                                        verificationStatus.setText("Verification email sent! Please check your email and click the verification link.");
                                        verificationStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                                        verificationStatus.setVisibility(View.VISIBLE);
                                        btnCheckVerification.setVisibility(View.VISIBLE);

                                        mAuth.signOut();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this,
                                                "Failed to send verification email: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                        mAuth.signOut();
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Please use the correct email or try again", Toast.LENGTH_SHORT).show();
                });
    }

    private void checkEmailVerification() {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email first", Toast.LENGTH_SHORT).show();
            return;
        }

        btnCheckVerification.setEnabled(false);
        btnCheckVerification.setText("Checking...");

        // Sign in temporarily to check verification status
        mAuth.signInWithEmailAndPassword(email, tempPassword)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        // Reload user to get updated verification status
                        user.reload().addOnCompleteListener(task -> {
                            if (user.isEmailVerified()) {
                                // Email verified successfully
                                Toast.makeText(this, "Email verified successfully!", Toast.LENGTH_SHORT).show();
                                enableRegistrationForm();

                                // Delete temporary account
                                user.delete().addOnCompleteListener(deleteTask -> mAuth.signOut());
                            } else {
                                Toast.makeText(this, "Email not yet verified. Please check your email and click the verification link.", Toast.LENGTH_LONG).show();
                                mAuth.signOut();
                            }

                            btnCheckVerification.setText("Check Email Verification");
                            btnCheckVerification.setEnabled(true);
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Unable to check verification status. Please try again.", Toast.LENGTH_SHORT).show();
                    btnCheckVerification.setText("Check Email Verification");
                    btnCheckVerification.setEnabled(true);
                });
    }

    private void enableRegistrationForm() {
        isEmailVerified = true;

        // Enable all form fields
        nameInput.setEnabled(true);
        phoneInput.setEnabled(true);
        passwordInput.setEnabled(true);
        btnCreateAccount.setEnabled(true);

        // Update verification status
        verificationStatus.setText("✓ Email verified! You can now complete your registration.");
        verificationStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

        // Hide verification button and disable email field
        btnCheckVerification.setVisibility(View.GONE);
        btnSendOtp.setVisibility(View.GONE);
        emailInput.setEnabled(false);

        Toast.makeText(this, "Please complete your registration details", Toast.LENGTH_SHORT).show();
    }

    private void createFinalAccount() {
        if (!isEmailVerified) {
            Toast.makeText(this, "Please verify your email first", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate all fields
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable create account button
        btnCreateAccount.setEnabled(false);
        btnCreateAccount.setText("Creating Account...");

        // Create final account with verified email
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        // Save user data to Firestore
                        saveUserDataToFirestore(user, name, phone, email);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnCreateAccount.setText("Create Account");
                    btnCreateAccount.setEnabled(true);
                });
    }

    private void saveUserDataToFirestore(FirebaseUser user, String name, String phone, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("phone", phone);
        userData.put("email", email);
        userData.put("verified", true);
        userData.put("uid", user.getUid());

        db.collection("users").document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();

                    // Redirect to login or main activity
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.putExtra("registered_email", email);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    // Still redirect to login even if Firestore fails
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.putExtra("registered_email", email);
                    startActivity(intent);
                    finish();
                });
    }
}
