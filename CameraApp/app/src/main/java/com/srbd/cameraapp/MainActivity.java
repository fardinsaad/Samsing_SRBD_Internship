package com.srbd.cameraapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "__CS";
    public static Context context = null;
    static Camera mCamera;
    CameraPreview mPreview;
    static MediaRecorder mMediaRecorder;
    Button btn;
    String PATH_TO_FILE;

    boolean isRecording,working;

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    static Camera getCameraInstance() {


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
       // mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);



        // Step 2.1:  ****Setting Video Size****


      //  Toast.makeText(context, "After Viedo tweeked ", Toast.LENGTH_SHORT).show();



        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
       /* mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setVideoSize(720,480);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
*/

        // Step 4: Set output file
          mMediaRecorder.setOutputFile(PATH_TO_FILE);

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
            Toast.makeText(context, "Media Recorder Preparation failed! ", Toast.LENGTH_SHORT).show();
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


        } else {
            releaseMediaRecorder();
        }
    }


    public static void StopRecording() {
        try {
            mMediaRecorder.stop();
            Log.d(TAG, "*********  Stopped Recording !");
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }

        releaseMediaRecorder(); // release the MediaRecorder object

        mCamera.lock();         // take camera access back from MediaRecorder

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        String[] PERMISSIONS = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
        }
        context = this;
        btn = (Button) findViewById(R.id.btn);
       // working = checkCameraHardware(this);
        mCamera = getCameraInstance();

        mCamera.setDisplayOrientation(180);
        mPreview = new CameraPreview(context, mCamera);

        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        isRecording = false;
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyFolder/";
        File dir = new File(path);
        if(!dir.exists())
            dir.mkdirs();
         PATH_TO_FILE = path + "filename" + ".mp4";


        //On Clicking button it will start recording

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isRecording) {
                    // stop recording and release camera
                 /*   mMediaRecorder.stop();  // stop the recording
                    releaseMediaRecorder(); // release the MediaRecorder object
                    mCamera.lock(); */        // take camera access back from MediaRecorder

                    // inform the user that recording has stopped
                    StopRecording();
                    isRecording = false;
                    btn.setText("Start Recording");

                } else {
                    if (checkCameraHardware(context) && isRecording == false) {

                        btn.setText("Stop Recording");
                        //isRecording = true;
                        Handler handler = new Handler();

//                        new CountDownTimer(2000, 1000) {
//                            public void onTick(long millisUntilFinished) {
//                            }
//
//                            public void onFinish() {
//                                RecordVideo(2);
//                            }
//
//                        }.start();

                        handler.postDelayed(() -> {
                            RecordVideo(2);
                        }, 2000);

                    }else {
                        releaseMediaRecorder();
                        Log.d(TAG, "onClick: not available");
                    }

                }
            }
        });

    }
}
