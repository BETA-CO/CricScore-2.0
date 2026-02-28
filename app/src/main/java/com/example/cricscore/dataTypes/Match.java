package com.example.cricscore.dataTypes;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "matches")
public class Match {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String teamA;
    public String teamB;
    public int scoreA;
    public int wicketsA;
    public double oversA;
    public int scoreB;
    public int wicketsB;
    public double oversB;
    public String winner;
    public String date;
    
    // Storing player stats as JSON strings
    public String statsAJson;
    public String statsBJson;

    // Super Over fields
    public boolean hasSuperOver = false;
    public int superScoreA;
    public int superWicketsA;
    public int superScoreB;
    public int superWicketsB;
    
    // Super Over player stats
    public String superStatsAJson;
    public String superStatsBJson;

    public Match(
            String teamA, String teamB,
            int scoreA,
            int wicketsA,
            double oversA,
            int scoreB,
            int wicketsB,
            double oversB,
            String winner, String date,
            String statsAJson, String statsBJson
    ) {
        this.teamA = teamA;
        this.teamB = teamB;
        this.scoreA = scoreA;
        this.wicketsA = wicketsA;
        this.oversA = oversA;
        this.scoreB = scoreB;
        this.wicketsB = wicketsB;
        this.oversB = oversB;
        this.winner = winner;
        this.date = date;
        this.statsAJson = statsAJson;
        this.statsBJson = statsBJson;
    }
}
