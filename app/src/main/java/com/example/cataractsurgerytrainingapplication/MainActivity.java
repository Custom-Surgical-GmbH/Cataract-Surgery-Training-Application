package com.example.cataractsurgerytrainingapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    MainActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
        }
    }

    public void startLimbusDetectionDemo(View view) {
        Intent intent = new Intent(this, LimbusDetectionDemoActivity.class);
        startActivity(intent);
    }

    public void startVideoProcessingDemo(View view) {
        Intent intent = new Intent(this, VideoProcessingDemoActivity.class);
        startActivity(intent);
    }

    public void startCataractSurgeryTrainingActivity(View view) {
        Intent intent = new Intent(this, CataractSurgeryTrainingActivity.class);
        startActivity(intent);
    }

    private void enableVideoFunctinalityAccess() {
        Button videoProcessingDemoBtn = (Button) findViewById(R.id.videoProcessingDemoBtn);
        Button cataractSurgeryTrainingBtn = (Button) findViewById(R.id.cataractSurgeryTrainingBtn);

        videoProcessingDemoBtn.setEnabled(true);
        cataractSurgeryTrainingBtn.setEnabled(true);
    }

    private void desibleVideoFunctionalityAccess() {
        Button videoProcessingDemoBtn = (Button) findViewById(R.id.videoProcessingDemoBtn);
        Button cataractSurgeryTrainingBtn = (Button) findViewById(R.id.cataractSurgeryTrainingBtn);

        videoProcessingDemoBtn.setEnabled(false);
        cataractSurgeryTrainingBtn.setEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && permissions[0].equals(Manifest.permission.CAMERA)) {
                    enableVideoFunctinalityAccess();
                    Toast.makeText(
                            MainActivity.this,
                            R.string.camera_permission_granted,
                            Toast.LENGTH_LONG).show();
                } else {
                    desibleVideoFunctionalityAccess();
                    Toast.makeText(
                            MainActivity.this,
                            R.string.camera_permission_denied,
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
}