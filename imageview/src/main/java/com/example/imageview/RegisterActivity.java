package com.example.imageview;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText username;
    private EditText nickname;
    private EditText password;
    private EditText confirmPassword;
    private Button registerButton;
    private AccountRepository accountRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        accountRepository = new AccountRepository(this);
        sessionManager = new SessionManager(this);

        username = findViewById(R.id.username);
        nickname = findViewById(R.id.nickname);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirm_password);
        registerButton = findViewById(R.id.register_button);
        TextView loginLink = findViewById(R.id.login_link);

        registerButton.setOnClickListener(v -> register());
        loginLink.setOnClickListener(v -> finish());
    }

    private void register() {
        String usernameValue = username.getText().toString().trim();
        String nicknameValue = nickname.getText().toString().trim();
        String passwordValue = password.getText().toString();
        String confirmValue = confirmPassword.getText().toString();

        if (TextUtils.isEmpty(usernameValue)) {
            username.setError("Please enter a username");
            return;
        }
        if (usernameValue.length() < 3) {
            username.setError("Username must be at least 3 characters");
            return;
        }
        if (TextUtils.isEmpty(passwordValue)) {
            password.setError("Please enter a password");
            return;
        }
        if (passwordValue.length() < 6) {
            password.setError("Password must be at least 6 characters");
            return;
        }
        if (!passwordValue.equals(confirmValue)) {
            confirmPassword.setError("Passwords do not match");
            return;
        }

        registerButton.setEnabled(false);
        accountRepository.register(usernameValue, passwordValue, nicknameValue, result -> {
            registerButton.setEnabled(true);
            if (!result.isSuccess()) {
                Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            sessionManager.login(result.getUser().getId());
            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MessageActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
