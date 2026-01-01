package com.example.uploadingdata;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.splashLogo);
        TextView title = findViewById(R.id.splashTitle);

        ObjectAnimator logoAlpha = ObjectAnimator.ofFloat(logo, View.ALPHA, 0f, 1f);
        logoAlpha.setDuration(700);
        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(logo, View.SCALE_X, 0.8f, 1f);
        logoScaleX.setDuration(700);
        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(logo, View.SCALE_Y, 0.8f, 1f);
        logoScaleY.setDuration(700);

        ObjectAnimator titleAlpha = ObjectAnimator.ofFloat(title, View.ALPHA, 0f, 1f);
        titleAlpha.setStartDelay(400);
        titleAlpha.setDuration(600);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(logoAlpha, logoScaleX, logoScaleY, titleAlpha);
        set.start();

        new Handler().postDelayed(this::routeNext, 1500);
    }

    private void routeNext() {
        boolean admin = getSharedPreferences("auth", MODE_PRIVATE).getBoolean("admin_logged_in", false);
        Intent intent;
        if (admin) {
            intent = new Intent(this, AdminDashboard.class);
        } else if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            intent = new Intent(this, UserHomeActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}