package com.example.cricscore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cricscore.R;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

public class MatchSetupActivity extends AppCompatActivity {

    private TextInputEditText etTeamA, etTeamB, etPlayerCount, etTotalOvers, etCommonPlayer;
    private MaterialCheckBox cbEnableDotOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_setup);

        etTeamA = findViewById(R.id.etTeamA);
        etTeamB = findViewById(R.id.etTeamB);
        etPlayerCount = findViewById(R.id.etPlayerCount);
        etTotalOvers = findViewById(R.id.etTotalOvers);
        etCommonPlayer = findViewById(R.id.etCommonPlayer);
        cbEnableDotOptions = findViewById(R.id.cbEnableDotOptions);

        findViewById(R.id.btnNext).setOnClickListener(v -> {
            String teamA = etTeamA.getText().toString().trim();
            String teamB = etTeamB.getText().toString().trim();
            String pCountStr = etPlayerCount.getText().toString().trim();
            String oversStr = etTotalOvers.getText().toString().trim();
            String commonPlayer = etCommonPlayer.getText().toString().trim();
            boolean enableDotOptions = cbEnableDotOptions.isChecked();

            if (teamA.isEmpty() || teamB.isEmpty() || pCountStr.isEmpty() || oversStr.isEmpty()) {
                Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(MatchSetupActivity.this, PlayersSetupActivity.class);
            intent.putExtra("teamA", teamA);
            intent.putExtra("teamB", teamB);
            intent.putExtra("playerCount", Integer.parseInt(pCountStr));
            intent.putExtra("totalOvers", Integer.parseInt(oversStr));
            intent.putExtra("commonPlayer", commonPlayer);
            intent.putExtra("enableDotOptions", enableDotOptions);
            intent.putExtra("setupForTeamA", true);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btnDummy).setOnClickListener(v -> {
            etTeamA.setText("India");
            etTeamB.setText("Australia");
            etPlayerCount.setText("11");
            etTotalOvers.setText("5");
            etCommonPlayer.setText("");
            cbEnableDotOptions.setChecked(true);
        });
    }
}
