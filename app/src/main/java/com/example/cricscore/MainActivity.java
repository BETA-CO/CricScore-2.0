package com.example.cricscore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cricscore.activity.MatchHistoryActivity;
import com.example.cricscore.activity.MatchSetupActivity;
import com.example.cricscore.activity.RulesActivity;
import com.example.cricscore.dataBase.AppDatabase;
import com.example.cricscore.dataTypes.Match;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnStartMatch).setOnClickListener(v -> {
            startActivity(
                    new Intent(MainActivity.this, MatchSetupActivity.class)
            );
        });

        findViewById(R.id.btnHistory).setOnClickListener(v -> {
            startActivity(
                    new Intent(MainActivity.this, MatchHistoryActivity.class)
            );
        });

        findViewById(R.id.btnRules).setOnClickListener(v -> {
            startActivity(
                    new Intent(MainActivity.this, RulesActivity.class)
            );
        });

        updateLastMatchSummary();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLastMatchSummary();
    }

    private void updateLastMatchSummary() {
        MaterialCardView cvLastMatch = findViewById(R.id.cvLastMatch);
        TextView tvTeams = findViewById(R.id.tvLastMatchTeams);
        TextView tvWinner = findViewById(R.id.tvLastMatchWinner);

        new Thread(() -> {
            List<Match> matches = AppDatabase.getDatabase(this).matchDao().getAllMatches();
            if (matches != null && !matches.isEmpty()) {
                Match last = matches.get(0); // Most recent match
                runOnUiThread(() -> {
                    tvTeams.setText(last.teamA + " vs " + last.teamB);
                    tvWinner.setText("Winner: " + last.winner);
                    cvLastMatch.setVisibility(View.VISIBLE);
                });
            } else {
                runOnUiThread(() -> cvLastMatch.setVisibility(View.GONE));
            }
        }).start();
    }
}
