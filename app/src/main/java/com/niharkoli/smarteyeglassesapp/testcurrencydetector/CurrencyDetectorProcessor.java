package com.niharkoli.smarteyeglassesapp.testcurrencydetector;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.FirebaseMLException;
import com.niharkoli.smarteyeglassesapp.common.BitmapUtils;
import com.niharkoli.smarteyeglassesapp.common.CameraImageGraphic;
import com.niharkoli.smarteyeglassesapp.common.FrameMetadata;
import com.niharkoli.smarteyeglassesapp.common.GraphicOverlay;
import com.niharkoli.smarteyeglassesapp.common.VisionImageProcessor;


import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.List;

import static android.content.ContentValues.TAG;

public class CurrencyDetectorProcessor implements VisionImageProcessor {

    private final CurrencyImageClassifier classifier;
    private final Reference<Activity> activityRef;

    public CurrencyDetectorProcessor(Activity activity) throws FirebaseMLException {
        activityRef = new WeakReference<>(activity);
        classifier = new CurrencyImageClassifier(activity.getApplicationContext());
    }


    @Override
    public void process(ByteBuffer data, FrameMetadata frameMetadata, GraphicOverlay graphicOverlay) throws FirebaseMLException {

        final Activity activity = activityRef.get();
        if (activity == null) {
            return;
        }

        classifier
                .classifyFrame(data, frameMetadata.getWidth(), frameMetadata.getHeight())
                .addOnSuccessListener(
                        activity,
                        new OnSuccessListener<List<String>>() {
                            @Override
                            public void onSuccess(List<String> result) {
                                CurrencyGraphic currencyGraphic = new CurrencyGraphic(graphicOverlay,
                                        result);
                                Bitmap bitmap = BitmapUtils.getBitmap(data, frameMetadata);
                                CameraImageGraphic imageGraphic =
                                        new CameraImageGraphic(graphicOverlay, bitmap);
                                graphicOverlay.clear();
                                graphicOverlay.add(imageGraphic);
                                graphicOverlay.add(currencyGraphic);
                                graphicOverlay.postInvalidate();
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "Currency classifier failed: " + e);
                                e.printStackTrace();
                            }
                        });

    }

    @Override
    public void process(Bitmap bitmap, GraphicOverlay graphicOverlay) {

    }

    @Override
    public void stop() {

    }
}
