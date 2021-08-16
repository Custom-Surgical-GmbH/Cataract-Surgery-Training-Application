package com.example.cataractsurgerytrainingapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class CapsulotomyProjectorPrototypeMenuActivity extends AppCompatActivity {
    private EditText capsulotomyDiameterEditText;
    private EditText projectorViewDiameterEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capsulotomy_projector_prototype_menu);

        capsulotomyDiameterEditText = findViewById(R.id.capsulotomyDiameterEditText);
        projectorViewDiameterEditText = findViewById(R.id.projectorViewDiameterEditText);
    }

    public void startCapsulotomyStage(View view) {
//        if (capsulotomyDiameterEditText.length() == 0) {
//            Toast.makeText(this, "All fields must be set to continue.",
//                    Toast.LENGTH_SHORT).show();
//            return;
//        }

        Intent intent = new Intent(this, CapsulotomyProjectorPrototypeStageActivity.class);
        if (capsulotomyDiameterEditText.length() != 0) {
            intent.putExtra("capsulotomyDiameter",
                    Double.parseDouble(capsulotomyDiameterEditText.getText().toString()));
        }
        if (projectorViewDiameterEditText.length() != 0) {
            intent.putExtra("projectorViewDiameter",
                    Double.parseDouble(projectorViewDiameterEditText.getText().toString()));
        }
        startActivity(intent);
    }
}