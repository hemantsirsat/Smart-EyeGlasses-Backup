package com.niharkoli.smarteyeglassesapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class Message extends AppCompatActivity {
    public static final String TAG = "MYJ MESSAGE";
    private TextToSpeech mTTS;
    TextView text_view;
    ArrayList<String> display = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        text_view = findViewById(R.id.text_view);

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

//                                while (mTTS.isSpeaking()){
//                                    System.out.println("TTS is Still On");
//                                }
//                                finish();
//                            Log.e("List:    ",name);
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},1);
        }
        else {
            startListening();
//            finish();
        }

    }




    public void startListening(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Hi Speak Something...");
        try {
            Log.e("Check","Activity Started");
            startActivityForResult(intent,10);
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 10) {
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                Log.e("Check onActivity",result.get(0));
                getcontact(result.get(0));
            }
        }


    }




    private void getcontact(String req_name) {
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        ArrayList<String> similar_names = new ArrayList<String>();

        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            if (name.equalsIgnoreCase(req_name)) {
                message(name , phone);
                break;
            }
            else if (name.toLowerCase().contains(req_name.toLowerCase())) {
                if(!similar_names.contains(name))
                    similar_names.add(name);
            }

        }
        if (similar_names.isEmpty()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Contact Not Found!", Toast.LENGTH_SHORT);
            toast.show();
        }
        else {
//                Intent i = new Intent(this, call_helper.class);
//                i.putExtra("my_list", similar_names);

            text_view = findViewById(R.id.text_view);

            for (int i = 0; i < similar_names.size(); i++) {
                display.add(similar_names.get(i) +"\n");
                text_view.setText(display.toString());
            }
            mTTS.setSpeechRate((float) 0.7);
            mTTS.speak(similar_names.toString(), TextToSpeech.QUEUE_FLUSH, null);


            Runnable r = new Runnable() {
                @Override
                public void run(){
//                    startListening(1);
                    finish();

                }
            };

            Handler h = new Handler();
            h.postDelayed(r, 2000*similar_names.size());

        }


//        Log.e(TAG, "List : "+ similar_names);
    }




    private void getContactList() {
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Log.i(TAG, "Name: " + name);
                        Log.i(TAG, "Phone Number: " + phoneNo);
                    }
                    pCur.close();
                }
            }
        }
        if(cur!=null){
            cur.close();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==1){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startListening();
//                finish();
            }
        }
    }

    private void call_hint(String name) {
        mTTS.speak("Calling "+name, TextToSpeech.QUEUE_FLUSH, null);
    }


    private void message(String name, String phone){


        Intent i = new Intent(this, message_helper.class);
        i.putExtra("phone",phone);
        startActivity(i);
//        if (phone.isEmpty()) {
//            Toast.makeText(getApplicationContext(), "Please Enter Number!", Toast.LENGTH_SHORT).show();
//        } else {
//            if (message.isEmpty()) {
//                Toast.makeText(getApplicationContext(), "Please Message!", Toast.LENGTH_SHORT).show();
//            }
//            else {
//
//                if (ContextCompat.checkSelfPermission(Call_Message.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
//                    ActivityCompat.requestPermissions(Call_Message.this, new String[]{Manifest.permission.SEND_SMS},1);
//                }
//                else
//                {
//                    SmsManager smsManager = SmsManager.getDefault();
//                    smsManager.sendTextMessage(phone,null,message,null,null);
//                    Toast.makeText(getApplicationContext(),  "Message Sent", Toast.LENGTH_SHORT).show();
//
//                }
//            }
//        }
    }
}