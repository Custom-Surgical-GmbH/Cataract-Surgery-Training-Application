package com.example.cataractsurgerytrainingapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class ToricIOLPositioningMenuActivity extends AppCompatActivity {
    private EditText lensAxisAngleEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toric_iol_positioning_menu);

        lensAxisAngleEditText = findViewById(R.id.lensAxisAngleEditText);

        lensAxisAngleEditText.addTextChangedListener(new AngleTextWatcher());
    }

    public void startToricIOLPositioningStage(View view) {
//        if (lensAxisAngleEditText.length() == 0) {
//            Toast.makeText(this, "All fields must be set to continue.",
//                    Toast.LENGTH_SHORT).show();
//            return;
//        }

        Intent intent = new Intent(this, ToricIOLPositioningStageActivity.class);
        if (lensAxisAngleEditText.length() != 0) {
            intent.putExtra("lensAxisAngle",
                    Double.parseDouble(lensAxisAngleEditText.getText().toString()));
        }
        startActivity(intent);
    }
}