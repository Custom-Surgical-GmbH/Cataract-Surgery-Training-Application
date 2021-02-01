package com.example.cataractsurgerytrainingapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class CataractSurgeryTrainingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cataract_surgery_training);
    }

    public void startIncisionsMenu(View view) {
        Intent intent = new Intent(this, IncisionsMenuActivity.class);
        startActivity(intent);
    }
}