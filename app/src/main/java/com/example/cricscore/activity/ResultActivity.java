package com.example.cricscore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cricscore.MainActivity;
import com.example.cricscore.R;

import java.util.ArrayList;
import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        String winner = getIntent().getStringExtra("winner");
        String teamA = getIntent().getStringExtra("teamA");
        String teamB = getIntent().getStringExtra("teamB");
        int scoreA = getIntent().getIntExtra("scoreA", 0);
        int wicketsA = getIntent().getIntExtra("wicketsA", 0);
        double oversA = getIntent().getDoubleExtra("oversA", 0.0);
        int scoreB = getIntent().getIntExtra("scoreB", 0);
        int wicketsB = getIntent().getIntExtra("wicketsB", 0);
        double oversB = getIntent().getDoubleExtra("oversB", 0.0);

        ArrayList<ScoringActivity.PlayerStats> statsA = (ArrayList<ScoringActivity.PlayerStats>) getIntent().getSerializableExtra("statsA");
        ArrayList<ScoringActivity.PlayerStats> statsB = (ArrayList<ScoringActivity.PlayerStats>) getIntent().getSerializableExtra("statsB");

        TextView tvWinner = findViewById(R.id.tvWinner);
        TextView tvTeam1Header = findViewById(R.id.tvTeam1Header);
        TextView tvTeam1Result = findViewById(R.id.tvTeam1Result);
        TextView tvTeam2Header = findViewById(R.id.tvTeam2Header);
        TextView tvTeam2Result = findViewById(R.id.tvTeam2Result);

        LinearLayout llTeam1Stats = findViewById(R.id.llTeam1Stats);
        LinearLayout llTeam2Stats = findViewById(R.id.llTeam2Stats);

        if ("Draw".equals(winner)) {
            tvWinner.setText("It's a Draw!");
        } else {
            tvWinner.setText(winner + " Won!");
        }

        tvTeam1Header.setText(teamA + " Scorecard");
        tvTeam1Result.setText(scoreA + "/" + wicketsA + " (" + oversA + ")");
        tvTeam2Header.setText(teamB + " Scorecard");
        tvTeam2Result.setText(scoreB + "/" + wicketsB + " (" + oversB + ")");

        populateStats(llTeam1Stats, statsA);
        populateStats(llTeam2Stats, statsB);

        findViewById(R.id.btnHome).setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void populateStats(LinearLayout container, ArrayList<ScoringActivity.PlayerStats> statsList) {
        if (statsList == null) return;
        
        LayoutInflater inflater = LayoutInflater.from(this);
        for (ScoringActivity.PlayerStats stat : statsList) {
            View view = inflater.inflate(R.layout.item_player_stat, container, false);
            
            TextView tvName = view.findViewById(R.id.tvStatName);
            TextView tvRuns = view.findViewById(R.id.tvStatRuns);
            TextView tvSR = view.findViewById(R.id.tvStatSR);
            TextView tvDismissal = view.findViewById(R.id.tvStatDismissal);

            tvName.setText(stat.name);
            tvRuns.setText(stat.runs + " (" + stat.balls + ")");
            
            double sr = (stat.balls == 0) ? 0.0 : (stat.runs * 100.0) / stat.balls;
            tvSR.setText(String.format(Locale.US, "SR: %.1f", sr));
            
            tvDismissal.setText(stat.dismissal);
            
            container.addView(view);
        }
    }
}
