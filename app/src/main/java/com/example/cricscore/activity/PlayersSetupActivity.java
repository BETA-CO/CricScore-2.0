package com.example.cricscore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cricscore.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.List;

public class PlayersSetupActivity extends AppCompatActivity {

    private String teamA, teamB;
    private int playerCount, totalOvers;
    private boolean setupForTeamA;
    private String playersA; 
    private List<EditText> editTexts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_players_setup);

        teamA = getIntent().getStringExtra("teamA");
        teamB = getIntent().getStringExtra("teamB");
        playerCount = getIntent().getIntExtra("playerCount", 11);
        totalOvers = getIntent().getIntExtra("totalOvers", 5);
        setupForTeamA = getIntent().getBooleanExtra("setupForTeamA", true);
        playersA = getIntent().getStringExtra("playersA");

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Enter " + (setupForTeamA ? teamA : teamB) + " Players");

        LinearLayout container = findViewById(R.id.llPlayersContainer);
        
        for (int i = 1; i <= playerCount; i++) {
            TextInputLayout inputLayout = new TextInputLayout(this, null, com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox);
            inputLayout.setHint("Enter Player " + i);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 16);
            inputLayout.setLayoutParams(params);

            EditText editText = new EditText(this);
            editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            inputLayout.addView(editText);
            
            // Add before the buttons
            int insertIndex = container.getChildCount() - 2; 
            if (insertIndex < 0) insertIndex = 0;
            container.addView(inputLayout, insertIndex);
            editTexts.add(editText);
        }

        findViewById(R.id.btnSubmit).setOnClickListener(v -> {
            StringBuilder names = new StringBuilder();
            for (int i = 0; i < editTexts.size(); i++) {
                String name = editTexts.get(i).getText().toString().trim();
                if (name.isEmpty()) {
                    name = (setupForTeamA ? teamA : teamB) + " Player " + (i + 1);
                }
                names.append(name);
                if (i < editTexts.size() - 1) names.append(",");
            }

            if (setupForTeamA) {
                Intent intent = new Intent(PlayersSetupActivity.this, PlayersSetupActivity.class);
                intent.putExtra("teamA", teamA);
                intent.putExtra("teamB", teamB);
                intent.putExtra("playerCount", playerCount);
                intent.putExtra("totalOvers", totalOvers);
                intent.putExtra("setupForTeamA", false);
                intent.putExtra("playersA", names.toString());
                startActivity(intent);
            } else {
                Intent intent = new Intent(PlayersSetupActivity.this, TossActivity.class);
                intent.putExtra("teamA", teamA);
                intent.putExtra("teamB", teamB);
                intent.putExtra("playerCount", playerCount);
                intent.putExtra("totalOvers", totalOvers);
                intent.putExtra("playersA", playersA);
                intent.putExtra("playersB", names.toString());
                startActivity(intent);
            }
        });

        findViewById(R.id.btnDummyPlayers).setOnClickListener(v -> {
            String teamName = setupForTeamA ? teamA : teamB;
            for (int i = 0; i < editTexts.size(); i++) {
                editTexts.get(i).setText(teamName + " Star " + (i + 1));
            }
        });
    }
}
