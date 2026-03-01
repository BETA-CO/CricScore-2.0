package com.example.cricscore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cricscore.R;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class PlayersSetupActivity extends AppCompatActivity {

    private String teamA, teamB, commonPlayer;
    private int playerCount, totalOvers;
    private boolean setupForTeamA, enableDotOptions;
    private String playersA;
    private List<EditText> editTexts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_players_setup);

        teamA = getIntent().getStringExtra("teamA");
        teamB = getIntent().getStringExtra("teamB");
        commonPlayer = getIntent().getStringExtra("commonPlayer");
        playerCount = getIntent().getIntExtra("playerCount", 11);
        totalOvers = getIntent().getIntExtra("totalOvers", 5);
        setupForTeamA = getIntent().getBooleanExtra("setupForTeamA", true);
        enableDotOptions = getIntent().getBooleanExtra("enableDotOptions", true);
        playersA = getIntent().getStringExtra("playersA");

        // Update labels based on which team we are setting up
        TextView tvTitle = findViewById(R.id.tvEnterPlayersTitle);
        TextView tvSubtitle = findViewById(R.id.tvEnterPlayersSubtitle);

        String currentTeam = setupForTeamA ? teamA : teamB;
        if (tvTitle != null) {
            tvTitle.setText(currentTeam + " Players");
        }
        if (tvSubtitle != null) {
            tvSubtitle.setText("Enter names for " + currentTeam);
        }

        LinearLayout fieldsHolder = findViewById(R.id.playerFieldsHolder);

        for (int i = 1; i <= playerCount; i++) {
            TextInputLayout inputLayout = new TextInputLayout(this, null, com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox);
            inputLayout.setHint("Player " + i);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 16);
            inputLayout.setLayoutParams(params);

            EditText editText = new EditText(this);
            editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            inputLayout.addView(editText);

            if (fieldsHolder != null) {
                fieldsHolder.addView(inputLayout);
            }
            editTexts.add(editText);
        }

        findViewById(R.id.btnSubmit).setOnClickListener(v -> {
            boolean allFilled = true;
            for (int i = 0; i < editTexts.size(); i++) {
                EditText et = editTexts.get(i);
                if (et.getText().toString().trim().isEmpty()) {
                    et.setError("Player name required");
                    allFilled = false;
                }
            }

            if (!allFilled) {
                Toast.makeText(this, "Please fill all player names before continuing", Toast.LENGTH_SHORT).show();
                return;
            }

            StringBuilder names = new StringBuilder();
            for (int i = 0; i < editTexts.size(); i++) {
                String name = editTexts.get(i).getText().toString().trim();
                names.append(name);
                if (i < editTexts.size() - 1) names.append(",");
            }

            if (setupForTeamA) {
                Intent intent = new Intent(PlayersSetupActivity.this, PlayersSetupActivity.class);
                intent.putExtra("teamA", teamA);
                intent.putExtra("teamB", teamB);
                intent.putExtra("commonPlayer", commonPlayer);
                intent.putExtra("playerCount", playerCount);
                intent.putExtra("totalOvers", totalOvers);
                intent.putExtra("enableDotOptions", enableDotOptions);
                intent.putExtra("setupForTeamA", false);
                intent.putExtra("playersA", names.toString());
                startActivity(intent);
                finish();
            } else {
                String finalPlayersA = playersA;
                String finalPlayersB = names.toString();
                
                // Add common player to both teams if specified
                if (commonPlayer != null && !commonPlayer.isEmpty()) {
                    finalPlayersA += "," + commonPlayer;
                    finalPlayersB += "," + commonPlayer;
                }

                Intent intent = new Intent(PlayersSetupActivity.this, TossActivity.class);
                intent.putExtra("teamA", teamA);
                intent.putExtra("teamB", teamB);
                intent.putExtra("commonPlayer", commonPlayer);
                intent.putExtra("playerCount", playerCount + (commonPlayer != null && !commonPlayer.isEmpty() ? 1 : 0));
                intent.putExtra("totalOvers", totalOvers);
                intent.putExtra("enableDotOptions", enableDotOptions);
                intent.putExtra("playersA", finalPlayersA);
                intent.putExtra("playersB", finalPlayersB);
                startActivity(intent);
                finish();
            }
        });

        findViewById(R.id.btnDummyPlayers).setOnClickListener(v -> {
            for (int i = 0; i < editTexts.size(); i++) {
                editTexts.get(i).setText(currentTeam + " Star " + (i + 1));
                editTexts.get(i).setError(null);
            }
        });
    }
}
