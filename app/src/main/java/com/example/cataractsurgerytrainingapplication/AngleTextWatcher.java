package com.example.cataractsurgerytrainingapplication;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

public class AngleTextWatcher implements TextWatcher {

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        try {
            int angle = Integer.parseInt(s.toString());
            if (angle < 0 || angle > 359) {
                s.replace(0, s.length(), Integer.toString(angle % 360));
            }
        } catch (NumberFormatException ex) {
            s.clear();
        }
    }
}
