package com.niharkoli.smarteyeglassesapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private static final String FACE_DETECTION = "face detect";
    private static final String OBJECT_DETECTION = "object detect";
    private static final String CURRENCY_DETECTION = "currency detect";
    private static final String BARCODE_READER = "barcode reader";
    private static final String TEXT_DETECTION = "text detect";
    private static final String LABEL_DETECTION = "label detect";
    private static final String FACE_CONTOUR = "face contour";

    private static final String PHONE_CALL = "call";
    private static final String MESSAGE = "message";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

    }

    public void recognize(View view) {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Hi Speak Something...");
        try {
            startActivityForResult(intent,1000);
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000) {
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                recognizeModule(result.get(0));
            }
        }
    }

    private void recognizeModule(String speechResult){
        Intent intent = new Intent(HomeActivity.this,MainActivity.class);
        Intent intent2 = new Intent(HomeActivity.this,Call_Message.class);
        switch(speechResult){
            case FACE_DETECTION:
                intent.putExtra("ModuleName",FACE_DETECTION);
                startActivity(intent);
                break;
            case OBJECT_DETECTION:
                intent.putExtra("ModuleName",OBJECT_DETECTION);
                startActivity(intent);
                break;
            case TEXT_DETECTION:
                intent.putExtra("ModuleName",TEXT_DETECTION);
                startActivity(intent);
                break;
            case LABEL_DETECTION:
                intent.putExtra("ModuleName",LABEL_DETECTION);
                startActivity(intent);
                break;
            case BARCODE_READER:
                intent.putExtra("ModuleName",BARCODE_READER);
                startActivity(intent);
                break;
            case CURRENCY_DETECTION:
                intent.putExtra("ModuleName",CURRENCY_DETECTION);
                startActivity(intent);
                break;
            case FACE_CONTOUR:
                intent.putExtra("ModuleName",FACE_CONTOUR);
                startActivity(intent);
                break;

            case PHONE_CALL:
                intent2.putExtra("ModuleName",PHONE_CALL);
                startActivity(intent2);
                break;

            case MESSAGE:
                intent2.putExtra("ModuleName",MESSAGE);
                startActivity(intent2);
                break;
            default:
                Toast.makeText(this,"NO TEXT MATCHED!",Toast.LENGTH_SHORT).show();
                break;
        }
    }
}