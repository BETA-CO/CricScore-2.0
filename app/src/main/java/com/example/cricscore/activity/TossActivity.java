package com.example.cricscore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
    private MaterialButton btnStartScoring;
    private String tossWinner;
    private String battingTeam;

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

        findViewById(R.id.btnFlip).setOnClickListener(v -> {
            Random random = new Random();
            tossWinner = random.nextBoolean() ? teamA : teamB;
            
            showTossDialog();
        });

        btnStartScoring.setOnClickListener(v -> {
            if (battingTeam == null) {
                Toast.makeText(this, "Please decide Bat/Bowl first!", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(TossActivity.this, ScoringActivity.class);
            
            // Re-order teams if Team B is batting first
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
        String bowlingTeam = tossWinner.equals(teamA) ? teamB : teamA;
        String decisionText = tossWinner + " won the toss and chose to " + choice + " first.";
        
        tvTossResult.setText(decisionText);
        findViewById(R.id.cvTossResult).setVisibility(View.VISIBLE);
        btnStartScoring.setVisibility(View.VISIBLE);
    }
}
