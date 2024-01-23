package com.example.bluemicv2;

import android.media.AudioManager;
import android.util.Log;
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.GainProcessor;
import be.tarsos.dsp.PitchShifter;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.android.AndroidAudioPlayer;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.resample.RateTransposer;
import be.tarsos.dsp.resample.Resampler;

public class MyAudioProcessor{
    private float amplitude = 1.5f;
    private float pitchFactor = 2f;
    private float rateTransposeFactor = 0.5f;
    float [] buffer;
    PitchShifter pitchShifter;
    AndroidAudioPlayer player;

    private AudioDispatcher dispatcher = null;
    public MyAudioProcessor(){
    }
    public void startProcessing() {
        if(this.dispatcher != null && !dispatcher.isStopped()){
            this.dispatcher.stop();
            this.dispatcher = null;
        }
        try {
            int SAMPLE_RATE = 44100;
            int BUFFER_SIZE = 4096;
            int OVERLAP = BUFFER_SIZE * 3 / 4;
            Log.d("TEST", "SAMPLE_RATE " + SAMPLE_RATE + " | BUFFER_SIZE " + BUFFER_SIZE);
            // ========================================================================
            // ************************************************************************
                dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(SAMPLE_RATE, BUFFER_SIZE, OVERLAP);
                pitchShifter = new PitchShifter(pitchFactor, SAMPLE_RATE, BUFFER_SIZE, OVERLAP);
                dispatcher.addAudioProcessor(new AudioProcessor() {
                    @Override
                    public boolean process(AudioEvent audioEvent) {
                        buffer = audioEvent.getFloatBuffer();
                        return true;
                    }

                    @Override
                    public void processingFinished() {

                    }
                });
                dispatcher.addAudioProcessor(pitchShifter);
                dispatcher.addAudioProcessor(new AudioProcessor() {
                    final Resampler r = new Resampler(false, 0.1, 4.0);
                    @Override
                    public boolean process(AudioEvent audioEvent) {
                        float factor = pitchFactor;
                        float[] src = audioEvent.getFloatBuffer();
                        float[] out = new float[(int) ((BUFFER_SIZE - OVERLAP)*factor)];
                        r.process(factor, src, OVERLAP, BUFFER_SIZE- OVERLAP, false, out, 0, out.length);
                        dispatcher.setStepSizeAndOverlap(out.length, 0);
                        audioEvent.setFloatBuffer(out);
                        audioEvent.setOverlap(0);
                        return true;
                    }
                    @Override
                    public void processingFinished() {
                    }
                });
            dispatcher.addAudioProcessor(new RateTransposer(this.rateTransposeFactor));
            dispatcher.addAudioProcessor(new GainProcessor(this.amplitude));
            player = new AndroidAudioPlayer(new TarsosDSPAudioFormat(
                    SAMPLE_RATE,
                    1024,
                    1,
                    true,
                    false
            ), 32, AudioManager.STREAM_VOICE_CALL);

            dispatcher.addAudioProcessor(player);
            dispatcher.addAudioProcessor(new AudioProcessor() {
                @Override
                public boolean process(AudioEvent audioEvent) {
                    dispatcher.setStepSizeAndOverlap(BUFFER_SIZE, OVERLAP);
                    dispatcher.setAudioFloatBuffer(buffer);
                    audioEvent.setFloatBuffer(buffer);
                    audioEvent.setOverlap(OVERLAP);
                    return true;
                }

                @Override
                public void processingFinished() {

                }
            });
            if(dispatcher != null && !dispatcher.isStopped()){
                new Thread(dispatcher).start();
            }
            // ************************************************************************
            // ========================================================================
        } catch (Exception e) {
            Log.e("MyAudioProcessor", "Error during audio processing: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void stopProcessing(){
        if(this.dispatcher != null && !dispatcher.isStopped()){
            this.dispatcher.stop();
            this.dispatcher = null;
        }
    }

    public float getAmplitude() {
        return amplitude;
    }

    public float getPitchFactor() {
        return pitchFactor;
    }
    public void setAmplitude(float amplitude) {
        this.amplitude = amplitude;
        startProcessing();
    }

    public void setPitchFactor(float pitchFactor) {
        this.pitchFactor = pitchFactor;
        startProcessing();
    }

    public void setRateTransposerFactor(float rateTransposeFactor) {
        this.rateTransposeFactor = rateTransposeFactor;
        startProcessing();
    }
}
