package com.example.cataractsurgerytrainingapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class EmulsificationMenuActivity extends AppCompatActivity {
    private EditText safeZoneDiameterEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emulsification_menu);

        safeZoneDiameterEditText = findViewById(R.id.safeZoneDiameterEditText);
    }

    public void startEmulsificationStage(View view) {
//        if (safeZoneDiameterEditText.length() == 0) {
//            Toast.makeText(this, "All fields must be set to continue.",
//                    Toast.LENGTH_SHORT).show();
//            return;
//        }

        Intent intent = new Intent(this, IncisionsStageActivity.class);
        if (safeZoneDiameterEditText.length() != 0) {
            intent.putExtra("safeZoneDiameter",
                    Double.parseDouble(safeZoneDiameterEditText.getText().toString()));
        }
        startActivity(intent);
    }
}