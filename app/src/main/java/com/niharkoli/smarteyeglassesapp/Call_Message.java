package com.niharkoli.smarteyeglassesapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class Call_Message extends AppCompatActivity {

    EditText etNumber;
    EditText etMessage;
    ImageButton btCall;
    ImageButton btMsg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call__message);

        etNumber = findViewById(R.id.et_number);
        etMessage = findViewById(R.id.et_msg);
        btCall = findViewById(R.id.bt_call);
        btMsg = findViewById(R.id.bt_msg);


        btCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = etNumber.getText().toString();
                if (phone.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please Enter Number!", Toast.LENGTH_SHORT).show();
                } else {
                    String s = "tel:" + phone;
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse(s));

                    if (ContextCompat.checkSelfPermission(Call_Message.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(Call_Message.this, new String[]{Manifest.permission.CALL_PHONE},1);
                    }
                    else
                    {
                        startActivity(intent);
                    }
                }
            }
        });

        btMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = etNumber.getText().toString();
                String message = etMessage.getText().toString();

                if (phone.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please Enter Number!", Toast.LENGTH_SHORT).show();
                } else {
                    if (message.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Please Message!", Toast.LENGTH_SHORT).show();
                    }
                    else {

                        if (ContextCompat.checkSelfPermission(Call_Message.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(Call_Message.this, new String[]{Manifest.permission.SEND_SMS},1);
                        }
                        else
                        {
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(phone,null,message,null,null);
                            Toast.makeText(getApplicationContext(),  "Message Sent", Toast.LENGTH_SHORT).show();

                        }
                    }
                }

            }
        });
    }
}