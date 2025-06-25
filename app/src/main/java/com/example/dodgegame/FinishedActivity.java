package com.example.dodgegame;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class FinishedActivity extends AppCompatActivity {
    TextView score;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finished);
        Bundle getScore = getIntent().getExtras();
        score = findViewById(R.id.scoreText);
        score.setText("You Scored: " + getScore.getInt("Score"));
    }
}