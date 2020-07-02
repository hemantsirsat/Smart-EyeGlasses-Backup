package com.niharkoli.smarteyeglassesapp.currencydetector;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseCustomLocalModel;
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelInterpreterOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class CurrencyImageClassifier {

    private static final String MODEL_NAME = "mobilenet_quant_v2_1.0_299";


    private static final int RESULTS_TO_SHOW = 3;

    private static final String LABEL_PATH = "labels.txt";

    /**
     * Dimensions of inputs.
     */
    private static final int DIM_BATCH_SIZE = 1;

    private static final int DIM_PIXEL_SIZE = 3;

    private static final int DIM_IMG_SIZE_X = 299;
    private static final int DIM_IMG_SIZE_Y = 299;
    private static final int QUANT_NUM_OF_BYTES_PER_CHANNEL = 1;
    private static final int FLOAT_NUM_OF_BYTES_PER_CHANNEL = 4;
    private static final String TAG = "Currency Image Classifier";

    /* Preallocated buffers for storing image data in. */
    private final int[] intValues = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y];

    /**
     * An instance of the driver class to run model inference with Firebase.
     */
    private FirebaseModelInterpreter interpreter;

    /**
     * Data configuration of input & output data of model.
     */
    private final FirebaseModelInputOutputOptions dataOptions;


    /**
     * Labels corresponding to the output of the vision model.
     **/

    private List<String> labelList;


    private ByteBuffer imgData;
    private final PriorityQueue<Map.Entry<String, Float>> sortedLabels =
            new PriorityQueue<>(
                    RESULTS_TO_SHOW,
                    new Comparator<Map.Entry<String, Float>>() {
                        @Override
                        public int compare(Map.Entry<String, Float> o1,
                                           Map.Entry<String, Float> o2) {
                            return (o1.getValue()).compareTo(o2.getValue());
                        }
                    });


    @SuppressLint("LongLogTag")
    CurrencyImageClassifier(final Context context) throws FirebaseMLException {
        FirebaseCustomLocalModel localModel = new FirebaseCustomLocalModel.Builder()
                .setAssetFilePath("currency.tflite")
                .build();


        FirebaseModelInterpreterOptions interpreterOptions =
                new FirebaseModelInterpreterOptions.Builder(localModel).build();
        try {
            interpreter =
                    FirebaseModelInterpreter.getInstance(interpreterOptions);
        } catch (FirebaseMLException e) {
            Log.e(TAG, "Failed to build FirebaseModelInterpreter. ", e);
        }

        labelList = loadLabelList(context.getApplicationContext());

        /*Log.d(TAG, "Created a Custom Image Classifier.");
        int[] inputDims = {DIM_BATCH_SIZE, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y, DIM_PIXEL_SIZE};
        int[] outputDims = {1, labelList.size()};

        int dataType = FirebaseModelDataType.BYTE;

        dataOptions =
                new FirebaseModelInputOutputOptions.Builder()
                        .setInputFormat(0, dataType, inputDims)
                        .setOutputFormat(0, dataType, outputDims)
                        .build();
        Log.d(TAG, "Configured input & output data for the custom image classifier.");

         */
         dataOptions =
                new FirebaseModelInputOutputOptions.Builder()
                        .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 224, 224, 3})
                        .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 5})
                        .build();
    }


    @SuppressLint("LongLogTag")
    Task<List<String>> classifyFrame(ByteBuffer buffer, int width, int height)
            throws FirebaseMLException {
        if (interpreter == null) {
            Log.e(TAG, "Image classifier has not been initialized; Skipped.");
            List<String> uninitialized = new ArrayList<>();
            uninitialized.add("Uninitialized Classifier.");
            Tasks.forResult(uninitialized);
        }
        // Create input data.
        convertBitmapToByteBuffer(buffer, width, height);

        FirebaseModelInputs inputs = new FirebaseModelInputs.Builder().add(this.imgData).build();
        // Here's where the magic happens!!
        return interpreter
                .run(inputs, dataOptions)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to get labels array: " + e.getMessage());
                        e.printStackTrace();
                    }
                })
                .continueWith(
                        new Continuation<FirebaseModelOutputs, List<String>>() {
                            @Override
                            public List<String> then(@NonNull Task<FirebaseModelOutputs> task) throws Exception {
                                    byte[][] labelProbArray =
                                            task.getResult().<byte[][]>getOutput(0);
                                    return getTopLabels(labelProbArray);

                            }
                        });
    }


    @SuppressLint("LongLogTag")
    private synchronized void convertBitmapToByteBuffer(
            ByteBuffer buffer, int width, int height) {
        int bytesPerChannel = QUANT_NUM_OF_BYTES_PER_CHANNEL;
        if (this.imgData == null) {
            this.imgData = ByteBuffer.allocateDirect(bytesPerChannel * DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE);
        } else {
            this.imgData.clear();
        }

        this.imgData.order(ByteOrder.nativeOrder());
        Bitmap bitmap = createResizedBitmap(buffer, width, height);
        this.imgData.rewind();
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(),
                bitmap.getHeight());
        // Convert the image to int points.
        int pixel = 0;
        long startTime = SystemClock.uptimeMillis();
        for (int i = 0; i < DIM_IMG_SIZE_X; ++i) {
            for (int j = 0; j < DIM_IMG_SIZE_Y; ++j) {
                final int val = intValues[pixel++];
                // Normalize the values according to the model used:
                // Quantized model expects a [0, 255] scale while a float model expects [0, 1].

                    this.imgData.put((byte) ((val >> 16) & 0xFF));
                    this.imgData.put((byte) ((val >> 8) & 0xFF));
                    this.imgData.put((byte) (val & 0xFF));

            }
        }
        long endTime = SystemClock.uptimeMillis();
        Log.d(TAG, "Timecost to put values into ByteBuffer: " + (endTime - startTime));
    }

    /**
     * Resizes image data from {@code ByteBuffer}.
     */
    private Bitmap createResizedBitmap(ByteBuffer buffer, int width, int height) {
        YuvImage img = new YuvImage(buffer.array(), ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        img.compressToJpeg(new Rect(0, 0, img.getWidth(), img.getHeight()), 50, out);
        byte[] imageBytes = out.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        return Bitmap.createScaledBitmap(bitmap, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y, true);
    }

    private synchronized List<String> getTopLabels(byte[][] labelProbArray) {
        for (int i = 0; i < labelList.size(); ++i) {
            sortedLabels.add(
                    new AbstractMap.SimpleEntry<>(labelList.get(i),
                            (labelProbArray[0][i] & 0xff) / 255.0f));
            if (sortedLabels.size() > RESULTS_TO_SHOW) {
                sortedLabels.poll();
            }
        }
        return getTopKLabels();
    }

    @SuppressLint("LongLogTag")
    private List<String> loadLabelList(Context context) {
        List<String> labelList = new ArrayList<>();
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(context.getAssets().open(LABEL_PATH)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                labelList.add(line);
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to read label list.", e);
        }
        return labelList;
    }

    private synchronized List<String> getTopKLabels() {
        List<String> result = new ArrayList<>();
        final int size = sortedLabels.size();
        for (int i = 0; i < size; ++i) {
            Map.Entry<String, Float> label = sortedLabels.poll();
            result.add(label.getKey() + ":" + label.getValue());
        }
        return result;
    }
}
