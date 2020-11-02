package com.niharkoli.smarteyeglassesapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class message_helper extends AppCompatActivity {
    public static final Integer RecordAudioRequestCode = 1;
    private SpeechRecognizer speechRecognizer;
    private EditText editText;
    private ImageView micButton;
    private Button copy;
    private ArrayList<String> data_list = new ArrayList<String>();
    private TextToSpeech mTTS;


    StringBuffer sBuffer1=new StringBuffer("");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_helper);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }

        String phone = getIntent().getExtras().getString("phone");

        editText = findViewById(R.id.text);
        micButton = findViewById(R.id.button);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(Locale.ENGLISH);
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    } else {

                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });



        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {



                sBuffer1.delete(0, sBuffer1.length());
                sBuffer1.append(editText.getText().toString());
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                micButton.setImageResource(R.drawable.ic_mic);
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (data.get(0).toString().equalsIgnoreCase("send message"))
                {
                    send_message(phone,sBuffer1.toString());
                    finish();
                }
                if (data.get(0).toString().equalsIgnoreCase("cancel message"))
                {
                    finish();
                }
                if (data.get(0).toString().equalsIgnoreCase("enter"))
                {
                    sBuffer1.append("\n");
                }
                else if (data.get(0).toString().equalsIgnoreCase("Review Message"))
                {
                    review();
                    return;
                }



                else if (data.get(0).toString().equalsIgnoreCase("Delete Message"))
                {
                    sBuffer1.delete(0, sBuffer1.length());
                    editText.setText("");
                    delete();
                    return;
                }
                else
                {
                    sBuffer1.append(data.get(0));
                    sBuffer1.append(" ");
                }
//                data_list.add(data.get(0));
////                display.clear();
//                for (int i=0; i<data_list.size();i++){
//
////                    display.add(data_list.get(i)+" ");
//                }
                editText.setText(sBuffer1.toString());
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        micButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    speechRecognizer.stopListening();
                    ended();
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    speak();
                    micButton.setImageResource(R.drawable.ic_mic);
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
                return false;
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},RecordAudioRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RecordAudioRequestCode && grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
        }
    }


    private void speak() {
        String text = new String("Please Add");
        mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }
    private void ended() {
        String text = new String("Ended");
        mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }
    private void review() {

        mTTS.speak("Reviewing Message", TextToSpeech.QUEUE_FLUSH, null);
        String text = editText.getText().toString();
        mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }
    private void delete() {
        String text = new String("Message Deleted");
        mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void send_message(String phone, String message){
        if (phone.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please Enter Number!", Toast.LENGTH_SHORT).show();
        } else {
            if (message.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please Message!", Toast.LENGTH_SHORT).show();
            }
            else {

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS},1);
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
    }
