package com.niharkoli.smarteyeglassesapp.automl;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.automl.FirebaseAutoMLLocalModel;
import com.google.firebase.ml.vision.automl.FirebaseAutoMLRemoteModel;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions;
import com.niharkoli.smarteyeglassesapp.R;
import com.niharkoli.smarteyeglassesapp.common.CameraImageGraphic;
import com.niharkoli.smarteyeglassesapp.common.FrameMetadata;
import com.niharkoli.smarteyeglassesapp.common.GraphicOverlay;
import com.niharkoli.smarteyeglassesapp.common.PreferenceUtils;
import com.niharkoli.smarteyeglassesapp.common.VisionProcessorBase;
import com.niharkoli.smarteyeglassesapp.currencydetector.CurrencyGraphic;
import com.niharkoli.smarteyeglassesapp.imagelabeling.LabelGraphic;


import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * AutoML image labeler Demo.
 */
public class AutoMLImageLabelerProcessor
    extends VisionProcessorBase<List<FirebaseVisionImageLabel>> {

  private static final String TAG = "ODAutoMLILProcessor";

  private final Context context;
  private FirebaseVisionImageLabeler detector;
  private Task<Void> modelDownloadingTask;


  /**
   * The detection mode of the processor. Different modes will have different behavior on whether or
   * not waiting for the model download complete.
   */


  public AutoMLImageLabelerProcessor(Context context) throws FirebaseMLException {
    this.context = context;

      Log.d(TAG, "Local model used.");
      FirebaseAutoMLLocalModel localModel =
          new FirebaseAutoMLLocalModel.Builder().setAssetFilePath("automl/manifest.json").build();
      detector =
          FirebaseVision.getInstance()
              .getOnDeviceAutoMLImageLabeler(
                  new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(localModel)
                      .setConfidenceThreshold(0)
                      .build());
      modelDownloadingTask = null;

  }

  @Override
  public void stop() {
    try {
      detector.close();
    } catch (IOException e) {
      Log.e(TAG, "Exception thrown while trying to close the image labeler", e);
    }
  }

  @Override
  protected Task<List<FirebaseVisionImageLabel>> detectInImage(final FirebaseVisionImage image) {
    if (modelDownloadingTask == null) {
      // No download task means only the locally bundled model is used. Model can be used directly.
      return detector.processImage(image);
    } else {
      return processImageOnDownloadComplete(image);
    }
  }

  @Override
  protected void onSuccess(
      @Nullable Bitmap originalCameraImage,
      @NonNull List<FirebaseVisionImageLabel> labels,
      @NonNull FrameMetadata frameMetadata,
      @NonNull GraphicOverlay graphicOverlay) {
    graphicOverlay.clear();
    if (originalCameraImage != null) {
      CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay, originalCameraImage);
      graphicOverlay.add(imageGraphic);
    }

    CurrencyLabelGraphic labelGraphic = new CurrencyLabelGraphic(graphicOverlay, labels);
    graphicOverlay.add(labelGraphic);
    graphicOverlay.postInvalidate();
  }

  @Override
  protected void onFailure(@NonNull Exception e) {
    Log.w(TAG, "Label detection failed.", e);
  }

  private Task<List<FirebaseVisionImageLabel>> processImageOnDownloadComplete(
      FirebaseVisionImage image) {
    if (modelDownloadingTask.isSuccessful()) {
      return detector.processImage(image);
    } else {
      String downloadingError = "Error downloading remote model.";
      Log.e(TAG, downloadingError, modelDownloadingTask.getException());
      Toast.makeText(context, downloadingError, Toast.LENGTH_SHORT).show();
      return Tasks.forException(
          new Exception("Failed to download remote model.", modelDownloadingTask.getException()));
    }
  }
}
