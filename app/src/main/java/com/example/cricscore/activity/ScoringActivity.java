package com.example.cricscore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cricscore.MainActivity;
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
    
    // Super Over tracking
    private boolean isSuperOver = false;
    private int mainScoreA, mainWicketsA, mainBallsA;
    private int mainScoreB, mainWicketsB, mainBallsB;
    private ArrayList<PlayerStats> mainStatsA, mainStatsB;

    private String currentStriker = "Select";
    private String currentNonStriker = "Select";
    private String currentBowler = "Select";

    private Map<String, Integer> batsmanRuns = new HashMap<>();
    private Map<String, Integer> playerBallsPlayed = new HashMap<>();
    private Map<String, Integer> batsmanFours = new HashMap<>();
    private Map<String, Integer> batsmanSixes = new HashMap<>();
    private Map<String, String> playerDismissal = new HashMap<>(); 

    private Map<String, Integer> bowlerRunsMap = new HashMap<>();
    private Map<String, Integer> bowlerWicketsMap = new HashMap<>();
    private Map<String, Integer> bowlerBallsMap = new HashMap<>();
    private Map<String, Integer> bowlerMaidensMap = new HashMap<>();
    
    private Map<String, Integer> fielderCatches = new HashMap<>();
    private Map<String, Integer> fielderStumpings = new HashMap<>();
    private Map<String, Integer> fielderRunOuts = new HashMap<>();

    private List<String> currentOverHistory = new ArrayList<>();
    private int currentOverRuns = 0;
    private boolean currentOverHasExtras = false; 
    
    private Set<String> outPlayers = new HashSet<>();

    private TextView tvCurrentInnings, tvScore, tvOvers, tvRunRate, tvStriker, tvNonStriker, tvBowler, tvThisOver;
    private AppDatabase db;

    public static class PlayerStats implements Serializable {
        public String name;
        public int runs;
        public int balls;
        public int fours;
        public int sixes;
        public String dismissal; 
        public boolean isOut;
        public int bowlingRuns;
        public int bowlingBalls;
        public int wickets;
        public int maidens;
        public int catches;
        public int stumpings;
        public int runOuts;

        public PlayerStats(String name) {
            this.name = name;
            this.dismissal = "DNB";
        }
    }

    private static class ScoreState {
        final int sA, wA, bA, sB, wB, bB;
        final boolean inn1;
        final String striker, nonStriker, bowler;
        final Map<String, Integer> bRuns, ballsPlayed, bFours, bSixes;
        final Map<String, Integer> boRuns, boWickets, boBalls, boMaidens;
        final Map<String, Integer> fCatches, fStumpings, fRunOuts;
        final List<String> overHistory;
        final Set<String> outPlayers;
        final Map<String, String> dismissals;
        final int cOverRuns;

        ScoreState(int sA, int wA, int bA, int sB, int wB, int bB, boolean inn1, 
                   String striker, String nonStriker, String bowler,
                   Map<String, Integer> bRuns, Map<String, Integer> ballsPlayed, Map<String, Integer> bFours, Map<String, Integer> bSixes,
                   Map<String, Integer> boRuns, Map<String, Integer> boWickets, Map<String, Integer> boBalls, Map<String, Integer> boMaidens,
                   Map<String, Integer> fCatches, Map<String, Integer> fStumpings, Map<String, Integer> fRunOuts,
                   List<String> overHistory, Set<String> outPlayers, Map<String, String> dismissals, int cOverRuns) {
            this.sA = sA; this.wA = wA; this.bA = bA;
            this.sB = sB; this.wB = wB; this.bB = bB;
            this.inn1 = inn1;
            this.striker = striker;
            this.nonStriker = nonStriker;
            this.bowler = bowler;
            this.bRuns = new HashMap<>(bRuns);
            this.ballsPlayed = new HashMap<>(ballsPlayed);
            this.bFours = new HashMap<>(bFours);
            this.bSixes = new HashMap<>(bSixes);
            this.boRuns = new HashMap<>(boRuns);
            this.boWickets = new HashMap<>(boWickets);
            this.boBalls = new HashMap<>(boBalls);
            this.boMaidens = new HashMap<>(boMaidens);
            this.fCatches = new HashMap<>(fCatches);
            this.fStumpings = new HashMap<>(fStumpings);
            this.fRunOuts = new HashMap<>(fRunOuts);
            this.overHistory = new ArrayList<>(overHistory);
            this.outPlayers = new HashSet<>(outPlayers);
            this.dismissals = new HashMap<>(dismissals);
            this.cOverRuns = cOverRuns;
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

        findViewById(R.id.btnSwap).setOnClickListener(v -> {
            saveState();
            rotateStrike();
            updateUI();
        });

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
        findViewById(R.id.btnStopMatch).setOnClickListener(v -> showStopMatchDialog());
    }

    private void showStopMatchDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Match?")
                .setMessage("Are you sure you want to stop this match? All current progress will be lost and no data will be saved.")
                .setPositiveButton("Stop Match", (dialog, which) -> {
                    Intent intent = new Intent(ScoringActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Continue Scoring", null)
                .show();
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
        currentOverRuns = 0;
        currentOverHasExtras = false;
        outPlayers.clear();
        updateUI();
        Toast.makeText(this, "Target: " + (scoreA + 1), Toast.LENGTH_LONG).show();
    }

    private void saveState() {
        history.add(new ScoreState(scoreA, wicketsA, ballsA, scoreB, wicketsB, ballsB, isInnings1, 
                currentStriker, currentNonStriker, currentBowler, batsmanRuns, playerBallsPlayed, batsmanFours, batsmanSixes,
                bowlerRunsMap, bowlerWicketsMap, bowlerBallsMap, bowlerMaidensMap, 
                fielderCatches, fielderStumpings, fielderRunOuts,
                currentOverHistory, outPlayers, playerDismissal, currentOverRuns));
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
            playerBallsPlayed = new HashMap<>(last.ballsPlayed);
            batsmanFours = new HashMap<>(last.bFours);
            batsmanSixes = new HashMap<>(last.bSixes);
            bowlerRunsMap = new HashMap<>(last.boRuns);
            bowlerWicketsMap = new HashMap<>(last.boWickets);
            bowlerBallsMap = new HashMap<>(last.boBalls);
            bowlerMaidensMap = new HashMap<>(last.boMaidens);
            fielderCatches = new HashMap<>(last.fCatches);
            fielderStumpings = new HashMap<>(last.fStumpings);
            fielderRunOuts = new HashMap<>(last.fRunOuts);
            currentOverHistory = new ArrayList<>(last.overHistory);
            outPlayers = new HashSet<>(last.outPlayers);
            playerDismissal = new HashMap<>(last.dismissals);
            currentOverRuns = last.cOverRuns;
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
        findViewById(R.id.btn1D).setOnClickListener(v -> addDotWithRuns(1));
        findViewById(R.id.btn2D).setOnClickListener(v -> addDotWithRuns(2));
        findViewById(R.id.btnWicket).setOnClickListener(v -> handleWicketClick());
        findViewById(R.id.btnWide).setOnClickListener(v -> addExtra("WD", 1));
        findViewById(R.id.btnNoBall).setOnClickListener(v -> showNoBallDialog());
    }

    private void addDotWithRuns(int runs) {
        if (!checkPlayersSelected()) return;
        saveState();
        
        String bKey = getPlayerKey(currentStriker, isInnings1);
        String boKey = getPlayerKey(currentBowler, !isInnings1);

        batsmanRuns.put(bKey, batsmanRuns.getOrDefault(bKey, 0) + runs);
        playerBallsPlayed.put(bKey, playerBallsPlayed.getOrDefault(bKey, 0) + 1);
        bowlerRunsMap.put(boKey, bowlerRunsMap.getOrDefault(boKey, 0) + runs);
        bowlerBallsMap.put(boKey, bowlerBallsMap.getOrDefault(boKey, 0) + 1);
        currentOverRuns += runs;
        
        currentOverHistory.add(runs + "D");

        if (isInnings1) {
            scoreA += runs;
            ballsA++;
            checkOverEnd(ballsA);
            checkInningsEnd();
        } else {
            scoreB += runs;
            ballsB++;
            checkOverEnd(ballsB);
            checkMatchEnd();
        }
        updateUI();
    }

    private void showNoBallDialog() {
        if (!checkPlayersSelected()) return;
        String[] options = {"NB (Only)", "NB + 1 Run", "NB + 2 Runs", "NB + 3 Runs", "NB + 4 Runs", "NB + 6 Runs"};
        int[] runValues = {0, 1, 2, 3, 4, 6};
        
        new AlertDialog.Builder(this)
                .setTitle("No Ball Extras")
                .setItems(options, (dialog, which) -> {
                    handleNoBall(runValues[which]);
                })
                .show();
    }

    private void handleNoBall(int extraRuns) {
        saveState();
        int totalRunsForBall = 1 + extraRuns; 
        
        String bKey = getPlayerKey(currentStriker, isInnings1);
        String boKey = getPlayerKey(currentBowler, !isInnings1);

        if (extraRuns > 0) {
            batsmanRuns.put(bKey, batsmanRuns.getOrDefault(bKey, 0) + extraRuns);
            if (extraRuns == 4) batsmanFours.put(bKey, batsmanFours.getOrDefault(bKey, 0) + 1);
            if (extraRuns == 6) batsmanSixes.put(bKey, batsmanSixes.getOrDefault(bKey, 0) + 1);
            if (extraRuns % 2 != 0) rotateStrike();
        }
        
        bowlerRunsMap.put(boKey, bowlerRunsMap.getOrDefault(boKey, 0) + totalRunsForBall);
        currentOverRuns += totalRunsForBall;
        currentOverHasExtras = true;
        
        currentOverHistory.add("NB" + (extraRuns > 0 ? "+" + extraRuns : ""));

        if (isInnings1) {
            scoreA += totalRunsForBall;
            checkInningsEnd();
        } else {
            scoreB += totalRunsForBall;
            checkMatchEnd();
        }
        updateUI();
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
                    showFielderPicker(playerName, isStrikerOut, howOut);
                })
                .show();
    }

    private void showFielderPicker(String playerOutName, boolean isStrikerOut, String type) {
        if (!type.equals("Caught") && !type.equals("Stumped") && !type.equals("Run Out")) {
            processWicket(playerOutName, isStrikerOut, type + " b " + currentBowler, null);
            return;
        }

        List<String> fieldingTeam = isInnings1 ? playersB : playersA;
        String[] fielders = fieldingTeam.toArray(new String[0]);

        new AlertDialog.Builder(this)
                .setTitle("Select Fielder/Keeper")
                .setItems(fielders, (dialog, which) -> {
                    String fielderName = fielders[which];
                    String desc = type + " (" + fielderName + ") b " + currentBowler;
                    if (type.equals("Run Out")) desc = "Run Out (" + fielderName + ")";
                    processWicket(playerOutName, isStrikerOut, desc, fielderName);
                })
                .show();
    }

    private void processWicket(String playerOutName, boolean isStrikerOut, String dismissalDesc, String fielderName) {
        saveState();
        String pKey = getPlayerKey(playerOutName, isInnings1);
        String boKey = getPlayerKey(currentBowler, !isInnings1);

        if (!dismissalDesc.contains("Run Out")) {
            bowlerWicketsMap.put(boKey, bowlerWicketsMap.getOrDefault(boKey, 0) + 1);
        }
        
        if (fielderName != null) {
            String fKey = getPlayerKey(fielderName, !isInnings1);
            if (dismissalDesc.contains("Caught")) fielderCatches.put(fKey, fielderCatches.getOrDefault(fKey, 0) + 1);
            else if (dismissalDesc.contains("Stumped")) fielderStumpings.put(fKey, fielderStumpings.getOrDefault(fKey, 0) + 1);
            else if (dismissalDesc.contains("Run Out")) fielderRunOuts.put(fKey, fielderRunOuts.getOrDefault(fKey, 0) + 1);
        }
        
        outPlayers.add(pKey);
        playerDismissal.put(pKey, dismissalDesc);
        playerBallsPlayed.put(pKey, playerBallsPlayed.getOrDefault(pKey, 0) + 1);
        bowlerBallsMap.put(boKey, bowlerBallsMap.getOrDefault(boKey, 0) + 1);
        
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
        if (runs == 4) batsmanFours.put(bKey, batsmanFours.getOrDefault(bKey, 0) + 1);
        if (runs == 6) batsmanSixes.put(bKey, batsmanSixes.getOrDefault(bKey, 0) + 1);
        
        bowlerRunsMap.put(boKey, bowlerRunsMap.getOrDefault(boKey, 0) + runs);
        bowlerBallsMap.put(boKey, bowlerBallsMap.getOrDefault(boKey, 0) + 1);
        currentOverRuns += runs;
        
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
        currentOverRuns += runs;
        currentOverHasExtras = true;
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
            String boKey = getPlayerKey(currentBowler, !isInnings1);
            if (currentOverRuns == 0 && !currentOverHasExtras) {
                bowlerMaidensMap.put(boKey, bowlerMaidensMap.getOrDefault(boKey, 0) + 1);
            }
            currentOverRuns = 0;
            currentOverHasExtras = false;
            
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
            if (scoreA == scoreB) {
                showSuperOverDialog();
            } else {
                saveMatch();
            }
        }
    }

    private void showSuperOverDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Match Drawn!")
                .setMessage("Scores are level! Would you like to play a Super Over (1 Over) to decide the winner?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> startSuperOver())
                .setNegativeButton("No", (dialog, which) -> saveMatch())
                .show();
    }

    private void startSuperOver() {
        // CAPTURE MAIN MATCH STATS BEFORE RESET
        mainStatsA = getDetailedStats(playersA, true);
        mainStatsB = getDetailedStats(playersB, false);
        
        mainScoreA = scoreA; mainWicketsA = wicketsA; mainBallsA = ballsA;
        mainScoreB = scoreB; mainWicketsB = wicketsB; mainBallsB = ballsB;
        
        isSuperOver = true;
        isInnings1 = true;
        totalOvers = 1;
        maxWicketsA = 2; 
        maxWicketsB = 2;
        
        // RESET GAME STATE FOR SUPER OVER
        scoreA = 0; wicketsA = 0; ballsA = 0;
        scoreB = 0; wicketsB = 0; ballsB = 0;
        
        currentStriker = "Select";
        currentNonStriker = "Select";
        currentBowler = "Select";
        currentOverHistory.clear();
        currentOverRuns = 0;
        currentOverHasExtras = false;
        outPlayers.clear();
        history.clear();
        
        // CLEAR STAT MAPS FOR SUPER OVER
        batsmanRuns.clear();
        playerBallsPlayed.clear();
        batsmanFours.clear();
        batsmanSixes.clear();
        playerDismissal.clear();
        bowlerRunsMap.clear();
        bowlerWicketsMap.clear();
        bowlerBallsMap.clear();
        bowlerMaidensMap.clear();
        fielderCatches.clear();
        fielderStumpings.clear();
        fielderRunOuts.clear();
        
        updateUI();
        Toast.makeText(this, "SUPER OVER STARTED!", Toast.LENGTH_SHORT).show();
    }

    private void updateUI() {
        String inningsTitle = isSuperOver ? "SUPER OVER: " : "";
        if (isInnings1) {
            tvCurrentInnings.setText(inningsTitle + teamA + " Batting");
            tvScore.setText(scoreA + "/" + wicketsA);
            tvOvers.setText("Overs: " + getOvers(ballsA) + " / " + totalOvers);
            tvRunRate.setText("CRR: " + String.format(Locale.US, "%.2f", getRunRate(scoreA, ballsA)));
        } else {
            tvCurrentInnings.setText(inningsTitle + teamB + " Batting (Target: " + (scoreA + 1) + ")");
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
        findViewById(R.id.btn1D).setEnabled(enabled);
        findViewById(R.id.btn2D).setEnabled(enabled);
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

        if (isSuperOver && !winner.equals("Draw")) {
            winner += " (Super Over)";
        }

        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        int finalScoreA = isSuperOver ? mainScoreA : scoreA;
        int finalWicketsA = isSuperOver ? mainWicketsA : wicketsA;
        double finalOversA = Double.parseDouble(getOvers(isSuperOver ? mainBallsA : ballsA));

        int finalScoreB = isSuperOver ? mainScoreB : scoreB;
        int finalWicketsB = isSuperOver ? mainWicketsB : wicketsB;
        double finalOversB = Double.parseDouble(getOvers(isSuperOver ? mainBallsB : ballsB));

        ArrayList<PlayerStats> statsA, statsB;
        ArrayList<PlayerStats> superStatsA = null, superStatsB = null;

        if (isSuperOver) {
            statsA = mainStatsA;
            statsB = mainStatsB;
            superStatsA = getDetailedStats(playersA, true);
            superStatsB = getDetailedStats(playersB, false);
        } else {
            statsA = getDetailedStats(playersA, true);
            statsB = getDetailedStats(playersB, false);
        }
        
        Gson gson = new Gson();
        String statsAJson = gson.toJson(statsA);
        String statsBJson = gson.toJson(statsB);

        Match match = new Match(teamA, teamB, finalScoreA, finalWicketsA, finalOversA,
                                finalScoreB, finalWicketsB, finalOversB, winner, date,
                                statsAJson, statsBJson);
        
        if (isSuperOver) {
            match.hasSuperOver = true;
            match.superScoreA = scoreA;
            match.superWicketsA = wicketsA;
            match.superScoreB = scoreB;
            match.superWicketsB = wicketsB;
            match.superStatsAJson = gson.toJson(superStatsA);
            match.superStatsBJson = gson.toJson(superStatsB);
        }

        String finalWinner = winner;
        ArrayList<PlayerStats> finalStatsA = statsA;
        ArrayList<PlayerStats> finalStatsB = statsB;
        ArrayList<PlayerStats> finalSuperStatsA = superStatsA;
        ArrayList<PlayerStats> finalSuperStatsB = superStatsB;

        new Thread(() -> {
            db.matchDao().insert(match);
            runOnUiThread(() -> {
                Intent intent = new Intent(ScoringActivity.this, ResultActivity.class);
                intent.putExtra("winner", finalWinner);
                intent.putExtra("teamA", teamA);
                intent.putExtra("teamB", teamB);
                intent.putExtra("scoreA", finalScoreA);
                intent.putExtra("wicketsA", finalWicketsA);
                intent.putExtra("oversA", finalOversA);
                intent.putExtra("scoreB", finalScoreB);
                intent.putExtra("wicketsB", finalWicketsB);
                intent.putExtra("oversB", finalOversB);
                intent.putExtra("statsA", finalStatsA);
                intent.putExtra("statsB", finalStatsB);
                if (isSuperOver) {
                    intent.putExtra("hasSuperOver", true);
                    intent.putExtra("superScoreA", scoreA);
                    intent.putExtra("superWicketsA", wicketsA);
                    intent.putExtra("superScoreB", scoreB);
                    intent.putExtra("superWicketsB", wicketsB);
                    intent.putExtra("superStatsA", finalSuperStatsA);
                    intent.putExtra("superStatsB", finalSuperStatsB);
                }
                startActivity(intent);
                finish();
            });
        }).start();
    }

    private ArrayList<PlayerStats> getDetailedStats(List<String> players, boolean isTeamA) {
        ArrayList<PlayerStats> stats = new ArrayList<>();
        for (String name : players) {
            String pKey = getPlayerKey(name, isTeamA);
            PlayerStats ps = new PlayerStats(name);
            
            ps.runs = batsmanRuns.getOrDefault(pKey, 0);
            ps.balls = playerBallsPlayed.getOrDefault(pKey, 0);
            ps.fours = batsmanFours.getOrDefault(pKey, 0);
            ps.sixes = batsmanSixes.getOrDefault(pKey, 0);
            ps.isOut = outPlayers.contains(pKey);
            ps.dismissal = playerDismissal.getOrDefault(pKey, ps.balls > 0 || name.equals(currentStriker) || name.equals(currentNonStriker) ? "Not Out" : "DNB");

            ps.bowlingRuns = bowlerRunsMap.getOrDefault(pKey, 0);
            ps.bowlingBalls = bowlerBallsMap.getOrDefault(pKey, 0);
            ps.wickets = bowlerWicketsMap.getOrDefault(pKey, 0);
            ps.maidens = bowlerMaidensMap.getOrDefault(pKey, 0);
            
            ps.catches = fielderCatches.getOrDefault(pKey, 0);
            ps.stumpings = fielderStumpings.getOrDefault(pKey, 0);
            ps.runOuts = fielderRunOuts.getOrDefault(pKey, 0);

            stats.add(ps);
        }
        return stats;
    }
}
