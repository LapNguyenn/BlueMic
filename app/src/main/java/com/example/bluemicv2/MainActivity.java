package com.example.bluemicv2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.EnvironmentalReverb;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
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
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int MICROPHONE_REQUEST_CODE = 200;
    private boolean permissionGranted = false;

    //=========== Record
    private AudioRecord audioRecord;
    private AudioTrack audioTrack;
    private int intBufferSize;
    private short[] shortsAudioData;
    private short[] processedShortAudioData;
    private boolean isRecording = false;
    //=========== Modify output sound
    double amplitude  = 1;
    double pitchFactor = 1;
    private EnvironmentalReverb environmentalReverb;
    private PresetReverb presetReverb;
    private PresetReverb.Settings presetReverbSettings;
    private Equalizer equalizer;
    private Thread thread;
    //=========== Widget
    private ImageButton btnStartVoice;
    private ImageButton btnStopVoice;

    private SeekBar seekBar_amplitude;
    private SeekBar seekBar_pitchFactor;

    private TextView tv_amplitude;
    private TextView tv_pitchFactor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Check permission
        if (!(ContextCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED)) {
            //If not be granted, request permission again
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MICROPHONE_REQUEST_CODE);
        }

        seekBar_amplitude = findViewById(R.id.seekBar_amplitude);
        seekBar_pitchFactor = findViewById(R.id.seekBar_pitchFactor);
        tv_amplitude = findViewById(R.id.tv_amplitude);
        tv_pitchFactor = findViewById(R.id.tv_pitchFactor);

        seekBar_amplitude.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                amplitude = (double) seekBar_amplitude.getProgress()/100;
                tv_amplitude.setText("Amplitude: " + amplitude);
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
                pitchFactor = (double) seekBar_pitchFactor.getProgress() /100;
                tv_pitchFactor.setText("PitchFactor: " + pitchFactor);
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
        processedShortAudioData = new short[intBufferSize];

        //Start a short record
        if(ContextCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED){
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION,
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
                // Reduce amplitude (biên độ)
                for(int i = 0; i < processedShortAudioData.length; i++){
                    processedShortAudioData[i] = (short) Math.min(shortsAudioData[i]*amplitude, Short.MAX_VALUE);
                }
                // Increase pitch (tần số)
                for (int i = 0; i < processedShortAudioData.length; i++) {
                    int newIndex = (int) (i / pitchFactor);
                    if (newIndex < shortsAudioData.length) {
                        processedShortAudioData[newIndex] = (short) Math.min(shortsAudioData[newIndex] + shortsAudioData[i], Short.MAX_VALUE);
                    }
                }
                for (int i = 0; i < processedShortAudioData.length; i++) {
                    processedShortAudioData[i] = (short) Math.max(processedShortAudioData[i] - shortsAudioData[i], Short.MIN_VALUE);
                }

                audioTrack.write(processedShortAudioData, 0, processedShortAudioData.length);
            }
        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MICROPHONE_REQUEST_CODE);
            threadLoop();
        }
    }
}