package com.example.imageview;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameInput;
    private EditText passwordInput;
    private Button loginButton;
    private TextView registerLink;
    private AccountRepository accountRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        accountRepository = new AccountRepository(this);
        sessionManager = new SessionManager(this);

        usernameInput = findViewById(R.id.username);
        passwordInput = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        registerLink = findViewById(R.id.register_link);

        loginButton.setOnClickListener(v -> login());
        registerLink.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void login() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (TextUtils.isEmpty(username)) {
            usernameInput.setError("请输入用户名");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("请输入密码");
            return;
        }

        loginButton.setEnabled(false);
        accountRepository.login(username, password, result -> {
            loginButton.setEnabled(true);
            if (!result.isSuccess()) {
                Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            sessionManager.login(result.getUser().getId());
            Intent intent = new Intent(this, MessageActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
