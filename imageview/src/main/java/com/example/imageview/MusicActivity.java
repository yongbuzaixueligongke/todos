package com.example.imageview;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MusicActivity extends AppCompatActivity {

    private static final int MEDIA_READ_REQUEST_CODE = 100;
    private MediaPlayer mediaPlayer;
    private TextView songTitle;
    private TextView songDuration;
    private ImageView albumArt;
    private Button playButton;
    private Button pauseButton;
    private Button stopButton;
    private SeekBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        songTitle = findViewById(R.id.song_title);
        songDuration = findViewById(R.id.song_duration);
        albumArt = findViewById(R.id.album_art);
        playButton = findViewById(R.id.play_button);
        pauseButton = findViewById(R.id.pause_button);
        stopButton = findViewById(R.id.stop_button);
        progressBar = findViewById(R.id.progress_bar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, MEDIA_READ_REQUEST_CODE);
            } else {
                loadMusic();
            }
        } else {
            loadMusic();
        }

        playButton.setOnClickListener(v -> playMusic());
        pauseButton.setOnClickListener(v -> pauseMusic());
        stopButton.setOnClickListener(v -> stopMusic());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_message) {
                startActivity(new Intent(MusicActivity.this, MessageActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_calendar) {
                startActivity(new Intent(MusicActivity.this, CalendarActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                startActivity(new Intent(MusicActivity.this, ProfileActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_projects) {
                Intent intent = new Intent(MusicActivity.this, MessageActivity.class);
                intent.putExtra(NavigationConstants.EXTRA_NAVIGATE_TO_PROJECTS, true);
                startActivity(intent);
                return true;
            }
            return false;
        });

        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    mediaPlayer.pause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MEDIA_READ_REQUEST_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadMusic();
        }
    }

    private void loadMusic() {
        mediaPlayer = MediaPlayer.create(this, R.raw.supernatural);
        if (mediaPlayer != null) {
            songTitle.setText("Supernatural");
            progressBar.setMax(mediaPlayer.getDuration());
            mediaPlayer.setOnCompletionListener(mp -> stopMusic());
            updateProgressBar();
        } else {
            songTitle.setText("Unable to load audio");
        }
    }

    private void updateProgressBar() {
        if (mediaPlayer != null) {
            progressBar.setProgress(mediaPlayer.getCurrentPosition());
            progressBar.postDelayed(this::updateProgressBar, 1000);
        }
    }

    private void playMusic() {
        if (mediaPlayer == null) {
            loadMusic();
        }
        if (mediaPlayer != null) {
            mediaPlayer.start();
            updateProgressBar();
        }
    }

    private void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    private void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            progressBar.setProgress(0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
