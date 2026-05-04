package com.example.imageview;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends BaseActivity {

    private static final String PREFS_NAME = "profile_prefs";
    private static final String KEY_BG_URI = "background_uri";

    private ImageView profileBackground;
    private ImageView profileAvatar;
    private TextView profileUsername;
    private TextView profileAccount;
    private AccountRepository accountRepository;
    private SessionManager sessionManager;
    private SharedPreferences prefs;
    private User currentUser;

    private final ActivityResultLauncher<String> backgroundPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    profileBackground.setImageURI(uri);
                    prefs.edit().putString(KEY_BG_URI, uri.toString()).apply();
                }
            });

    private final ActivityResultLauncher<String[]> avatarPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    persistReadPermission(uri);
                    saveProfile(null, uri.toString());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            openLoginAndFinish();
            return;
        }

        setContentView(R.layout.activity_profile);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        accountRepository = new AccountRepository(this);

        initViews();
        loadBackground();
        loadAccount();
        setupClickListeners();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        setupBottomNavigation(bottomNavigationView, R.id.nav_profile);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager != null && sessionManager.isLoggedIn()) {
            loadAccount();
        }
    }

    private void initViews() {
        profileBackground = findViewById(R.id.profile_background);
        profileAvatar = findViewById(R.id.profile_avatar);
        profileUsername = findViewById(R.id.profile_username);
        profileAccount = findViewById(R.id.profile_account);
    }

    private void loadBackground() {
        String bgUri = prefs.getString(KEY_BG_URI, null);
        if (bgUri != null) {
            profileBackground.setImageURI(Uri.parse(bgUri));
        }
    }

    private void loadAccount() {
        long userId = sessionManager.getCurrentUserId();
        accountRepository.getUser(userId, user -> {
            if (user == null) {
                sessionManager.logout();
                openLoginAndFinish();
                return;
            }
            currentUser = user;
            bindUser(user);
        });
    }

    private void bindUser(User user) {
        profileUsername.setText(user.getNickname());
        profileAccount.setText("Account: " + user.getUsername());
        String avatarUri = user.getAvatarUri();
        if (avatarUri != null && !avatarUri.isEmpty()) {
            profileAvatar.setImageURI(Uri.parse(avatarUri));
        } else {
            profileAvatar.setImageResource(R.drawable.icon_01);
        }
    }

    private void setupClickListeners() {
        findViewById(R.id.profile_card_container).setOnClickListener(v -> backgroundPickerLauncher.launch("image/*"));
        profileAvatar.setOnClickListener(v -> avatarPickerLauncher.launch(new String[]{"image/*"}));
        findViewById(R.id.menu_edit_profile).setOnClickListener(v -> showEditProfileDialog());
        findViewById(R.id.menu_logout).setOnClickListener(v -> {
            sessionManager.logout();
            openLoginAndFinish();
        });
        findViewById(R.id.menu_all_tasks).setOnClickListener(v ->
                NavigationHelper.navigateToTodoList(ProfileActivity.this, NavigationConstants.FILTER_ALL_TASKS));
        findViewById(R.id.menu_completed).setOnClickListener(v ->
                NavigationHelper.navigateToTodoList(ProfileActivity.this, NavigationConstants.FILTER_COMPLETED_TASKS));
        findViewById(R.id.menu_archived).setOnClickListener(v ->
                NavigationHelper.navigateToTodoList(ProfileActivity.this, NavigationConstants.FILTER_ARCHIVED_TASKS));
        findViewById(R.id.menu_tag_management).setOnClickListener(v ->
                NavigationHelper.navigateToTagManagement(ProfileActivity.this));
    }

    private void showEditProfileDialog() {
        if (currentUser == null) {
            return;
        }

        EditText nicknameInput = new EditText(this);
        nicknameInput.setSingleLine(true);
        nicknameInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        nicknameInput.setHint("Nickname");
        nicknameInput.setText(currentUser.getNickname());
        nicknameInput.setSelection(nicknameInput.getText().length());
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        nicknameInput.setPadding(padding, padding / 2, padding, padding / 2);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit profile")
                .setView(nicknameInput)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();
        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                saveProfile(nicknameInput.getText().toString(), null);
                dialog.dismiss();
            });
            nicknameInput.requestFocus();
            nicknameInput.postDelayed(() -> {
                InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (manager != null) {
                    manager.showSoftInput(nicknameInput, InputMethodManager.SHOW_IMPLICIT);
                }
            }, 150);
        });
        dialog.show();
    }

    private void saveProfile(String nickname, String avatarUri) {
        if (currentUser == null) {
            return;
        }

        String nextNickname = nickname == null ? currentUser.getNickname() : nickname;
        String nextAvatarUri = avatarUri == null ? currentUser.getAvatarUri() : avatarUri;
        accountRepository.updateProfile(currentUser.getId(), nextNickname, nextAvatarUri, user -> {
            if (user == null) {
                Toast.makeText(this, "Failed to save profile", Toast.LENGTH_SHORT).show();
                return;
            }
            currentUser = user;
            bindUser(user);
            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
        });
    }

    private void persistReadPermission(Uri uri) {
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException ignored) {
            // Some document providers do not grant persistable permissions.
        }
    }

    private void openLoginAndFinish() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
