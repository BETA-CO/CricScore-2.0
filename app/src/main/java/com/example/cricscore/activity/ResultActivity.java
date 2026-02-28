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
import com.google.android.material.card.MaterialCardView;

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

        boolean hasSuperOver = getIntent().getBooleanExtra("hasSuperOver", false);
        int superScoreA = getIntent().getIntExtra("superScoreA", 0);
        int superWicketsA = getIntent().getIntExtra("superWicketsA", 0);
        int superScoreB = getIntent().getIntExtra("superScoreB", 0);
        int superWicketsB = getIntent().getIntExtra("superWicketsB", 0);

        ArrayList<ScoringActivity.PlayerStats> statsA = (ArrayList<ScoringActivity.PlayerStats>) getIntent().getSerializableExtra("statsA");
        ArrayList<ScoringActivity.PlayerStats> statsB = (ArrayList<ScoringActivity.PlayerStats>) getIntent().getSerializableExtra("statsB");
        ArrayList<ScoringActivity.PlayerStats> superStatsA = (ArrayList<ScoringActivity.PlayerStats>) getIntent().getSerializableExtra("superStatsA");
        ArrayList<ScoringActivity.PlayerStats> superStatsB = (ArrayList<ScoringActivity.PlayerStats>) getIntent().getSerializableExtra("superStatsB");

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

        // Handle Super Over Section
        View llSuperOverSection = findViewById(R.id.llSuperOverSection);
        if (hasSuperOver) {
            llSuperOverSection.setVisibility(View.VISIBLE);
            
            TextView tvSuperTeam1Result = findViewById(R.id.tvSuperTeam1Result);
            TextView tvSuperTeam2Result = findViewById(R.id.tvSuperTeam2Result);
            LinearLayout llSuperTeam1Stats = findViewById(R.id.llSuperTeam1Stats);
            LinearLayout llSuperTeam2Stats = findViewById(R.id.llSuperTeam2Stats);

            tvSuperTeam1Result.setText(teamA + ": " + superScoreA + "/" + superWicketsA);
            tvSuperTeam2Result.setText(teamB + ": " + superScoreB + "/" + superWicketsB);

            populateStats(llSuperTeam1Stats, superStatsA);
            populateStats(llSuperTeam2Stats, superStatsB);
        } else {
            llSuperOverSection.setVisibility(View.GONE);
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
        for (ScoringActivity.PlayerStats ps : statsList) {
            // Only show players who were involved in this specific part of the game
            if (ps.balls == 0 && ps.bowlingBalls == 0 && ps.catches == 0 && ps.stumpings == 0 && ps.runOuts == 0 && "DNB".equals(ps.dismissal)) {
                continue;
            }

            View view = inflater.inflate(R.layout.item_player_stat, container, false);
            
            ((TextView) view.findViewById(R.id.tvStatName)).setText(ps.name);
            ((TextView) view.findViewById(R.id.tvStatDismissal)).setText(ps.dismissal);

            // Batting
            ((TextView) view.findViewById(R.id.tvStatRuns)).setText(String.valueOf(ps.runs));
            ((TextView) view.findViewById(R.id.tvStatBalls)).setText(String.valueOf(ps.balls));
            ((TextView) view.findViewById(R.id.tvStat4s)).setText(String.valueOf(ps.fours));
            ((TextView) view.findViewById(R.id.tvStat6s)).setText(String.valueOf(ps.sixes));
            
            double sr = (ps.balls == 0) ? 0.0 : (ps.runs * 100.0) / ps.balls;
            ((TextView) view.findViewById(R.id.tvStatSR)).setText(String.format(Locale.US, "Strike Rate: %.1f", sr));

            // Bowling
            if (ps.bowlingBalls > 0) {
                view.findViewById(R.id.labelBowling).setVisibility(View.VISIBLE);
                view.findViewById(R.id.gridBowling).setVisibility(View.VISIBLE);
                view.findViewById(R.id.tvStatEcon).setVisibility(View.VISIBLE);

                String oversStr = (ps.bowlingBalls / 6) + "." + (ps.bowlingBalls % 6);
                ((TextView) view.findViewById(R.id.tvStatOvers)).setText(oversStr);
                ((TextView) view.findViewById(R.id.tvStatMaidens)).setText(String.valueOf(ps.maidens));
                ((TextView) view.findViewById(R.id.tvStatBowlRuns)).setText(String.valueOf(ps.bowlingRuns));
                ((TextView) view.findViewById(R.id.tvStatWickets)).setText(String.valueOf(ps.wickets));

                double econ = (ps.bowlingBalls == 0) ? 0.0 : (ps.bowlingRuns * 6.0) / ps.bowlingBalls;
                ((TextView) view.findViewById(R.id.tvStatEcon)).setText(String.format(Locale.US, "Economy: %.2f", econ));
            } else {
                view.findViewById(R.id.labelBowling).setVisibility(View.GONE);
                view.findViewById(R.id.gridBowling).setVisibility(View.GONE);
                view.findViewById(R.id.tvStatEcon).setVisibility(View.GONE);
            }

            // Fielding
            if (ps.catches > 0 || ps.stumpings > 0 || ps.runOuts > 0) {
                view.findViewById(R.id.labelFielding).setVisibility(View.VISIBLE);
                view.findViewById(R.id.gridFielding).setVisibility(View.VISIBLE);

                ((TextView) view.findViewById(R.id.tvStatCatches)).setText(String.valueOf(ps.catches));
                ((TextView) view.findViewById(R.id.tvStatStumpings)).setText(String.valueOf(ps.stumpings));
                ((TextView) view.findViewById(R.id.tvStatRunOuts)).setText(String.valueOf(ps.runOuts));
            } else {
                view.findViewById(R.id.labelFielding).setVisibility(View.GONE);
                view.findViewById(R.id.gridFielding).setVisibility(View.GONE);
            }
            
            container.addView(view);
        }
    }
}
