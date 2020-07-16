// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.niharkoli.smarteyeglassesapp.currencydetector;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.niharkoli.smarteyeglassesapp.MainActivity;
import com.niharkoli.smarteyeglassesapp.SettingsActivity;
import com.niharkoli.smarteyeglassesapp.common.GraphicOverlay;

import java.util.List;
import java.util.Locale;

/**
 * Graphic instance for rendering a label within an associated graphic overlay view.
 */
public class CurrencyLabelGraphic extends GraphicOverlay.Graphic {

    private final Paint textPaint;
    private final GraphicOverlay overlay;

    TextToSpeech tts;
    String text;

    private final List<FirebaseVisionImageLabel> labels;

    public CurrencyLabelGraphic(GraphicOverlay overlay, List<FirebaseVisionImageLabel> labels) {
        super(overlay);
        this.overlay = overlay;
        this.labels = labels;
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(60.0f);
    }

    @Override
    public synchronized void draw(Canvas canvas) {
        float x = overlay.getWidth() / 4.0f;
        float y = overlay.getHeight() / 2.0f;

        for (FirebaseVisionImageLabel label : labels) {
            if (label.getConfidence() > 0.85) {
                canvas.drawText(label.getText(), x, y, textPaint);
                canvas.drawText(String.valueOf(label.getConfidence()), x + 150, y, textPaint);
                y = y - 62.0f;

                text = label.getText();
                tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            int result = tts.setLanguage(Locale.US);
                            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Log.e("error", "This Language is not supported");
                            } else {
                                if ("".equals(text)) {
                                    text = "Please enter some text to speak.";
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    //tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                                } else {
                                   // tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                                }
                            }
                        } else {
                            Log.e("error", "Failed to Initialize");
                        }
                    }
                });
            }
        }
    }
}
