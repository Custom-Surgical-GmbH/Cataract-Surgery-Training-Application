package com.example.cataractsurgerytrainingapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startLimbusDetection(View view) {
        Intent intent = new Intent(this, LimbusDetectionActivity.class);
        startActivity(intent);
    }

    public void startVideoProcessing(View view) {
        Intent intent = new Intent(this, VideoProcessingActivity.class);
        startActivity(intent);
    }
}