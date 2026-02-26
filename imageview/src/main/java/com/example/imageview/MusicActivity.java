package com.example.imageview;

import static androidx.core.util.TimeUtils.formatDuration;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;

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
                loadMusic(); // 权限已授予，加载音乐
            }
        } else {
            loadMusic(); // 低于 Android 13，直接加载音乐
        }

        playButton.setOnClickListener(v -> playMusic());
        pauseButton.setOnClickListener(v -> pauseMusic());
        stopButton.setOnClickListener(v -> stopMusic());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
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
                startActivity(new Intent(MusicActivity.this, MessageActivity.class));
                return true;
            }
            return false;
        });

        // 设置 SeekBar 的变化监听器
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 暂停更新进度
                if (mediaPlayer != null) {
                    mediaPlayer.pause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 恢复播放
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MEDIA_READ_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadMusic(); // 权限被授予，加载音乐
            } else {
                // 权限被拒绝，您可以显示一个提示信息
            }
        }
    }

    private void loadMusic() {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        mediaPlayer = MediaPlayer.create(this, R.raw.supernatural); // 替换为您的音频文件名，不需要扩展名
        if (mediaPlayer != null) {
            songTitle.setText("Supernatural");
            //songDuration.setText(formatDuration((long) mediaPlayer.getDuration())); // 设置歌曲持续时间
            progressBar.setMax(mediaPlayer.getDuration()); // 设置 SeekBar 最大值
            mediaPlayer.setOnCompletionListener(mp -> stopMusic()); // 音乐播放完成后停止
            updateProgressBar(); // 启动进度条更新
        } else {
            songTitle.setText("无法加载音乐");
        }
    }

    private void updateProgressBar() {
        if (mediaPlayer != null) {
            progressBar.setProgress(mediaPlayer.getCurrentPosition());
            progressBar.postDelayed(this::updateProgressBar, 1000); // 每秒更新一次进度条
        }
    }

    private void playMusic() {
        if (mediaPlayer == null) {
            loadMusic();
        }
        if (mediaPlayer != null) {
            mediaPlayer.start();
            updateProgressBar(); // 开始更新进度条
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
            mediaPlayer = null; // 释放资源
            progressBar.setProgress(0); // 重置进度条
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