package com.example.cataractsurgerytrainingapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class CapsulotomyMenuActivity extends AppCompatActivity {
    private EditText capsulotomyDiameterEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capsulotomy_menu);

        capsulotomyDiameterEditText = findViewById(R.id.capsulotomyDiameterEditText);
    }

    public void startCapsulotomyStage(View view) {
//        if (capsulotomyDiameterEditText.length() == 0) {
//            Toast.makeText(this, "All fields must be set to continue.",
//                    Toast.LENGTH_SHORT).show();
//            return;
//        }

        Intent intent = new Intent(this, IncisionsStageActivity.class);
        if (capsulotomyDiameterEditText.length() != 0) {
            intent.putExtra("capsulotomyDiameter",
                    Double.parseDouble(capsulotomyDiameterEditText.getText().toString()));
        }
        startActivity(intent);
    }
}