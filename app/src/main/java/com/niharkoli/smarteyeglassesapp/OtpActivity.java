package com.niharkoli.smarteyeglassesapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class OtpActivity extends AppCompatActivity {

    private String verificationId;
    private FirebaseAuth mAuth;
    private TextInputEditText codeEditText;
    private TextView otpMessage;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        mAuth = FirebaseAuth.getInstance();
        codeEditText = findViewById(R.id.OTPEditText1);
        otpMessage = findViewById(R.id.OTPMessage);

        phoneNumber = getIntent().getStringExtra("phoneNumber");
        String message = "Enter the OTP send to <b>"+phoneNumber+"</b>";
        otpMessage.setText(Html.fromHtml(message));
        Log.e("Intent", "Phone Number");
        Log.e("Working", "" + phoneNumber);


        sendVerificationCode(phoneNumber);

    }

    private void verifyCode(String code){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Intent intent = new Intent(OtpActivity.this, InfoActivity.class);
                    intent.putExtra("phoneNumber", phoneNumber);
                    Log.e("Phone Number", phoneNumber);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(OtpActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void sendVerificationCode(String number){

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        Log.e("Code","Code Send");

    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
            Log.e("Code","" + s);

        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code!=null){
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(OtpActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    public void checkOTP(View view){

        String codeEntered = codeEditText.getText().toString().trim();
        if (codeEntered.isEmpty() || codeEntered.length() < 6){
            codeEditText.setError("Enter Code");
            codeEditText.requestFocus();
            return;
        }

        verifyCode(codeEntered);
        Log.e("CHECKOTP", "Function");
    }
}
