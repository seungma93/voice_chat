package com.example.seungma.rtsptest2;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {


    public MediaPlayer mPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        String audioUrl = "rtsp://211.219.108.237:1935/live/myStream";

        try {
            mPlayer = new MediaPlayer();

            mPlayer.setDataSource(audioUrl);
            mPlayer.prepare();

            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            mPlayer.start();

        }catch (IOException E){
            E.printStackTrace();
        }


    }
}
