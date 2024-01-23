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
    private float amplitude  = 1f;
    private float pitchFactor = 1.86f;
    protected int intRecordSampleRate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_VOICE_CALL);
    protected int intBufferSize = AudioRecord.getMinBufferSize(intRecordSampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
    protected int intOverlap = intBufferSize/4;
    //=========== Widget
    protected ImageButton btnStartVoice;
    protected ImageButton btnStopVoice;

    private SeekBar seekBar_amplitude;
    private SeekBar seekBar_pitchFactor;

    private TextView tv_amplitude;
    private TextView tv_pitchFactor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyAudioProcessor chibiProcess = new MyAudioProcessor(amplitude, pitchFactor, intRecordSampleRate, intBufferSize, intOverlap);
        // Check permission
        if (!(ContextCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED)) {
            //If not be granted, request permission again
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MICROPHONE_REQUEST_CODE);
        }

        seekBar_amplitude = findViewById(R.id.seekBar_amplitude);
        seekBar_pitchFactor = findViewById(R.id.seekBar_pitchFactor);
        tv_amplitude = findViewById(R.id.tv_amplitude);
        tv_pitchFactor = findViewById(R.id.tv_pitchFactor);
        tv_amplitude.setText("Amplitude: " + String.format("%.1f", amplitude));
        tv_pitchFactor.setText("pitchFactor: " + String.format("%.1f", pitchFactor));
        seekBar_amplitude.setProgress((int)amplitude*100);
        seekBar_pitchFactor.setProgress((int) pitchFactor*100);

        seekBar_amplitude.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                amplitude = (float) seekBar_amplitude.getProgress()/100;
                tv_amplitude.setText("Amplitude: " + amplitude);
                chibiProcess.setAmplitude(amplitude);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBar_pitchFactor.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pitchFactor = (float) seekBar_pitchFactor.getProgress() /100;
                tv_pitchFactor.setText("PitchFactor: " + pitchFactor);
                if(chibiProcess != null){
                    chibiProcess.setPitchFactor(pitchFactor);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

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
