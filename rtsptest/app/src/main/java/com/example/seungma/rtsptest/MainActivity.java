package com.example.seungma.rtsptest;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.wowza.gocoder.sdk.api.WowzaGoCoder;
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcast;
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcastConfig;
import com.wowza.gocoder.sdk.api.configuration.WOWZMediaConfig;
import com.wowza.gocoder.sdk.api.devices.WOWZAudioDevice;
import com.wowza.gocoder.sdk.api.devices.WOWZCamera;
import com.wowza.gocoder.sdk.api.devices.WOWZCameraView;
import com.wowza.gocoder.sdk.api.errors.WOWZError;
import com.wowza.gocoder.sdk.api.errors.WOWZStreamingError;
import com.wowza.gocoder.sdk.api.logging.WOWZLog;
import com.wowza.gocoder.sdk.api.status.WOWZState;
import com.wowza.gocoder.sdk.api.status.WOWZStatus;
import com.wowza.gocoder.sdk.api.status.WOWZStatusCallback;

import java.io.IOException;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, WOWZStatusCallback {

    public Context context;

    // The top-level GoCoder API interface
    private WowzaGoCoder goCoder;

    // The GoCoder SDK audio device
    private WOWZAudioDevice goCoderAudioDevice;

    // The GoCoder SDK broadcaster
    private WOWZBroadcast goCoderBroadcaster;

    // The broadcast configuration settings
    private WOWZBroadcastConfig goCoderBroadcastConfig;

    public MediaPlayer mPlayer;

    public String audioUrl = "rtsp://211.219.108.237:1935/live/myStream";




    // Properties needed for Android 6+ permissions handling
    private static final int PERMISSIONS_REQUEST_CODE = 0x1;
    private boolean mPermissionsGranted = true;
    private String[] mRequiredPermissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        // Initialize the GoCoder SDK
        goCoder = WowzaGoCoder.init(getApplicationContext(), "GOSK-9045-010F-F59F-A36B-CF66");

        if (goCoder == null) {
            // If initialization failed, retrieve the last error and display it
            WOWZError goCoderInitError = WowzaGoCoder.getLastError();
            Toast.makeText(this,
                    "GoCoder SDK error: " + goCoderInitError.getErrorDescription(),
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Create an audio device instance for capturing and broadcasting audio
        goCoderAudioDevice = new WOWZAudioDevice();

        // Create a broadcaster instance
        goCoderBroadcaster = new WOWZBroadcast();

        // Create a configuration instance for the broadcaster
        goCoderBroadcastConfig = new WOWZBroadcastConfig();

        // Set the connection properties for the target Wowza Streaming Engine server or Wowza Streaming Cloud live stream
        goCoderBroadcastConfig.setHostAddress("211.219.108.237");
        goCoderBroadcastConfig.setPortNumber(1935);
        goCoderBroadcastConfig.setApplicationName("live");
        goCoderBroadcastConfig.setStreamName("myStream");

        //비디오 죽이기
        goCoderBroadcastConfig.setVideoEnabled(false);
        // Designate the audio device as the audio broadcaster
        goCoderBroadcastConfig.setAudioBroadcaster(goCoderAudioDevice);

        // Associate the onClick() method as the callback for the broadcast button's click event
        Button broadcastButton = (Button) findViewById(R.id.button);
        broadcastButton.setOnClickListener(this);
    }

    //
    // Called when an activity is brought to the foreground
    //
    @Override
    protected void onResume() {
        super.onResume();

        // If running on Android 6 (Marshmallow) or above, check to see if the necessary permissions
        // have been granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mPermissionsGranted = hasPermissions(this, mRequiredPermissions);
            if (!mPermissionsGranted)
                ActivityCompat.requestPermissions(this, mRequiredPermissions, PERMISSIONS_REQUEST_CODE);
        } else
            mPermissionsGranted = true;

    }

    //
    // Callback invoked in response to a call to ActivityCompat.requestPermissions() to interpret
    // the results of the permissions request
    //
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        mPermissionsGranted = true;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                // Check the result of each permission granted
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        mPermissionsGranted = false;
                    }
                }
            }
        }
    }

    //
    // Utility method to check the status of a permissions request for an array of permission identifiers
    //
    private static boolean hasPermissions(Context context, String[] permissions) {
        for (String permission : permissions)
            if (context.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                return false;

        return true;
    }

    //
    // The callback invoked when the broadcast button is tapped
    //
    @Override
    public void onClick(View view) {
        // return if the user hasn't granted the app the necessary permissions
        if (!mPermissionsGranted) return;

        // Ensure the minimum set of configuration settings have been specified necessary to
        // initiate a broadcast streaming session
        WOWZStreamingError configValidationError = goCoderBroadcastConfig.validateForBroadcast();

        if (configValidationError != null) {
            Toast.makeText(this, "오류" + configValidationError.getErrorDescription(), Toast.LENGTH_LONG).show();
        } else if (goCoderBroadcaster.getStatus().isRunning()) {
            // Stop the broadcast that is currently running
            goCoderBroadcaster.endBroadcast(this);
        } else {
            // Start streaming


            goCoderBroadcaster.startBroadcast(goCoderBroadcastConfig, this);


            Toast.makeText(this, "성공", Toast.LENGTH_LONG).show();


            try{

                mPlayer = new MediaPlayer();

                mPlayer.setDataSource(audioUrl);
                mPlayer.prepare();

                mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mPlayer.start();

            }catch (IOException e){
                e.printStackTrace();
            }




        }


    }

    @Override
    public void onWZStatus(final WOWZStatus goCoderStatus) {
        // A successful status transition has been reported by the GoCoder SDK
        final StringBuffer statusMessage = new StringBuffer("Broadcast status: ");

        switch (goCoderStatus.getState()) {
            case WOWZState.STARTING:
                statusMessage.append("Broadcast initialization");
                break;

            case WOWZState.READY:
                statusMessage.append("Ready to begin streaming");
                break;

            case WOWZState.RUNNING:
                statusMessage.append("Streaming is active");
                break;

            case WOWZState.STOPPING:
                statusMessage.append("Broadcast shutting down");
                break;

            case WOWZState.IDLE:
                statusMessage.append("The broadcast is stopped");
                break;

            default:
                return;
        }


        //
        // The callback invoked when an error occurs during a broadcast
        //


        //
        // Enable Android's immersive, sticky full-screen mode
        //


    }

    @Override
    public void onWZError(final WOWZStatus goCoderStatus) {
        // If an error is reported by the GoCoder SDK, display a message
        // containing the error details using the U/I thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,
                        "Streaming error: " + goCoderStatus.getLastError().getErrorDescription(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }


}




