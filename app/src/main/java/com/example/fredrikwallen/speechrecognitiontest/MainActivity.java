package com.example.fredrikwallen.speechrecognitiontest;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {



    private SpeechRecognizer speechRecognizer;
    private HotwordDetectionThread hotwordDetectionThread;


    private TextView recognized_text;

    private static final String TAG = "SpeechRecTest";
    private static final String RECOGNITION_LANGUAGE = "sv-SE";

    private static final int MICROPHONE_PERMISSION_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(MainActivity.this);
        speechRecognizer.setRecognitionListener(recListener);

        recognized_text = (TextView)findViewById(R.id.recognized_text);
        runTimePermissions();
        LocalBroadcastManager.getInstance(this).registerReceiver(hotwordDetectedReciever,new IntentFilter(HotwordDetectionThread.BROADCAST_TAG));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(hotwordDetectedReciever);
        speechRecognizer.destroy();
    }

    private void runTimePermissions(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MICROPHONE_PERMISSION_REQUEST);
        }
        else{
            startHotwordDetectorIfNotStarted();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == MICROPHONE_PERMISSION_REQUEST){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startHotwordDetectorIfNotStarted();
            }
        }
    }




    private void startHotwordDetectorIfNotStarted(){
        hotwordDetectionThread = new HotwordDetectionThread(getApplicationContext());
        hotwordDetectionThread.start();

    }


    BroadcastReceiver hotwordDetectedReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "hej hotwordDetectedReciever");

            if(HotwordDetectionThread.threadActive) {
                hotwordDetectionThread.cancel();
            }

            Intent recIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            recIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, RECOGNITION_LANGUAGE);
            recIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, MainActivity.this.getPackageName());
            speechRecognizer.startListening(recIntent);
        }
    };

    private RecognitionListener recListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            Log.d(TAG, "hej onReadyForSpeech");
        }

        @Override
        public void onBeginningOfSpeech() {
            Log.d(TAG, "hej onBeginningOfSpeech");
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            //Log.d(TAG, "hej onRmsChanged");

        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            Log.d(TAG, "hej onBufferReceived");
        }

        @Override
        public void onEndOfSpeech() {
            Log.d(TAG, "hej onEndOfSpeech");


        }

        @Override
        public void onError(int error) {
            Log.d(TAG, "hej onError: " + error);

            speechRecognizer.cancel();
            startHotwordDetectorIfNotStarted();
        }

        @Override
        public void onResults(Bundle results) {
            Log.d(TAG, "hej onResults");

            String str = "";
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            str += data.get(0);
            /*for (int i = 0; i < data.size(); i++)
            {
                str += data.get(i);
            }*/
            recognized_text.setText("");
            recognized_text.setText(str);

            speechRecognizer.cancel();
            startHotwordDetectorIfNotStarted();



        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    };

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return super.onRetainCustomNonConfigurationInstance();
    }
}
