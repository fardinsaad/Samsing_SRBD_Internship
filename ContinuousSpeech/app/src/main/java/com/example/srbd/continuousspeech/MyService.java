package com.example.srbd.continuousspeech;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;


public class MyService extends Service implements RecognitionListener {

    public String TAG = "__CS";

    private SpeechRecognizer speech = null;
    private Intent recognizer_intent;
    public static Context context = null;

    String full_text = "";

    String str = "";

    String prev = "";

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {

        context = this;

        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        recognizer_intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizer_intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "en");
        recognizer_intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());
        recognizer_intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizer_intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        /*
        Minimum time to listen in millis. Here 5 seconds
         */
        recognizer_intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 5000);
        recognizer_intent.putExtra("android.speech.extra.DICTATION_MODE", true);

        speech.startListening(recognizer_intent);
    }


    public void IdentifyPerson() {

    }

    public void ReciteDateTime() {

    }


    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
    }

    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "End of Speech");



    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);

        Log.d(TAG, "FAILED " + errorMessage);

        //if(errorMessage.equals("No speech input"))
        //{
        if (errorCode == SpeechRecognizer.ERROR_RECOGNIZER_BUSY)
        {
            new CountDownTimer(2000, 50) {
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    speech.startListening(recognizer_intent);
                }

            }.start();
            str = " ";
            return;
        }
        new CountDownTimer(100, 50) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                speech.startListening(recognizer_intent);
            }

        }.start();
        str = " ";
        //}


    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        ArrayList<String> matches = arg0.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";

        text = matches.get(0);

        //Log.d(TAG, "OnPartialResults : " + text);

        str = text;

    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.d(TAG, "Ready For Speech");
    }


    @Override
    public void onResults(Bundle results) {

        Log.d(TAG, "Results : " + str);

        str = str.toLowerCase();

        if (str.equals("start")) {
            Intent intent = new Intent(MyService.this, RecordVideo.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (str.equals("stop")) {
            RecordVideo.StopRecording();
        }

        //str = "";

        if (str.equals("bff")) {
            Log.d(TAG, "Full Result : " + full_text);

            MainActivity.txt_view.setText(full_text);
            full_text = "";
            str = " ";
        }

        full_text += " " + str;

        str = " ";
        new CountDownTimer(300, 50) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                speech.startListening(recognizer_intent);
            }

        }.start();
    }

    @Override
    public void onRmsChanged(float rmsdB) {
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }
}