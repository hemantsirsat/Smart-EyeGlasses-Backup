package com.niharkoli.smarteyeglassesapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class HomeActivity extends AppCompatActivity {
    TextToSpeech tts;

    private FusedLocationProviderClient fusedLocationClient;

    private static final String FACE_DETECTION = "face detect";
    private static final String OBJECT_DETECTION = "object detect";
    private static final String CURRENCY_DETECTION = "currency detect";
    private static final String BARCODE_READER = "barcode reader";
    private static final String TEXT_DETECTION = "text detect";
    private static final String LABEL_DETECTION = "label detect";
    private static final String FACE_CONTOUR = "face contour";
    private static final String DATE_ = "date";
    private static final String TIME = "time";
    private static final String LOCATION = "location";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public class DownloadTask extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... strings) {
            try{
                String result = "";
                URL url = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream in = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while(data != -1){
                    char current =(char) data;
                    result += current;
                    data = reader.read();
                }
                return result;
            }catch (Exception e) {
                e.printStackTrace();
                return null;
            }
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
            case DATE_:
                Date date = Calendar.getInstance().getTime();
                DateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, ''yyyy");
                String strDate = dateFormat.format(date);
                if(strDate!=null) {
                    date_time(strDate);
                }
                break;
            case TIME:
                Date Time = Calendar.getInstance().getTime();
                DateFormat timeFormat = new SimpleDateFormat("h:mm a");
                String strTime = timeFormat.format(Time);
                if(strTime!=null) {
                    date_time(strTime);
                }
                break;
            case LOCATION:
                getlocation();
                //Toast.makeText(this,"TEXT MATCHED!",Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this,"NO TEXT MATCHED!",Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void getlocation(){
        FusedLocationProviderClient fusedLocationProviderClient;

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
            {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location!=null) {
                        Double lat = location.getLatitude();
                        Double lon = location.getLongitude();
                        //date_time(String.valueOf(lat));

                    }
                }
            });
            }
            else
            {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44);
            }
    }



    //tts for date and time
    public void date_time(String text){
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("error", "This Language is not supported");
                    }else{

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                        } else {
                            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }
                } else {
                    Log.e("error", "Failed to Initialize");
                }
            }
        });
    }
}
