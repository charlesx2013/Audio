package com.example.charlesx.audio;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.io.IOException;


public class AudioActivity extends Activity {

    private static final String LOG_TAG = "AudioTest";
    private static String mFileName = null;

    private MediaRecorder mRecorder = null;
    private RecordButton button;

    private static final int GREEN_COLOR = Color.parseColor("#24D330");
    private static final int RED_COLOR = Color.parseColor("#FF6A6A");

    private Handler handler = new Handler();
    private Thread animation = null;

    private RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(500, 500);


    private void record(boolean isRecording) {
        if (isRecording) {
            startRecording();
            animation = (new Thread( new Runnable() {
                @Override
                public void run() {
                    while (button.isRecording) {
                        try {
                            Thread.sleep(100);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (Thread.interrupted()) { return; }
                                    changeAnimation();
                                }
                            });
                        } catch (Exception e) {
                            Log.i(LOG_TAG, "Interrupted");
                            return;
                        }
                    }
                }
            }));
            animation.start();
        } else {
            stopRecording();
            animation.interrupt();
            animation = null;
            reset();
        }
    }

    private void changeAnimation() {
        int level = mRecorder == null ? 0 : mRecorder.getMaxAmplitude() / 100;
        View v = findViewById(R.id.animation);
        RelativeLayout.LayoutParams newParams = new RelativeLayout.LayoutParams(500 + level, 500 + level);
        newParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        v.setLayoutParams(newParams);
        v.invalidate();
    }

    private void reset() {
        View v = findViewById(R.id.animation);
        v.setLayoutParams(params);
        v.invalidate();
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.i(LOG_TAG, "StartRecording Failed");
        }

        mRecorder.start();
        Log.i(LOG_TAG, "Started Recording");

    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.reset();
        mRecorder.release();
        mRecorder = null;
        Log.i(LOG_TAG, "Stopped Recording");
    }

    class RecordButton extends Button {

        boolean isRecording = false;

        OnClickListener listener = new OnClickListener() {
            public void onClick(View v) {
                isRecording = !isRecording;
                record(isRecording);
                if (isRecording) { setCircleColor(v,GREEN_COLOR); }
                else { setCircleColor(v, RED_COLOR); }

            }
        };

        public RecordButton(Context context) {
            super(context);
            setBackgroundResource(R.drawable.circle);
            setLayoutParams(params);
            setOnClickListener(listener);
        }
    }

    private void setCircleColor(View v, int color) {
        RecordButton button = (RecordButton) v;
        GradientDrawable circle = (GradientDrawable) button.getBackground();
        circle.setColor(color);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_audio);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout);
        layout.setGravity(Gravity.CENTER);

        View v = new View(this);
        v.setBackgroundResource(R.drawable.circle_animation);
        v.setLayoutParams(params);
        v.setId(R.id.animation);
        layout.addView(v);

        button = new RecordButton(this);
        layout.addView(button);
    }

    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
            setCircleColor(button, RED_COLOR);
            button.isRecording = false;
            reset();
        }
    }

    public AudioActivity() {
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audio.3gp";
    }
}
