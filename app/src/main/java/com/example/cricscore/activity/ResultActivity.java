package com.example.cricscore.activity;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.cricscore.MainActivity;
import com.example.cricscore.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    private String teamA, teamB, winner;
    private int scoreA, wicketsA, scoreB, wicketsB;
    private double oversA, oversB;
    private boolean hasSuperOver;
    private int superScoreA, superWicketsA, superScoreB, superWicketsB;
    private ArrayList<ScoringActivity.PlayerStats> statsA, statsB, superStatsA, superStatsB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        winner = getIntent().getStringExtra("winner");
        teamA = getIntent().getStringExtra("teamA");
        teamB = getIntent().getStringExtra("teamB");
        
        scoreA = getIntent().getIntExtra("scoreA", 0);
        wicketsA = getIntent().getIntExtra("wicketsA", 0);
        oversA = getIntent().getDoubleExtra("oversA", 0.0);
        scoreB = getIntent().getIntExtra("scoreB", 0);
        wicketsB = getIntent().getIntExtra("wicketsB", 0);
        oversB = getIntent().getDoubleExtra("oversB", 0.0);

        hasSuperOver = getIntent().getBooleanExtra("hasSuperOver", false);
        superScoreA = getIntent().getIntExtra("superScoreA", 0);
        superWicketsA = getIntent().getIntExtra("superWicketsA", 0);
        superScoreB = getIntent().getIntExtra("superScoreB", 0);
        superWicketsB = getIntent().getIntExtra("superWicketsB", 0);

        statsA = (ArrayList<ScoringActivity.PlayerStats>) getIntent().getSerializableExtra("statsA");
        statsB = (ArrayList<ScoringActivity.PlayerStats>) getIntent().getSerializableExtra("statsB");
        superStatsA = (ArrayList<ScoringActivity.PlayerStats>) getIntent().getSerializableExtra("superStatsA");
        superStatsB = (ArrayList<ScoringActivity.PlayerStats>) getIntent().getSerializableExtra("superStatsB");

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

        findViewById(R.id.btnSharePdf).setOnClickListener(v -> createAndSharePdf());
    }

    private void populateStats(LinearLayout container, ArrayList<ScoringActivity.PlayerStats> statsList) {
        if (statsList == null) return;
        
        LayoutInflater inflater = LayoutInflater.from(this);
        for (ScoringActivity.PlayerStats ps : statsList) {
            if (ps.balls == 0 && ps.bowlingBalls == 0 && ps.catches == 0 && ps.stumpings == 0 && ps.runOuts == 0 && "DNB".equals(ps.dismissal)) {
                continue;
            }

            View view = inflater.inflate(R.layout.item_player_stat, container, false);
            
            ((TextView) view.findViewById(R.id.tvStatName)).setText(ps.name);
            ((TextView) view.findViewById(R.id.tvStatDismissal)).setText(ps.dismissal);

            ((TextView) view.findViewById(R.id.tvStatRuns)).setText(String.valueOf(ps.runs));
            ((TextView) view.findViewById(R.id.tvStatBalls)).setText(String.valueOf(ps.balls));
            ((TextView) view.findViewById(R.id.tvStat4s)).setText(String.valueOf(ps.fours));
            ((TextView) view.findViewById(R.id.tvStat6s)).setText(String.valueOf(ps.sixes));
            
            double sr = (ps.balls == 0) ? 0.0 : (ps.runs * 100.0) / ps.balls;
            ((TextView) view.findViewById(R.id.tvStatSR)).setText(String.format(Locale.US, "Strike Rate: %.1f", sr));

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

    private void createAndSharePdf() {
        PdfDocument document = new PdfDocument();
        Paint paint = new Paint();
        Paint titlePaint = new Paint();
        Paint headerPaint = new Paint();

        int pageWidth = 595; // A4 size
        int pageHeight = 842;
        int y = 40;
        int margin = 40;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setTextSize(20);
        titlePaint.setColor(Color.BLACK);

        headerPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        headerPaint.setTextSize(16);
        headerPaint.setColor(Color.DKGRAY);

        paint.setTextSize(12);
        paint.setColor(Color.BLACK);

        // Header
        canvas.drawText("Match Result - CricScore", margin, y, titlePaint);
        y += 30;
        canvas.drawText("Winner: " + ("Draw".equals(winner) ? "Match Drawn" : winner + " Won"), margin, y, headerPaint);
        y += 40;

        // Main Match Stats
        y = drawTeamStats(canvas, teamA, scoreA, wicketsA, oversA, statsA, y, margin, pageWidth, document);
        
        // Check if we need a new page for Team B
        if (y > pageHeight - 100) {
            document.finishPage(page);
            page = document.startPage(new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create());
            canvas = page.getCanvas();
            y = 40;
        } else {
            y += 20;
        }

        y = drawTeamStats(canvas, teamB, scoreB, wicketsB, oversB, statsB, y, margin, pageWidth, document);

        // Super Over Stats
        if (hasSuperOver) {
            if (y > pageHeight - 150) {
                document.finishPage(page);
                page = document.startPage(new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 3).create());
                canvas = page.getCanvas();
                y = 40;
            } else {
                y += 30;
            }
            canvas.drawText("SUPER OVER", margin, y, titlePaint);
            y += 25;
            y = drawTeamStats(canvas, teamA + " (Super Over)", superScoreA, superWicketsA, 1.0, superStatsA, y, margin, pageWidth, document);
            y += 20;
            y = drawTeamStats(canvas, teamB + " (Super Over)", superScoreB, superWicketsB, 1.0, superStatsB, y, margin, pageWidth, document);
        }

        document.finishPage(page);

        File cachePath = new File(getCacheDir(), "pdf");
        cachePath.mkdirs();
        
        String dateStr = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date());
        String fileName = teamA.replace(" ", "_") + "_vs_" + teamB.replace(" ", "_") + "_" + dateStr + ".pdf";
        File file = new File(cachePath, fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            document.writeTo(fos);
            document.close();
            shareFile(file);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generating PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private int drawTeamStats(Canvas canvas, String teamName, int score, int wickets, double overs, 
                              ArrayList<ScoringActivity.PlayerStats> stats, int y, int margin, int pageWidth, PdfDocument document) {
        Paint headerPaint = new Paint();
        headerPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        headerPaint.setTextSize(14);
        
        Paint textPaint = new Paint();
        textPaint.setTextSize(11);

        canvas.drawText(teamName + ": " + score + "/" + wickets + " (" + overs + " Overs)", margin, y, headerPaint);
        y += 20;

        // Batting Table Header
        canvas.drawText("Batter", margin, y, textPaint);
        canvas.drawText("R", margin + 150, y, textPaint);
        canvas.drawText("B", margin + 180, y, textPaint);
        canvas.drawText("4s", margin + 210, y, textPaint);
        canvas.drawText("6s", margin + 240, y, textPaint);
        canvas.drawText("SR", margin + 270, y, textPaint);
        canvas.drawText("Dismissal", margin + 320, y, textPaint);
        y += 15;
        canvas.drawLine(margin, y - 10, pageWidth - margin, y - 10, textPaint);

        for (ScoringActivity.PlayerStats ps : stats) {
            if (ps.balls == 0 && "DNB".equals(ps.dismissal)) continue;
            
            canvas.drawText(ps.name, margin, y, textPaint);
            canvas.drawText(String.valueOf(ps.runs), margin + 150, y, textPaint);
            canvas.drawText(String.valueOf(ps.balls), margin + 180, y, textPaint);
            canvas.drawText(String.valueOf(ps.fours), margin + 210, y, textPaint);
            canvas.drawText(String.valueOf(ps.sixes), margin + 240, y, textPaint);
            double sr = (ps.balls == 0) ? 0.0 : (ps.runs * 100.0) / ps.balls;
            canvas.drawText(String.format(Locale.US, "%.1f", sr), margin + 270, y, textPaint);
            canvas.drawText(ps.dismissal, margin + 320, y, textPaint);
            y += 15;
        }

        y += 10;
        // Bowling Table Header
        canvas.drawText("Bowler", margin, y, textPaint);
        canvas.drawText("O", margin + 150, y, textPaint);
        canvas.drawText("M", margin + 180, y, textPaint);
        canvas.drawText("R", margin + 210, y, textPaint);
        canvas.drawText("W", margin + 240, y, textPaint);
        canvas.drawText("Econ", margin + 270, y, textPaint);
        y += 15;
        canvas.drawLine(margin, y - 10, pageWidth - margin, y - 10, textPaint);

        for (ScoringActivity.PlayerStats ps : stats) {
            if (ps.bowlingBalls == 0) continue;
            
            canvas.drawText(ps.name, margin, y, textPaint);
            String oversStr = (ps.bowlingBalls / 6) + "." + (ps.bowlingBalls % 6);
            canvas.drawText(oversStr, margin + 150, y, textPaint);
            canvas.drawText(String.valueOf(ps.maidens), margin + 180, y, textPaint);
            canvas.drawText(String.valueOf(ps.bowlingRuns), margin + 210, y, textPaint);
            canvas.drawText(String.valueOf(ps.wickets), margin + 240, y, textPaint);
            double econ = (ps.bowlingBalls == 0) ? 0.0 : (ps.bowlingRuns * 6.0) / ps.bowlingBalls;
            canvas.drawText(String.format(Locale.US, "%.2f", econ), margin + 270, y, textPaint);
            y += 15;
        }

        return y;
    }

    private void shareFile(File file) {
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Share Match Result"));
    }
}
