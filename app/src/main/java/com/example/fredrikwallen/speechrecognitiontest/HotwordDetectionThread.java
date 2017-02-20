package com.example.fredrikwallen.speechrecognitiontest;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import ai.kitt.snowboy.SnowboyDetect;


public class HotwordDetectionThread extends Thread {

    private boolean threadActive = false;

    private AudioRecord ar = null;

    private Context context;

    private static final String TAG = "SpeechRecTest";
    public static final String BROADCAST_TAG = "HotwordDetected";
    private static final int SAMPLE_RATE = 16000;
    private static final int LISTENING_PERIOD_FRAMES = 2000;



    public HotwordDetectionThread(Context context){
        this.context = context;
    }

    @Override
    public void run() {
        threadActive = true;

        String modelPath = context.getFilesDir().getAbsolutePath()+"/"+ "snowboy.umdl";
        String commonPath = context.getFilesDir().getAbsolutePath()+"/"+ "common.res";

        SnowboyDetect snowboyDetector = new SnowboyDetect(commonPath, modelPath);
        snowboyDetector.SetSensitivity("0.5");         // Sensitivity for each hotword
        snowboyDetector.SetAudioGain(2.0f);              // Audio gain for detection*/

        int minSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        ar = new AudioRecord(MediaRecorder.AudioSource.MIC, snowboyDetector.SampleRate() ,AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minSize);
        if (ar.getState() != AudioRecord.STATE_INITIALIZED) {
            throw new RuntimeException("AudioRecord initialization failed");
        }
        ar.startRecording();


        short[] audioData = new short[LISTENING_PERIOD_FRAMES];
        while(threadActive) {
            ar.read(audioData, 0, LISTENING_PERIOD_FRAMES);

            int result = snowboyDetector.RunDetection(audioData, audioData.length);
            if(result > 0) {
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(HotwordDetectionThread.BROADCAST_TAG));
            }
        }
    }

    public void cancel(){
        threadActive = false;
        if(ar != null) {
            ar.stop();
            ar.release();
            ar = null;
        }
        this.interrupt();
    }
}
