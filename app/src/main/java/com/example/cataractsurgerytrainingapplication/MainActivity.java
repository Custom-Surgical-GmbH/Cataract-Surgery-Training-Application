package com.example.cataractsurgerytrainingapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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

    public void startMemoryLeakTestActivity(View view) {
        Intent intent = new Intent(this, MemoryLeakTestActivity.class);
        startActivity(intent);
    }

    public void startNativeVideoProcessingDemoActivity(View view) {
        Intent intent = new Intent(this, NativeVideoProcessingDemoActivity.class);
        startActivity(intent);
    }

    public void startCataractSurgeryTrainingActivity(View view) {
        Intent intent = new Intent(this, CataractSurgeryTrainingActivity.class);
        startActivity(intent);
    }

    public void startCapsulotomyProjectorPrototypeMenuActivity(View view) {
        Intent intent = new Intent(this, CapsulotomyProjectorPrototypeMenuActivity.class);
        startActivity(intent);
    }

    private void enableVideoFunctionalityAccess() {
        Button videoProcessingDemoBtn = (Button) findViewById(R.id.videoProcessingDemoBtn);
        Button memoryLeakTestBtn = (Button) findViewById(R.id.memoryLeakTestBtn);
        Button nativeVideoProcessingDemoBtn = (Button) findViewById(R.id.nativeVideoProcessingDemoBtn);
        Button cataractSurgeryTrainingBtn = (Button) findViewById(R.id.cataractSurgeryTrainingBtn);
        Button capsulotomyProjectorPrototypeMenuBtn = (Button) findViewById(R.id.capsulotomyProjectorPrototypeMenuBtn);

        videoProcessingDemoBtn.setEnabled(true);
        memoryLeakTestBtn.setEnabled(true);
        nativeVideoProcessingDemoBtn.setEnabled(true);
        cataractSurgeryTrainingBtn.setEnabled(true);
        capsulotomyProjectorPrototypeMenuBtn.setEnabled(true);
    }

    private void desibleVideoFunctionalityAccess() {
        Button videoProcessingDemoBtn = (Button) findViewById(R.id.videoProcessingDemoBtn);
        Button memoryLeakTestBtn = (Button) findViewById(R.id.memoryLeakTestBtn);
        Button nativeVideoProcessingDemoBtn = (Button) findViewById(R.id.nativeVideoProcessingDemoBtn);
        Button cataractSurgeryTrainingBtn = (Button) findViewById(R.id.cataractSurgeryTrainingBtn);
        Button capsulotomyProjectorPrototypeMenuBtn = (Button) findViewById(R.id.capsulotomyProjectorPrototypeMenuBtn);

        videoProcessingDemoBtn.setEnabled(false);
        memoryLeakTestBtn.setEnabled(false);
        nativeVideoProcessingDemoBtn.setEnabled(false);
        cataractSurgeryTrainingBtn.setEnabled(false);
        capsulotomyProjectorPrototypeMenuBtn.setEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && permissions[0].equals(Manifest.permission.CAMERA)) {
                    enableVideoFunctionalityAccess();
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