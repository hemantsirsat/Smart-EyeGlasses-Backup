package com.niharkoli.smarteyeglassesapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.speech.tts.SynthesisCallback;
import android.speech.tts.SynthesisRequest;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeechService;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions;
import com.niharkoli.smarteyeglassesapp.barcodescanning.BarcodeScanningProcessor;
import com.niharkoli.smarteyeglassesapp.common.CameraSource;
import com.niharkoli.smarteyeglassesapp.common.CameraSourcePreview;
import com.niharkoli.smarteyeglassesapp.common.GraphicOverlay;
import com.niharkoli.smarteyeglassesapp.facedetection.FaceContourDetectorProcessor;
import com.niharkoli.smarteyeglassesapp.facedetection.FaceDetectionProcessor;
import com.niharkoli.smarteyeglassesapp.imagelabeling.ImageLabelingProcessor;
import com.niharkoli.smarteyeglassesapp.objectdetection.ObjectDetectorProcessor;
import com.niharkoli.smarteyeglassesapp.textrecognition.TextRecognitionProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback,
        AdapterView.OnItemSelectedListener,
        CompoundButton.OnCheckedChangeListener{

    private static final String FACE_DETECTION = "Face Detection";
    private static final String OBJECT_DETECTION = "Object Detection";
    private static final String AUTOML_IMAGE_LABELING = "AutoML Vision Edge";
    private static final String TEXT_DETECTION = "Text Detection";
    private static final String BARCODE_DETECTION = "Barcode Detection";
    private static final String IMAGE_LABEL_DETECTION = "Label Detection";
    private static final String CLASSIFICATION_QUANT = "Classification (quantized)";
    private static final String CLASSIFICATION_FLOAT = "Classification (float)";
    private static final String FACE_CONTOUR = "Face Contour";
    private static final String TAG = "LivePreviewActivity";
    private static final int PERMISSION_REQUESTS = 1;

    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    private String selectedModel = FACE_CONTOUR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preview = findViewById(R.id.firePreview);
        if (preview == null) {
            Log.d(TAG, "Preview is null");
        }
        graphicOverlay = findViewById(R.id.fireFaceOverlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }

        Spinner spinner = findViewById(R.id.spinner);
        List<String> options = new ArrayList<>();
        options.add(TEXT_DETECTION);
        options.add(OBJECT_DETECTION);
        options.add(IMAGE_LABEL_DETECTION);
        options.add(FACE_DETECTION);
        options.add(FACE_CONTOUR);
        options.add(BARCODE_DETECTION);

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_style,
                options);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(this);

        ToggleButton facingSwitch = findViewById(R.id.facingSwitch);
        facingSwitch.setOnCheckedChangeListener(this);
        // Hide the toggle button if there is only 1 camera
        if (Camera.getNumberOfCameras() == 1) {
            facingSwitch.setVisibility(View.GONE);
        }

        if (allPermissionsGranted()) {
            createCameraSource(selectedModel);
        } else {
            getRuntimePermissions();
        }

    }

    @Override
    public synchronized void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        selectedModel = parent.getItemAtPosition(pos).toString();
        Log.d(TAG, "Selected model: " + selectedModel);
        preview.stop();
        if (allPermissionsGranted()) {
            createCameraSource(selectedModel);
            startCameraSource();
        } else {
            getRuntimePermissions();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing.
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "Set facing");
        if (cameraSource != null) {
            if (isChecked) {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
            } else {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
            }
        }
        preview.stop();
        startCameraSource();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.live_preview_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {

            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra(SettingsActivity.EXTRA_LAUNCH_SOURCE, SettingsActivity.LaunchSource.LIVE_PREVIEW);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void createCameraSource(String model) {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
        }

        try {
            switch (model) {
                case TEXT_DETECTION:
                    Log.i(TAG, "Using Text Detector Processor");
                    cameraSource.setMachineLearningFrameProcessor(new TextRecognitionProcessor());
                    break;

                case OBJECT_DETECTION:
                    Log.i(TAG, "Using Object Detector Processor");
                    FirebaseVisionObjectDetectorOptions objectDetectorOptions =
                            new FirebaseVisionObjectDetectorOptions.Builder()
                                    .setDetectorMode(FirebaseVisionObjectDetectorOptions.STREAM_MODE)
                                    .enableClassification().build();
                    cameraSource.setMachineLearningFrameProcessor(
                            new ObjectDetectorProcessor(objectDetectorOptions));
                    break;
                case IMAGE_LABEL_DETECTION:
                    Log.i(TAG, "Using Image Label Detector Processor");
                    cameraSource.setMachineLearningFrameProcessor(new ImageLabelingProcessor());
                    break;
                case FACE_DETECTION:
                    Log.i(TAG, "Using Face Detector Processor");
                    cameraSource.setMachineLearningFrameProcessor(new FaceDetectionProcessor(getResources()));
                    break;
                case FACE_CONTOUR:
                    Log.i(TAG, "Using Face Contour Detector Processor");
                    cameraSource.setMachineLearningFrameProcessor(new FaceContourDetectorProcessor());
                    break;
                case BARCODE_DETECTION:
                    Log.i(TAG, "Using Barcode Detector Processor");
                    cameraSource.setMachineLearningFrameProcessor(new BarcodeScanningProcessor());
                    break;
                default:
                    Log.e(TAG, "Unknown model: " + model);
            }
        } catch (Exception e) {
            Log.e(TAG, "Can not create image processor: " + model, e);
            Toast.makeText(
                    getApplicationContext(),
                    "Can not create image processor: " + e.getMessage(),
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "Permission granted!");
        if (allPermissionsGranted()) {
            createCameraSource(selectedModel);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }

}
