package com.example.cricscore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cricscore.MainActivity;
import com.example.cricscore.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView ivBall = findViewById(R.id.ivBall);
        TextView tvAppName = findViewById(R.id.tvAppName);

        // 1. Spinning Animation
        ivBall.animate()
                .rotationBy(1440) // 4 full spins
                .setDuration(2500)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // 2. Falling/Bouncing Animation inside the background
        ivBall.setTranslationY(-1200f);
        ivBall.animate()
                .translationY(0)
                .setDuration(1800)
                .setInterpolator(new BounceInterpolator())
                .start();

        // 3. App Name Zoom-In Animation (Bigger text)
        tvAppName.postDelayed(() -> {
            tvAppName.setAlpha(0f);
            tvAppName.setScaleX(0.1f);
            tvAppName.setScaleY(0.1f);
            tvAppName.animate()
                    .alpha(1f)
                    .scaleX(1.2f) // Zoom in slightly larger than 1.0
                    .scaleY(1.2f)
                    .setDuration(1000)
                    .setInterpolator(new OvershootInterpolator())
                    .withEndAction(() -> {
                        // Settle back to 1.0 scale
                        tvAppName.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(300)
                                .start();
                    })
                    .start();
        }, 1500);

        // Transition to MainActivity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, 3500);
    }
}
