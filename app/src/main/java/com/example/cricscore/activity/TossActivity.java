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

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TossActivity extends AppCompatActivity {

    private String teamA, teamB, commonPlayer, playersA, playersB;
    private int playerCount, totalOvers;
    private boolean enableDotOptions;
    private TextView tvTossResult;
    private MaterialButton btnStartScoring, btnFlip, btnEditTeams;
    private String tossWinner;
    private String battingTeam;
    private boolean isFlipping = false;
    private final SecureRandom secureRandom = new SecureRandom();

    private String headsTeam = null;
    private String tailsTeam = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toss);

        teamA = getIntent().getStringExtra("teamA");
        teamB = getIntent().getStringExtra("teamB");
        commonPlayer = getIntent().getStringExtra("commonPlayer");
        playerCount = getIntent().getIntExtra("playerCount", 11);
        totalOvers = getIntent().getIntExtra("totalOvers", 5);
        enableDotOptions = getIntent().getBooleanExtra("enableDotOptions", true);
        playersA = getIntent().getStringExtra("playersA");
        playersB = getIntent().getStringExtra("playersB");
        
        tvTossResult = findViewById(R.id.tvTossResult);
        btnStartScoring = findViewById(R.id.btnStartScoring);
        btnFlip = findViewById(R.id.btnFlip);
        btnEditTeams = findViewById(R.id.btnEditTeams);

        btnFlip.setOnClickListener(v -> {
            if (isFlipping) return;
            if (headsTeam == null) {
                showHeadsSelectionDialog();
            } else {
                startCoinFlip();
            }
        });

        btnEditTeams.setOnClickListener(v -> {
            // Take user back to Team A setup page
            Intent intent = new Intent(TossActivity.this, PlayersSetupActivity.class);
            intent.putExtra("teamA", teamA);
            intent.putExtra("teamB", teamB);
            intent.putExtra("commonPlayer", commonPlayer);
            intent.putExtra("playerCount", playerCount - (commonPlayer != null && !commonPlayer.isEmpty() ? 1 : 0));
            intent.putExtra("totalOvers", totalOvers);
            intent.putExtra("enableDotOptions", enableDotOptions);
            intent.putExtra("setupForTeamA", true);
            startActivity(intent);
            finish();
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
            
            intent.putExtra("commonPlayer", commonPlayer);
            intent.putExtra("totalOvers", totalOvers);
            intent.putExtra("enableDotOptions", enableDotOptions);
            intent.putExtra("maxWicketsA", playerCount - 1);
            intent.putExtra("maxWicketsB", playerCount - 1);
            startActivity(intent);
            finish();
        });
    }

    private void showHeadsSelectionDialog() {
        String[] teams = {teamA, teamB};
        new AlertDialog.Builder(this)
                .setTitle("Select team for HEADS")
                .setItems(teams, (dialog, which) -> {
                    headsTeam = teams[which];
                    tailsTeam = (which == 0) ? teamB : teamA;
                    Toast.makeText(this, headsTeam + " is HEADS, " + tailsTeam + " is TAILS", Toast.LENGTH_SHORT).show();
                    startCoinFlip();
                })
                .setCancelable(false)
                .show();
    }

    private void startCoinFlip() {
        isFlipping = true;
        btnFlip.setEnabled(false);
        btnEditTeams.setVisibility(View.GONE); // Hide edit button when flip starts
        findViewById(R.id.cvTossResult).setVisibility(View.GONE);
        btnStartScoring.setVisibility(View.GONE);

        // Random selection of result
        final boolean isResultHeads = secureRandom.nextBoolean();
        tossWinner = isResultHeads ? headsTeam : tailsTeam;

        Handler handler = new Handler(Looper.getMainLooper());
        final int flipCount = 35; 
        final long flipDelay = 110; 

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

        handler.postDelayed(() -> finalizeToss(isResultHeads), flipCount * flipDelay);
    }

    private void finalizeToss(boolean isResultHeads) {
        btnFlip.animate()
                .rotationYBy(180)
                .setDuration(600)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    btnFlip.setText(isResultHeads ? "HEADS" : "TAILS");
                    btnFlip.setRotationY(0); 
                    btnFlip.setEnabled(true);
                    isFlipping = false;
                    
                    new Handler(Looper.getMainLooper()).postDelayed(this::showTossDialog, 700);
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
        btnEditTeams.setVisibility(View.GONE); // Hide edit button after toss is completed
        
        btnFlip.setText("FLIP COIN");
        btnFlip.setRotationY(0);
        // Reset for next time if needed, though activity finishes
        headsTeam = null;
        tailsTeam = null;
    }
}
