package com.example.imageview;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 最小可交互入口页：用于隔离 NO_INPUT_CHANNEL 问题。
 */
public class MessageActivityLite extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_lite);

        Button openMainButton = findViewById(R.id.btn_open_message);
        openMainButton.setOnClickListener(v ->
                startActivity(new Intent(MessageActivityLite.this, MessageActivity.class))
        );
    }
}

