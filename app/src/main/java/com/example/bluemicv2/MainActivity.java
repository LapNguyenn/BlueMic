package com.example.bluemicv2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.media.AudioTrack;



public class MainActivity extends AppCompatActivity {

    //=========== Permission
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int MICROPHONE_REQUEST_CODE = 200;
    private boolean permissionGranted = false;

    //=========== Record
    private AudioRecord audioRecord;
    private AudioTrack audioTrack;
    private int intBufferSize;
    private short[] shortsAudioData;
    private boolean isRecording = false;
    private int intGain = 2;
    private Thread thread;
    //=========== Widget
    private ImageButton btnStartVoice;
    private ImageButton btnStopVoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Check permission
        if (!(ContextCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED)) {
            //If not be granted, request permission again
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MICROPHONE_REQUEST_CODE);
        }

        //Click start voice button
        btnStartVoice = findViewById(R.id.btn_start_voice);
        btnStartVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(ContextCompat.checkSelfPermission(MainActivity.this, permissions[0]) == PackageManager.PERMISSION_GRANTED)) {
                    //If not be granted, request permission again
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, MICROPHONE_REQUEST_CODE);
                }else{
                    startVoice();
                }
            }
        });

        //Click stop voice button
        btnStopVoice = findViewById(R.id.btn_stop_voice);
        btnStopVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopVoice();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MICROPHONE_REQUEST_CODE)
            permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }

    private void startVoice() {
        if(!isRecording) {
            isRecording = true;
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    threadLoop();
                }
            });
            thread.start();
            Toast.makeText(MainActivity.this, "Mic đã bật, có thể bắt dầu nói", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopVoice() {
        if (isRecording) {
            isRecording = false;
            try {
                // Wait for thread to stop
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Stop audio
            audioTrack.stop();
            audioRecord.stop();
            // Reset variable to null
            audioRecord.release();
            audioTrack.release();
            audioRecord = null;
            audioTrack = null;
        }
        Toast.makeText(MainActivity.this, "Mic đã tắt", Toast.LENGTH_SHORT).show();
    }

    private void threadLoop() {
        //Audio quality
        int intRecordSampleRate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);
        //Record length
        intBufferSize = AudioRecord.getMinBufferSize(intRecordSampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        shortsAudioData = new short[intBufferSize];
        //Start a short record
        if(ContextCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED){
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    intRecordSampleRate,
                    AudioFormat.CHANNEL_IN_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    intBufferSize);

            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    intRecordSampleRate,
                    AudioFormat.CHANNEL_IN_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    intBufferSize,
                    AudioTrack.MODE_STREAM);


            audioTrack.setPlaybackRate(intRecordSampleRate);
            audioRecord.startRecording();
            audioTrack.play();
            //play short record
            while (isRecording){
                audioRecord.read(shortsAudioData, 0, shortsAudioData.length);
                for(int i = 0; i < shortsAudioData.length; i++){
                    shortsAudioData[i] = (short) Math.min(shortsAudioData[i]*intGain, Short.MAX_VALUE);
                }
                audioTrack.write(shortsAudioData, 0, shortsAudioData.length);
            }
        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MICROPHONE_REQUEST_CODE);
            threadLoop();
        }

    }
}