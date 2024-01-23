package com.example.bluemicv2;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.util.Log;

import java.time.LocalDate;
import java.util.concurrent.ExecutionException;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.GainProcessor;
import be.tarsos.dsp.PitchShifter;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.android.AndroidAudioPlayer;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;

public class MyAudioProcessor{
    private float amplitude;
    private float pitchFactor;
    private AudioProcessor pitchShifter, gainProcessor;
    protected AudioProcessor audioTrackProcessor;
    private int SAMPLE_RATE;
    private int BUFFER_SIZE;
    private int OVERLAP;

    private AudioDispatcher dispatcher = null;
    private AndroidAudioPlayer player;

    public MyAudioProcessor(float amplitude, float pitchFactor, int SAMPLE_RATE, int BUFFER_SIZE, int OVERLAP){
        this.amplitude = amplitude;
        this.pitchFactor = pitchFactor;
        this.SAMPLE_RATE = SAMPLE_RATE;
        this.BUFFER_SIZE = BUFFER_SIZE;
        this.OVERLAP = OVERLAP;
    }
    public void setAmplitude(float amplitude) {
        this.amplitude = amplitude;
        startProcessing();
    }

    public void setPitchFactor(float pitchFactor) {
        this.pitchFactor = pitchFactor;
        startProcessing();
    }

    public void setSAMPLE_RATE(int SAMPLE_RATE) {
        this.SAMPLE_RATE = SAMPLE_RATE;
    }

    public void setBUFFER_SIZE(int BUFFER_SIZE) {
        this.BUFFER_SIZE = BUFFER_SIZE;
    }

    public void setOVERLAP(int OVERLAP) {
        this.OVERLAP = OVERLAP;
    }

    public void startProcessing() {
        if(dispatcher != null){
            dispatcher.stop();
        }
        try {
            Log.d("TEST", "SAMPLE_RATE " + this.SAMPLE_RATE + " | BUFFER_SIZE " + this.BUFFER_SIZE);
            this.player = new AndroidAudioPlayer(new TarsosDSPAudioFormat(
                    this.SAMPLE_RATE,
                    512,
                    1,
                    true,
                    false
            ), 64, AudioManager.STREAM_VOICE_CALL);
            this.pitchShifter = new PitchShifter(this.pitchFactor, this.SAMPLE_RATE, this.BUFFER_SIZE, this.OVERLAP);
            this.gainProcessor = new GainProcessor(this.amplitude);
            //Start record
            this.dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(this.SAMPLE_RATE, this.BUFFER_SIZE, this.OVERLAP);
            //this.dispatcher.addAudioProcessor(this.pitchShifter);
            this.dispatcher.addAudioProcessor(this.gainProcessor);
            // Play record
            this.dispatcher.addAudioProcessor(player);
            new Thread(this.dispatcher).start();
        } catch (Exception e) {
            Log.e("MyAudioProcessor", "Error during audio processing: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void stopProcessing(){
        if(this.dispatcher != null){
            this.dispatcher.stop();
        }
    }
}
