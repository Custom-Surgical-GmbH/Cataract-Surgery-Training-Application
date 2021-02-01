package com.example.cataractsurgerytrainingapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

public class IncisionsMenuActivity extends AppCompatActivity {
    private EditText firstIncisionLengthEditText;
    private EditText firstIncisionAngleEditText;
    private EditText secondIncisionLengthEditText;
    private EditText secondIncisionAngleEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incisions_menu);

        firstIncisionLengthEditText = findViewById(R.id.firstIncisionLengthEditText);
        firstIncisionAngleEditText = findViewById(R.id.firstIncisionAngleEditText);
        secondIncisionLengthEditText = findViewById(R.id.secondIncisionLengthEditText);
        secondIncisionAngleEditText = findViewById(R.id.secondIncisionAngleEditText);

        firstIncisionAngleEditText.addTextChangedListener(new AngleTextWatcher());
        secondIncisionAngleEditText.addTextChangedListener(new AngleTextWatcher());
    }

    public void startIncisionsStage(View view) {
//        if (
//            firstIncisionLengthEditText.length() == 0 ||
//            firstIncisionAngleEditText.length() == 0 ||
//            secondIncisionLengthEditText.length() == 0 ||
//            secondIncisionAngleEditText.length() == 0
//        ) {
//            Toast.makeText(this, "All fields must be set to continue.",
//                    Toast.LENGTH_SHORT).show();
//            return;
//        }

        Intent intent = new Intent(this, IncisionsStageActivity.class);
        if (firstIncisionLengthEditText.length() != 0) { 
                intent.putExtra("firstIncisionLength",
                        Double.parseDouble(firstIncisionLengthEditText.getText().toString()));
        }
        if (firstIncisionAngleEditText.length() != 0) {         
                intent.putExtra("firstIncisionAngle",
                        Double.parseDouble(firstIncisionAngleEditText.getText().toString()));
        }
        if (secondIncisionLengthEditText.length() != 0) {         
                intent.putExtra("secondIncisionLength",
                        Double.parseDouble(secondIncisionLengthEditText.getText().toString()));
        }
        if (secondIncisionAngleEditText.length() != 0) {         
                intent.putExtra("secondIncisionAngle",
                        Double.parseDouble(secondIncisionAngleEditText.getText().toString()));
        }
        startActivity(intent);
    }
}