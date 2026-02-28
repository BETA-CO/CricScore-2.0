package com.example.cricscore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cricscore.R;
import com.example.cricscore.dataBase.AppDatabase;
import com.example.cricscore.dataTypes.Match;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MatchHistoryActivity extends AppCompatActivity implements MatchAdapter.OnMatchClickListener {

    private RecyclerView rvHistory;
    private MatchAdapter adapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_history);

        rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        
        db = AppDatabase.getDatabase(this);
        
        loadMatches();
    }

    private void loadMatches() {
        new Thread(() -> {
            List<Match> matches = db.matchDao().getAllMatches();
            runOnUiThread(() -> {
                adapter = new MatchAdapter(matches, this);
                rvHistory.setAdapter(adapter);
            });
        }).start();
    }

    @Override
    public void onMatchClick(Match match) {
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<ScoringActivity.PlayerStats>>() {}.getType();
        
        ArrayList<ScoringActivity.PlayerStats> statsA = gson.fromJson(match.statsAJson, listType);
        ArrayList<ScoringActivity.PlayerStats> statsB = gson.fromJson(match.statsBJson, listType);

        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("winner", match.winner);
        intent.putExtra("teamA", match.teamA);
        intent.putExtra("teamB", match.teamB);
        intent.putExtra("scoreA", match.scoreA);
        intent.putExtra("wicketsA", match.wicketsA);
        intent.putExtra("oversA", match.oversA);
        intent.putExtra("scoreB", match.scoreB);
        intent.putExtra("wicketsB", match.wicketsB);
        intent.putExtra("oversB", match.oversB);
        intent.putExtra("statsA", statsA);
        intent.putExtra("statsB", statsB);

        if (match.hasSuperOver) {
            ArrayList<ScoringActivity.PlayerStats> superStatsA = gson.fromJson(match.superStatsAJson, listType);
            ArrayList<ScoringActivity.PlayerStats> superStatsB = gson.fromJson(match.superStatsBJson, listType);
            intent.putExtra("hasSuperOver", true);
            intent.putExtra("superScoreA", match.superScoreA);
            intent.putExtra("superWicketsA", match.superWicketsA);
            intent.putExtra("superScoreB", match.superScoreB);
            intent.putExtra("superWicketsB", match.superWicketsB);
            intent.putExtra("superStatsA", superStatsA);
            intent.putExtra("superStatsB", superStatsB);
        }

        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Match match) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Match")
                .setMessage("Are you sure you want to delete this match history?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    new Thread(() -> {
                        db.matchDao().delete(match);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Match deleted", Toast.LENGTH_SHORT).show();
                            loadMatches();
                        });
                    }).start();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
