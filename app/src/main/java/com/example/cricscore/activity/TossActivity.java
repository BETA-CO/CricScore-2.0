package com.example.cricscore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cricscore.R;
import com.google.android.material.button.MaterialButton;

import java.util.Random;

public class TossActivity extends AppCompatActivity {

    private String teamA, teamB, playersA, playersB;
    private int playerCount, totalOvers;
    private TextView tvTossResult;
    private MaterialButton btnStartScoring, btnFlip;
    private String tossWinner;
    private String battingTeam;
    private boolean isFlipping = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toss);

        teamA = getIntent().getStringExtra("teamA");
        teamB = getIntent().getStringExtra("teamB");
        playerCount = getIntent().getIntExtra("playerCount", 11);
        totalOvers = getIntent().getIntExtra("totalOvers", 5);
        playersA = getIntent().getStringExtra("playersA");
        playersB = getIntent().getStringExtra("playersB");
        
        tvTossResult = findViewById(R.id.tvTossResult);
        btnStartScoring = findViewById(R.id.btnStartScoring);
        btnFlip = findViewById(R.id.btnFlip);

        btnFlip.setOnClickListener(v -> {
            if (isFlipping) return;
            startCoinFlip();
        });

        btnStartScoring.setOnClickListener(v -> {
            if (battingTeam == null) {
                Toast.makeText(this, "Please decide Bat/Bowl first!", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(TossActivity.this, ScoringActivity.class);
            
            if (battingTeam.equals(teamB)) {
                intent.putExtra("teamA", teamB);
                intent.putExtra("teamB", teamA);
                intent.putExtra("playersA", playersB);
                intent.putExtra("playersB", playersA);
            } else {
                intent.putExtra("teamA", teamA);
                intent.putExtra("teamB", teamB);
                intent.putExtra("playersA", playersA);
                intent.putExtra("playersB", playersB);
            }
            
            intent.putExtra("totalOvers", totalOvers);
            intent.putExtra("maxWicketsA", playerCount - 1);
            intent.putExtra("maxWicketsB", playerCount - 1);
            startActivity(intent);
            finish();
        });
    }

    private void startCoinFlip() {
        isFlipping = true;
        btnFlip.setEnabled(false);
        findViewById(R.id.cvTossResult).setVisibility(View.GONE);
        btnStartScoring.setVisibility(View.GONE);

        Handler handler = new Handler(Looper.getMainLooper());
        final int flipCount = 25; 
        final long flipDelay = 120; 

        for (int i = 0; i < flipCount; i++) {
            final int index = i;
            handler.postDelayed(() -> {
                btnFlip.setText(index % 2 == 0 ? "HEADS" : "TAILS");
                btnFlip.animate()
                        .rotationYBy(180)
                        .setDuration(flipDelay)
                        .setInterpolator(new LinearInterpolator())
                        .start();
            }, i * flipDelay);
        }

        // Final land animation
        handler.postDelayed(this::finalizeToss, flipCount * flipDelay);
    }

    private void finalizeToss() {
        Random random = new Random();
        boolean heads = random.nextBoolean();
        tossWinner = heads ? teamA : teamB;
        
        // Final "landing" flip to bring it to a flat stop
        btnFlip.animate()
                .rotationYBy(180)
                .setDuration(500)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    btnFlip.setText(heads ? "HEADS" : "TAILS");
                    btnFlip.setRotationY(0); // Ensure perfectly flat (0 degrees)
                    btnFlip.setEnabled(true);
                    isFlipping = false;
                    
                    new Handler(Looper.getMainLooper()).postDelayed(this::showTossDialog, 600);
                })
                .start();
    }

    private void showTossDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Toss Winner!")
                .setMessage(tossWinner + " won the toss! What would you like to do?")
                .setCancelable(false)
                .setPositiveButton("Bat First", (dialog, which) -> {
                    battingTeam = tossWinner;
                    updateTossResultUI("Bat");
                })
                .setNegativeButton("Bowl First", (dialog, which) -> {
                    battingTeam = tossWinner.equals(teamA) ? teamB : teamA;
                    updateTossResultUI("Bowl");
                })
                .show();
    }

    private void updateTossResultUI(String choice) {
        String choiceDisplay = choice.equals("Bat") ? "BAT" : "BOWL";
        String decisionText = tossWinner + " won the toss and chose to " + choiceDisplay + " first.";
        
        tvTossResult.setText(decisionText);
        findViewById(R.id.cvTossResult).setVisibility(View.VISIBLE);
        btnStartScoring.setVisibility(View.VISIBLE);
        
        // Reset coin to original flat state with "FLIP COIN" text
        btnFlip.setText("FLIP COIN");
        btnFlip.setRotationY(0);
    }
}
