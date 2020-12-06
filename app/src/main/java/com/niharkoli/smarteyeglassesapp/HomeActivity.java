package com.niharkoli.smarteyeglassesapp;

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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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

    private static final String PHONE_CALL = "call";
    private static final String MESSAGE = "message";

    private static final String DATE_ = "date";
    private static final String TIME = "time";
    private static final String WEATHER = "weather";
    private static final String TEMPERATURE = "temperature";

    int check = 0;

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
                Intent p = new Intent(HomeActivity.this, Phone.class);
                startActivity(p);
                break;
            case MESSAGE:
                Intent m = new Intent(HomeActivity.this, Message.class);
                startActivity(m);
                break;
            case DATE_:
                Date date = Calendar.getInstance().getTime();
                DateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, ''yyyy");
                String strDate = dateFormat.format(date);
                if (strDate != null) {
                    speak(strDate);
                }
                break;
            case TIME:
                Date Time = Calendar.getInstance().getTime();
                DateFormat timeFormat = new SimpleDateFormat("h:mm a");
                String strTime = timeFormat.format(Time);
                if (strTime != null) {
                    speak(strTime);
                }
                break;
            case WEATHER:
                check = 1;
                getWeather();
                //Toast.makeText(this,"TEXT MATCHED!",Toast.LENGTH_SHORT).show();
                break;
            case TEMPERATURE:
                check = 2;
                getWeather();
                break;
            default:
                Toast.makeText(this,"NO TEXT MATCHED!",Toast.LENGTH_SHORT).show();
                break;
        }
    }
    public void getWeather() {
        FusedLocationProviderClient fusedLocationProviderClient;

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        Double lat = location.getLatitude();
                        Double lon = location.getLongitude();

                        DownloadTask task = new DownloadTask();
                        try {
                            task.execute("https://api.openweathermap.org/data/2.5/weather?lat=" + String.valueOf(lat) + "&lon=" + String.valueOf(lon) + "&appid=4813d029496e561abbb4524d11129b0a");
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Weather Not Found :( ", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                String result = "";
                URL url = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream in = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;
            } catch (Exception e) {
                Log.e("exception", String.valueOf(e));
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Log.e("get weather", s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                String weatherInfo = jsonObject.getString("weather");
                Log.i("Weather ", weatherInfo);
                String name = jsonObject.getString("name");
                String main1 = jsonObject.getString("main");

                if (check == 1) {
                    JSONArray array = new JSONArray(weatherInfo);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject jsonObject1 = array.getJSONObject(i);
                        String main = jsonObject1.getString("main");
                        String description = jsonObject1.getString("description");

                        if (!main.equals("") && !description.equals("")) {
                            speak(main + " and " + description + " in " + name);
                        }
                    }
                } else if (check == 2) {
                    JSONObject mainPart = new JSONObject(main1);
                    String temperature;
                    Double temperature1 = (Double) mainPart.get("temp");
                    int tempInCelsius = (int) (temperature1 - 273.15);

                    temperature = String.valueOf(tempInCelsius);
                    if (!temperature.equals("")) {
                        speak(temperature+" degree celsius");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Weather Not Found :( ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //tts for date and time
    public void speak(String text) {
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("error", "This Language is not supported");
                    } else {

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