package com.example.bluemicv2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.media.AudioTrack;


public class MainActivity extends AppCompatActivity {

    //=========== Permission
    private final String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int MICROPHONE_REQUEST_CODE = 200;

    //=========== Record
    private boolean isRecording = false;
    //=========== Modify output sound

    //=========== Widget
    protected ImageButton btnStartVoice;
    protected ImageButton btnStopVoice;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyAudioProcessor chibiProcess = new MyAudioProcessor();
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
                    if(!isRecording) {
                        isRecording = true;
                        chibiProcess.startProcessing();
                        Toast.makeText(MainActivity.this, "Mic đã bật, có thể bắt dầu nói", Toast.LENGTH_SHORT).show();
                        btnStartVoice.setVisibility(View.GONE);
                        btnStopVoice.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        //Click stop voice button
        btnStopVoice = findViewById(R.id.btn_stop_voice);
        btnStopVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    isRecording = false;
                    chibiProcess.stopProcessing();
                    btnStartVoice.setVisibility(View.VISIBLE);
                    btnStopVoice.setVisibility(View.GONE);
                }
                Toast.makeText(MainActivity.this, "Mic đã tắt", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == MICROPHONE_REQUEST_CODE);
    }



}
