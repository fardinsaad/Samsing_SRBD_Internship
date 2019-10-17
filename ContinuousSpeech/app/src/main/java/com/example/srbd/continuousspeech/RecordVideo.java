package com.example.srbd.continuousspeech;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

public class RecordVideo extends AppCompatActivity implements RecognitionListener {

    public static String TAG = "__CS";

    private TextView returned_text, text_view_1;
    //ImageButton btn_record;
    //Button btn_stop;
    //private ProgressBar progress_bar;
    private SpeechRecognizer speech = null;
    private Intent recognizer_intent;
    public static Context context = null;



    static Camera mCamera;
    CameraPreview mPreview;
    static MediaRecorder mMediaRecorder;

    boolean isRecording;
    static TextView camera_text;

    String prev = "";

    boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }


    static Camera getCameraInstance() {
//        Camera c = null;
//        try {
//            c = Camera.open();
//        } catch (Exception e) {
//        }
//        return c;


        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    Log.e(TAG, "Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }

        return cam;
    }



    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    static void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }


    boolean prepareVideoRecorder(int type) {

        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        //mMediaRecorder.setOutputFile(BFF_Utils.getOutputMediaFile(type).toString());

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }


    void RecordVideo(int type) {
        if (prepareVideoRecorder(type)) {
            mMediaRecorder.start();
            Log.d(TAG, "*********  Recording !");

            isRecording = true;

            camera_text.setText("Recording..");
        } else {
            releaseMediaRecorder();
        }
    }


    public static void StopRecording() {
        try {
            mMediaRecorder.stop();
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }

        releaseMediaRecorder(); // release the MediaRecorder object

        mCamera.lock();         // take camera access back from MediaRecorder

        camera_text.setText("stopped");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.bff_video_camera_layout);

        returned_text = (TextView) findViewById(R.id.recorded_text);

        text_view_1 = (TextView) findViewById(R.id.text_view_1);

        //progress_bar = (ProgressBar) findViewById(R.id.progress_bar);
       // btn_record = (ImageButton) findViewById(R.id.btn_record);
        //btn_stop = (Button) findViewById(R.id.btn_stop);
        context = this;


        //progress_bar.setVisibility(View.INVISIBLE);
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


        //prev = getIntent().getExtras().getString("cmd");


//        btn_stop.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View p1) {
//
//                StopRecording();
//
//                new CountDownTimer(2000, 1000) {
//                    public void onTick(long millisUntilFinished) {
//                    }
//
//                    public void onFinish() {
//                        onBackPressed();
//                    }
//
//                }.start();
//            }
//        });
//
//
//        btn_record.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View p1) {
//                progress_bar.setVisibility(View.VISIBLE);
//                speech.startListening(recognizer_intent);
//            }
//        });

        isRecording = false;

        if (checkCameraHardware(this)) {


            mCamera = getCameraInstance();
            mCamera.setDisplayOrientation(90);
            mPreview = new CameraPreview(this, mCamera);

            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);

            camera_text = findViewById(R.id.camera_text);

            Handler handler = new Handler();

            handler.postDelayed(() -> {
                RecordVideo(2);
            }, 500);

        }

    }


    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onBeginningOfSpeech() {
        //progress_bar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
    }

    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "End of Speech");
       // progress_bar.setVisibility(View.INVISIBLE);
        //btn_record.setEnabled(true);
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.d(TAG, "FAILED " + errorMessage);
       // progress_bar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        ArrayList<String> matches = arg0.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";

        text = matches.get(0);

        Log.d(TAG, "OnPartialResults : " + text);

        returned_text.setText(text);
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.d(TAG, "Ready For Speech");
    }


    @Override
    public void onResults(Bundle results) {

        Log.d(TAG, "Results : " + returned_text.getText());

        CharSequence ss = returned_text.getText();

        String str = String.valueOf(ss).toLowerCase();


    }

    @Override
    public void onRmsChanged(float rmsdB) {
        //progress_bar.setProgress((int) rmsdB);
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