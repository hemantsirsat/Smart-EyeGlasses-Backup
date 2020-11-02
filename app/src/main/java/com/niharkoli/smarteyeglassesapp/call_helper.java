package com.niharkoli.smarteyeglassesapp;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class call_helper extends AppCompatActivity {

    ArrayList<String> display = new ArrayList<String>();
    public static final String TAG = "HELPER CLASS";
    private TextToSpeech mTTS;

//    Log.e(TAG, "Name: "+ name);
    TextView text_view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_helper);

        ArrayList<String> similar_names = (ArrayList<String>) getIntent().getSerializableExtra("my_list");

        text_view = findViewById(R.id.text_view);

        for (int i = 0; i < similar_names.size(); i++) {
            display.add(similar_names.get(i) +"\n");
            text_view.setText(display.toString());
        }
            mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
//            String name = similar_names.get(i);
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        int result = mTTS.setLanguage(Locale.ENGLISH);
                        if (result == TextToSpeech.LANG_MISSING_DATA
                                || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.e("TTS", "Language not supported");
                        } else {
                            mTTS.setSpeechRate((float) 0.7);
                            mTTS.speak(similar_names.toString(), TextToSpeech.QUEUE_FLUSH, null);
                            while (mTTS.isSpeaking()){
                                System.out.println("TTS is Still On");
                            }
                            finish();
//                            Log.e("List:    ",name);
                        }
                    } else {
                        Log.e("TTS", "Initialization failed");
                    }
                }
            });

    }

}
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                // Do something after 5s = 5000ms

//            }
//        }, 10000);
