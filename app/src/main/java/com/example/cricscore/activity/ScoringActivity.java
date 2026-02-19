package com.example.cricscore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cricscore.R;
import com.example.cricscore.dataBase.AppDatabase;
import com.example.cricscore.dataTypes.Match;
import com.google.gson.Gson;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ScoringActivity extends AppCompatActivity {

    private String teamA, teamB;
    private List<String> playersA = new ArrayList<>(), playersB = new ArrayList<>();
    private int totalOvers;
    private int maxWicketsA, maxWicketsB;
    
    private int scoreA = 0, wicketsA = 0, ballsA = 0;
    private int scoreB = 0, wicketsB = 0, ballsB = 0;
    private boolean isInnings1 = true;

    private String currentStriker = "Select";
    private String currentNonStriker = "Select";
    private String currentBowler = "Select";

    private Map<String, Integer> batsmanRuns = new HashMap<>();
    private Map<String, Integer> playerBallsPlayed = new HashMap<>();
    private Map<String, String> playerDismissal = new HashMap<>(); 

    private Map<String, Integer> bowlerRunsMap = new HashMap<>();
    private Map<String, Integer> bowlerWicketsMap = new HashMap<>();
    private List<String> currentOverHistory = new ArrayList<>();
    
    private Set<String> outPlayers = new HashSet<>();

    private TextView tvCurrentInnings, tvScore, tvOvers, tvRunRate, tvStriker, tvNonStriker, tvBowler, tvThisOver;
    private AppDatabase db;

    public static class PlayerStats implements Serializable {
        public String name;
        public int runs;
        public int balls;
        public String dismissal; 
        public boolean isOut;

        public PlayerStats(String name, int runs, int balls, String dismissal, boolean isOut) {
            this.name = name;
            this.runs = runs;
            this.balls = balls;
            this.dismissal = dismissal;
            this.isOut = isOut;
        }
    }

    private static class ScoreState {
        final int sA, wA, bA, sB, wB, bB;
        final boolean inn1;
        final String striker, nonStriker, bowler;
        final Map<String, Integer> bRuns;
        final Map<String, Integer> boRuns;
        final Map<String, Integer> boWickets;
        final List<String> overHistory;
        final Set<String> outPlayers;
        final Map<String, Integer> ballsPlayed;
        final Map<String, String> dismissals;

        ScoreState(int sA, int wA, int bA, int sB, int wB, int bB, boolean inn1, 
                   String striker, String nonStriker, String bowler,
                   Map<String, Integer> bRuns, Map<String, Integer> boRuns, Map<String, Integer> boWickets,
                   List<String> overHistory, Set<String> outPlayers, Map<String, Integer> ballsPlayed,
                   Map<String, String> dismissals) {
            this.sA = sA; this.wA = wA; this.bA = bA;
            this.sB = sB; this.wB = wB; this.bB = bB;
            this.inn1 = inn1;
            this.striker = striker;
            this.nonStriker = nonStriker;
            this.bowler = bowler;
            this.bRuns = new HashMap<>(bRuns);
            this.boRuns = new HashMap<>(boRuns);
            this.boWickets = new HashMap<>(boWickets);
            this.overHistory = new ArrayList<>(overHistory);
            this.outPlayers = new HashSet<>(outPlayers);
            this.ballsPlayed = new HashMap<>(ballsPlayed);
            this.dismissals = new HashMap<>(dismissals);
        }
    }
    private final List<ScoreState> history = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoring);

        teamA = getIntent().getStringExtra("teamA");
        teamB = getIntent().getStringExtra("teamB");
        String pA = getIntent().getStringExtra("playersA");
        String pB = getIntent().getStringExtra("playersB");
        totalOvers = getIntent().getIntExtra("totalOvers", 5);
        maxWicketsA = getIntent().getIntExtra("maxWicketsA", 10);
        maxWicketsB = getIntent().getIntExtra("maxWicketsB", 10);

        playersA = parsePlayers(pA, teamA, maxWicketsA + 1);
        playersB = parsePlayers(pB, teamB, maxWicketsB + 1);

        db = AppDatabase.getDatabase(this);

        tvCurrentInnings = findViewById(R.id.tvCurrentInnings);
        tvScore = findViewById(R.id.tvScore);
        tvOvers = findViewById(R.id.tvOvers);
        tvRunRate = findViewById(R.id.tvRunRate);
        tvStriker = findViewById(R.id.tvStriker);
        tvNonStriker = findViewById(R.id.tvNonStriker);
        tvBowler = findViewById(R.id.tvBowler);
        tvThisOver = findViewById(R.id.tvThisOver);

        tvStriker.setOnClickListener(v -> showPlayerPicker(0));
        tvNonStriker.setOnClickListener(v -> showPlayerPicker(1));
        tvBowler.setOnClickListener(v -> showPlayerPicker(2));

        updateUI();
        setupButtons();

        findViewById(R.id.btnFinishInnings).setOnClickListener(v -> {
            if (isInnings1) {
                new AlertDialog.Builder(this)
                        .setTitle("Finish Innings?")
                        .setMessage("Are you sure you want to end the first innings?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            saveState();
                            startSecondInnings();
                        })
                        .setNegativeButton("No", null)
                        .show();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Finish Match?")
                        .setMessage("Are you sure you want to end the match?")
                        .setPositiveButton("Yes", (dialog, which) -> saveMatch())
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        findViewById(R.id.btnUndo).setOnClickListener(v -> undo());
    }

    private String getPlayerKey(String playerName, boolean isTeamA) {
        return (isTeamA ? "A:" : "B:") + playerName;
    }

    private void startSecondInnings() {
        isInnings1 = false;
        currentStriker = "Select";
        currentNonStriker = "Select";
        currentBowler = "Select";
        currentOverHistory.clear();
        outPlayers.clear();
        updateUI();
        Toast.makeText(this, "Target: " + (scoreA + 1), Toast.LENGTH_LONG).show();
    }

    private void saveState() {
        history.add(new ScoreState(scoreA, wicketsA, ballsA, scoreB, wicketsB, ballsB, isInnings1, 
                currentStriker, currentNonStriker, currentBowler, batsmanRuns, bowlerRunsMap, bowlerWicketsMap, 
                currentOverHistory, outPlayers, playerBallsPlayed, playerDismissal));
        if (history.size() > 50) history.remove(0);
    }

    private void undo() {
        if (!history.isEmpty()) {
            ScoreState last = history.remove(history.size() - 1);
            scoreA = last.sA; wicketsA = last.wA; ballsA = last.bA;
            scoreB = last.sB; wicketsB = last.wB; ballsB = last.bB;
            isInnings1 = last.inn1;
            currentStriker = last.striker;
            currentNonStriker = last.nonStriker;
            currentBowler = last.bowler;
            batsmanRuns = new HashMap<>(last.bRuns);
            bowlerRunsMap = new HashMap<>(last.boRuns);
            bowlerWicketsMap = new HashMap<>(last.boWickets);
            currentOverHistory = new ArrayList<>(last.overHistory);
            outPlayers = new HashSet<>(last.outPlayers);
            playerBallsPlayed = new HashMap<>(last.ballsPlayed);
            playerDismissal = new HashMap<>(last.dismissals);
            updateUI();
        } else {
            Toast.makeText(this, "Nothing to undo", Toast.LENGTH_SHORT).show();
        }
    }

    private List<String> parsePlayers(String pStr, String teamName, int count) {
        List<String> result = new ArrayList<>();
        if (pStr != null && !pStr.trim().isEmpty()) {
            String[] names = pStr.split(",");
            for (String name : names) {
                if (!name.trim().isEmpty()) {
                    result.add(name.trim());
                }
            }
        }
        while (result.size() < count) {
            result.add(teamName + " Player " + (result.size() + 1));
        }
        return result;
    }

    private void showPlayerPicker(int type) {
        List<String> pool;
        boolean isPoolTeamA;
        if (isInnings1) {
            pool = (type == 2) ? playersB : playersA;
            isPoolTeamA = (type != 2);
        } else {
            pool = (type == 2) ? playersA : playersB;
            isPoolTeamA = (type == 2);
        }
        
        List<String> available = new ArrayList<>();
        List<String> displayNames = new ArrayList<>();
        
        for (String p : pool) {
            String pKey = getPlayerKey(p, isPoolTeamA);
            if (type == 2) {
                available.add(p);
                int runs = bowlerRunsMap.getOrDefault(pKey, 0);
                int wkts = bowlerWicketsMap.getOrDefault(pKey, 0);
                displayNames.add(p + " (" + wkts + "-" + runs + ")");
            } else {
                if (!outPlayers.contains(pKey) && !p.equals(currentStriker) && !p.equals(currentNonStriker)) {
                    available.add(p);
                    displayNames.add(p);
                }
            }
        }

        if (available.isEmpty()) {
            Toast.makeText(this, "No players available", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] displayArray = displayNames.toArray(new String[0]);
        String title = (type == 0) ? "Select Striker" : (type == 1) ? "Select Non-Striker" : "Select Bowler";

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(displayArray, (dialog, which) -> {
                    saveState();
                    String selected = available.get(which);
                    if (type == 0) currentStriker = selected;
                    else if (type == 1) currentNonStriker = selected;
                    else {
                        currentBowler = selected;
                    }
                    updateUI();
                })
                .show();
    }

    private void setupButtons() {
        findViewById(R.id.btn0).setOnClickListener(v -> addRuns(0));
        findViewById(R.id.btn1).setOnClickListener(v -> addRuns(1));
        findViewById(R.id.btn2).setOnClickListener(v -> addRuns(2));
        findViewById(R.id.btn3).setOnClickListener(v -> addRuns(3));
        findViewById(R.id.btn4).setOnClickListener(v -> addRuns(4));
        findViewById(R.id.btn6).setOnClickListener(v -> addRuns(6));
        findViewById(R.id.btnWicket).setOnClickListener(v -> handleWicketClick());
        findViewById(R.id.btnWide).setOnClickListener(v -> addExtra("WD", 1));
        findViewById(R.id.btnNoBall).setOnClickListener(v -> addExtra("NB", 1));
    }

    private void handleWicketClick() {
        if (!checkPlayersSelected()) return;
        
        String[] options = {"Striker: " + currentStriker, "Non-Striker: " + currentNonStriker};
        new AlertDialog.Builder(this)
                .setTitle("Who got out?")
                .setItems(options, (dialog, which) -> {
                    String playerOut = (which == 0) ? currentStriker : currentNonStriker;
                    showDismissalTypeDialog(playerOut, which == 0);
                })
                .show();
    }

    private void showDismissalTypeDialog(String playerName, boolean isStrikerOut) {
        String[] types = {"Bowled", "Caught", "LBW", "Run Out", "Stumped", "Hit Wicket"};
        new AlertDialog.Builder(this)
                .setTitle("How was " + playerName + " out?")
                .setItems(types, (dialog, which) -> {
                    String howOut = types[which];
                    if (!howOut.equals("Run Out")) {
                        howOut += " b " + currentBowler;
                    }
                    processWicket(playerName, isStrikerOut, howOut);
                })
                .show();
    }

    private void processWicket(String playerOutName, boolean isStrikerOut, String dismissalDesc) {
        saveState();
        String pKey = getPlayerKey(playerOutName, isInnings1);
        String boKey = getPlayerKey(currentBowler, !isInnings1);

        if (!dismissalDesc.contains("Run Out")) {
            bowlerWicketsMap.put(boKey, bowlerWicketsMap.getOrDefault(boKey, 0) + 1);
        }
        
        outPlayers.add(pKey);
        playerDismissal.put(pKey, dismissalDesc);
        playerBallsPlayed.put(pKey, playerBallsPlayed.getOrDefault(pKey, 0) + 1);
        
        currentOverHistory.add("W");

        if (isInnings1) {
            wicketsA++;
            ballsA++;
            if (isStrikerOut) currentStriker = "Select";
            else currentNonStriker = "Select";
            checkOverEnd(ballsA);
            checkInningsEnd();
        } else {
            wicketsB++;
            ballsB++;
            if (isStrikerOut) currentStriker = "Select";
            else currentNonStriker = "Select";
            checkOverEnd(ballsB);
            checkMatchEnd();
        }
        
        updateUI();
        
        int currentWickets = isInnings1 ? wicketsA : wicketsB;
        int maxWickets = isInnings1 ? maxWicketsA : maxWicketsB;
        if (currentWickets < maxWickets) {
            showPlayerPicker(isStrikerOut ? 0 : 1);
        }
    }

    private boolean checkPlayersSelected() {
        if (currentStriker.equals("Select") || currentNonStriker.equals("Select") || currentBowler.equals("Select")) {
            Toast.makeText(this, "Select players first!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void addRuns(int runs) {
        if (!checkPlayersSelected()) return;
        saveState();
        
        String bKey = getPlayerKey(currentStriker, isInnings1);
        String boKey = getPlayerKey(currentBowler, !isInnings1);

        batsmanRuns.put(bKey, batsmanRuns.getOrDefault(bKey, 0) + runs);
        playerBallsPlayed.put(bKey, playerBallsPlayed.getOrDefault(bKey, 0) + 1);
        bowlerRunsMap.put(boKey, bowlerRunsMap.getOrDefault(boKey, 0) + runs);
        
        currentOverHistory.add(String.valueOf(runs));

        if (isInnings1) {
            scoreA += runs;
            ballsA++;
            if (runs % 2 != 0) rotateStrike();
            checkOverEnd(ballsA);
            checkInningsEnd();
        } else {
            scoreB += runs;
            ballsB++;
            if (runs % 2 != 0) rotateStrike();
            checkOverEnd(ballsB);
            checkMatchEnd();
        }
        updateUI();
    }

    private void addExtra(String type, int runs) {
        if (!checkPlayersSelected()) return;
        saveState();
        
        String boKey = getPlayerKey(currentBowler, !isInnings1);
        bowlerRunsMap.put(boKey, bowlerRunsMap.getOrDefault(boKey, 0) + runs);
        currentOverHistory.add(type);

        if (isInnings1) {
            scoreA += runs;
        } else {
            scoreB += runs;
            checkMatchEnd();
        }
        updateUI();
    }

    private void rotateStrike() {
        String temp = currentStriker;
        currentStriker = currentNonStriker;
        currentNonStriker = temp;
    }

    private void checkOverEnd(int totalBalls) {
        if (totalBalls > 0 && totalBalls % 6 == 0) {
            rotateStrike();
            currentBowler = "Select";
            currentOverHistory.clear(); 
            Toast.makeText(this, "Over Complete! New bowler.", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkInningsEnd() {
        if (wicketsA >= maxWicketsA || ballsA >= totalOvers * 6) {
            startSecondInnings();
        }
    }

    private void checkMatchEnd() {
        if (scoreB > scoreA) {
            saveMatch();
        } else if (wicketsB >= maxWicketsB || ballsB >= totalOvers * 6) {
            saveMatch();
        }
    }

    private void updateUI() {
        if (isInnings1) {
            tvCurrentInnings.setText(teamA + " Batting");
            tvScore.setText(scoreA + "/" + wicketsA);
            tvOvers.setText("Overs: " + getOvers(ballsA) + " / " + totalOvers);
            tvRunRate.setText("CRR: " + String.format(Locale.US, "%.2f", getRunRate(scoreA, ballsA)));
        } else {
            tvCurrentInnings.setText(teamB + " Batting (Target: " + (scoreA + 1) + ")");
            tvScore.setText(scoreB + "/" + wicketsB);
            tvOvers.setText("Overs: " + getOvers(ballsB) + " / " + totalOvers);
            tvRunRate.setText("CRR: " + String.format(Locale.US, "%.2f", getRunRate(scoreB, ballsB)));
        }

        String sKey = getPlayerKey(currentStriker, isInnings1);
        String nsKey = getPlayerKey(currentNonStriker, isInnings1);
        String boKey = getPlayerKey(currentBowler, !isInnings1);

        tvStriker.setText("Striker: " + currentStriker + (currentStriker.equals("Select") ? "" : " (" + batsmanRuns.getOrDefault(sKey, 0) + ")"));
        tvNonStriker.setText("Non-Striker: " + currentNonStriker + (currentNonStriker.equals("Select") ? "" : " (" + batsmanRuns.getOrDefault(nsKey, 0) + ")"));
        tvBowler.setText("Bowler: " + currentBowler + (currentBowler.equals("Select") ? "" : " (" + bowlerWicketsMap.getOrDefault(boKey, 0) + "-" + bowlerRunsMap.getOrDefault(boKey, 0) + ")"));

        StringBuilder overStr = new StringBuilder("This Over: ");
        for (String ball : currentOverHistory) overStr.append(ball).append(" ");
        tvThisOver.setText(overStr.toString());
        
        boolean enabled = !currentStriker.equals("Select") && !currentNonStriker.equals("Select") && !currentBowler.equals("Select");
        findViewById(R.id.btn0).setEnabled(enabled);
        findViewById(R.id.btn1).setEnabled(enabled);
        findViewById(R.id.btn2).setEnabled(enabled);
        findViewById(R.id.btn3).setEnabled(enabled);
        findViewById(R.id.btn4).setEnabled(enabled);
        findViewById(R.id.btn6).setEnabled(enabled);
        findViewById(R.id.btnWicket).setEnabled(enabled);
        findViewById(R.id.btnWide).setEnabled(enabled);
        findViewById(R.id.btnNoBall).setEnabled(enabled);
    }

    private String getOvers(int totalBalls) {
        int overs = totalBalls / 6;
        int balls = totalBalls % 6;
        return overs + "." + balls;
    }

    private double getRunRate(int runs, int totalBalls) {
        if (totalBalls == 0) return 0.0;
        return (runs * 6.0) / totalBalls;
    }

    private void saveMatch() {
        String winner;
        if (scoreB > scoreA) winner = teamB;
        else if (scoreA > scoreB) winner = teamA;
        else winner = "Draw";

        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        double finalOversA = Double.parseDouble(getOvers(ballsA));
        double finalOversB = Double.parseDouble(getOvers(ballsB));

        ArrayList<PlayerStats> statsA = getDetailedStats(playersA, true);
        ArrayList<PlayerStats> statsB = getDetailedStats(playersB, false);
        
        Gson gson = new Gson();
        String statsAJson = gson.toJson(statsA);
        String statsBJson = gson.toJson(statsB);

        Match match = new Match(teamA, teamB, scoreA, wicketsA, finalOversA,
                                scoreB, wicketsB, finalOversB, winner, date,
                                statsAJson, statsBJson);
        
        new Thread(() -> {
            db.matchDao().insert(match);
            runOnUiThread(() -> {
                Intent intent = new Intent(ScoringActivity.this, ResultActivity.class);
                intent.putExtra("winner", winner);
                intent.putExtra("teamA", teamA);
                intent.putExtra("teamB", teamB);
                intent.putExtra("scoreA", scoreA);
                intent.putExtra("wicketsA", wicketsA);
                intent.putExtra("oversA", finalOversA);
                intent.putExtra("scoreB", scoreB);
                intent.putExtra("wicketsB", wicketsB);
                intent.putExtra("oversB", finalOversB);
                intent.putExtra("statsA", statsA);
                intent.putExtra("statsB", statsB);
                startActivity(intent);
                finish();
            });
        }).start();
    }

    private ArrayList<PlayerStats> getDetailedStats(List<String> players, boolean isTeamA) {
        ArrayList<PlayerStats> stats = new ArrayList<>();
        for (String name : players) {
            String pKey = getPlayerKey(name, isTeamA);
            int runs = batsmanRuns.getOrDefault(pKey, 0);
            int balls = playerBallsPlayed.getOrDefault(pKey, 0);
            boolean isOut = outPlayers.contains(pKey);
            String dismissal = playerDismissal.getOrDefault(pKey, "Not Out");
            
            if (!isOut && (name.equals(currentStriker) || name.equals(currentNonStriker))) {
                // Not out
            } else if (!isOut && balls == 0) {
                dismissal = "DNB";
            }

            stats.add(new PlayerStats(name, runs, balls, dismissal, isOut));
        }
        return stats;
    }
}
