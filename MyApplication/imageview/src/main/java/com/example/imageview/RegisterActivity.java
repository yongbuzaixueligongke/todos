package com.example.imageview;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText username, password, confirmPassword;
    private Button registerButton;
    private TextView forgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirm_password);
        registerButton = findViewById(R.id.register_button);
        forgotPassword = findViewById(R.id.forgot_password);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 处理注册逻辑
                Intent intent = new Intent(RegisterActivity.this, MessageActivity.class);
                startActivity(intent);

            }
        });
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

    }
}