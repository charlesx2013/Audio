package com.example.charlesx.audio;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaRecorder;
import android.os.Bundle;
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

    private void record(boolean isRecording) {
        if (isRecording) {
            startRecording();
        } else {
            stopRecording();
        }
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
        Log.i(LOG_TAG, "Stopped");
    }

    class RecordButton extends Button {

        boolean isRecording = false;

        OnClickListener listener = new OnClickListener() {
            public void onClick(View v) {
                RecordButton button = (RecordButton) v;
                GradientDrawable circle = (GradientDrawable) button.getBackground();
                isRecording = !isRecording;
                record(isRecording);
                if (isRecording) { circle.setColor(Color.parseColor("#24D330")); }
                else { circle.setColor(Color.parseColor("#FF6A6A")); }

            }
        };

        public RecordButton(Context context) {
            super(context);
            setBackgroundResource(R.drawable.circle);
            setLayoutParams(new RelativeLayout.LayoutParams(500, 500));
            setOnClickListener(listener);
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_audio);

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout);
        layout.setGravity(Gravity.CENTER);

//        CircleView v = new CircleView(this);
//        layout.addView(v);

        RecordButton button = new RecordButton(this);
        layout.addView(button);
    }

    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
    }

    public AudioActivity() {
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audio.3gp";
    }

    class CircleView extends View {
        int amp;
        Paint paint = new Paint();
        public void onDraw(Canvas c) {
            if (mRecorder != null) amp = mRecorder.getMaxAmplitude();
            else amp = 0;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor("#88D330"));
            c.drawCircle(c.getWidth()/2, c.getHeight()/2, (float) 250 + amp, paint);

        }

        public CircleView(Context c) {
            super(c);
        }
    }
}
