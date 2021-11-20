package com.example.cooltimer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SeekBar seekBar;
    private TextView textView;
    private Button button;
    private boolean isTimerOn;
    private CountDownTimer countDownTimer;
    private int defaultInterval;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isTimerOn = false;
        seekBar = findViewById(R.id.seekBar);
        textView = findViewById(R.id.textView);
        button = findViewById(R.id.button);
        initializeSeekBar();
        initializeButtonStartStop();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    public void initializeButtonStartStop() {
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                createTimer();
                button.setText("STOP");
            }
        });
    }

    public void updateTimer(long millisUtilFinished) {
        int minutes = (int) millisUtilFinished / 1000 / 60;
        int seconds = (int) millisUtilFinished / 1000 - (minutes * 60);

        String minute = "";
        String second = "";

        if (minutes < 10) {
            minute = "0" + minutes;
        } else {
            minute = String.valueOf(minutes);
        }

        if (seconds < 10) {
            second = "0" + seconds;
        } else {
            second = String.valueOf(seconds);
        }

        textView.setText(minute + ":" + second);
    }

    public void createTimer() {
        if (!isTimerOn) {
            seekBar.setEnabled(false);
            isTimerOn = true;

            countDownTimer = new CountDownTimer(seekBar.getProgress() * 1000L, 1000) {
                @Override
                public void onTick(long l) {
                    updateTimer(l);
                }

                @Override
                public void onFinish() {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                    if (sharedPreferences.getBoolean("enable_sound", true)) {
                        String melodyName = sharedPreferences.getString("timer_melody", "bell");
                        switch (melodyName) {
                            case "bell": {
                                MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.bell_sound);
                                mediaPlayer.start();
                                break;
                            }
                            case "alarm_siren": {
                                MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alarm_siren_sound);
                                mediaPlayer.start();
                                break;
                            }
                            case "bip": {
                                MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.bip_sound);
                                mediaPlayer.start();
                                break;
                            }
                        }


                    }
                    resetTimer();
                }
            }.start();

        } else {
            resetTimer();
        }
    }


    public void initializeSeekBar() {
        seekBar.setMax(600);
        setIntervalFromSharedInterval(PreferenceManager.getDefaultSharedPreferences(this));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                long progressInMillis = i * 1000L;
                updateTimer(progressInMillis);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void resetTimer() {
        countDownTimer.cancel();
        isTimerOn = false;
        setIntervalFromSharedInterval(sharedPreferences);
        button.setText("START");
        seekBar.setEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.timer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_setting) {
            Intent openSettings = new Intent(this, SettingsActivity.class);
            startActivity(openSettings);
            return true;
        } else if (id == R.id.action_about) {
            Intent openAbout = new Intent(this, AboutActivity.class);
            startActivity(openAbout);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setIntervalFromSharedInterval(SharedPreferences sharedPreferences) {

        defaultInterval = Integer.valueOf(sharedPreferences.getString("default_interval", "59"));
        updateTimer(defaultInterval * 1000L);
        seekBar.setProgress(defaultInterval);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals("default_interval")) {
            setIntervalFromSharedInterval(sharedPreferences);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }
}

