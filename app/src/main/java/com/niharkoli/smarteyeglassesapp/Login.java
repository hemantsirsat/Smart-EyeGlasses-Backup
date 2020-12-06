package com.niharkoli.smarteyeglassesapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {
    private TextInputEditText editTextPhoneNumber;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null) {
            goToMainActivity();
        }
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean validatePhoneNumber(String phoneNumber) {
        if (phoneNumber.isEmpty() || phoneNumber.length() < 10 || phoneNumber.length() > 10) {
            editTextPhoneNumber.setError("Valid Number is Required");
            editTextPhoneNumber.requestFocus();
            return false;
        }
        return true;
    }

    public void sendOTP(View view) {
        editTextPhoneNumber = findViewById(R.id.loginEditText);
        String phoneNumber = editTextPhoneNumber.getText().toString();
        if (validatePhoneNumber(phoneNumber)) {
            phoneNumber = "+91" + phoneNumber;
            Log.e("Working", "" + phoneNumber);
            Intent intent = new Intent(this, OtpActivity.class);
            intent.putExtra("phoneNumber", phoneNumber);
            startActivity(intent);
        }
    }
}
