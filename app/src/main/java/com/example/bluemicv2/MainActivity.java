package com.example.bluemicv2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.slider.Slider;
public class MainActivity extends AppCompatActivity {

    //=========== Permission
    private final String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int MICROPHONE_REQUEST_CODE = 200;

    //=========== Record checking
    private boolean isRecording = false;
    //=========== Widget
    private ImageButton btnStartVoice, btnStopVoice;
    private Slider gainSlider;
    private TextView tvGain;
    @SuppressLint("UseSwitchCompatOrMaterialCode") private Switch switchChibiEffect;
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
        // Get widget
        btnStartVoice = findViewById(R.id.btn_start_voice);
        btnStopVoice = findViewById(R.id.btn_stop_voice);
        gainSlider = findViewById(R.id.slider_gain);
        tvGain = findViewById(R.id.tv_gain);
        switchChibiEffect = findViewById(R.id.switch_chibi_effect);
        // get memory
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        if (preferences.contains("amplitude")) {
            float savedAmplitude = preferences.getFloat("amplitude", chibiProcess.getAmplitude());
            chibiProcess.setAmplitude(savedAmplitude);
        }
        boolean savedSwitchStatus = preferences.getBoolean("switchChibiEffect", false);
        if(savedSwitchStatus){
            chibiProcess.setPitchFactor(2.0f);
        }
        switchChibiEffect.setChecked(savedSwitchStatus);

        tvGain.setText("Tăng âm (" + chibiProcess.getAmplitude() + "x)");
        gainSlider.setValue(chibiProcess.getAmplitude());
        // ================ Start/Stop record button =====================
        //Click start voice button
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
                        gainSlider.setEnabled(false);
                        switchChibiEffect.setEnabled(false);
                    }
                }
            }
        });
        //Click stop voice button
        btnStopVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    isRecording = false;
                    try {
                        chibiProcess.stopProcessing();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    btnStartVoice.setVisibility(View.VISIBLE);
                    btnStopVoice.setVisibility(View.GONE);
                    gainSlider.setEnabled(true);
                    switchChibiEffect.setEnabled(true);
                }
                Toast.makeText(MainActivity.this, "Mic đã tắt", Toast.LENGTH_SHORT).show();
            }
        });
        // ================ Change Gain Factor ============================
        gainSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                tvGain.setText("Tăng âm (" + value + "x)");
                chibiProcess.setAmplitude(value);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putFloat("amplitude", value);
                editor.apply();
            }
        });
        // ================ Switch Chibi Effect ===========================
        switchChibiEffect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    chibiProcess.setPitchFactor(2.0f);
                } else {
                    chibiProcess.setPitchFactor(1.0f);
                }
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("switchChibiEffect", isChecked);
                editor.apply();
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == MICROPHONE_REQUEST_CODE);
    }



}
