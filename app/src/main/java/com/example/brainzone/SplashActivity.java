package com.example.brainzone;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * SplashActivity – first screen the user sees (Lecture 4 – Activity Lifecycle).
 *
 * • Displays the app logo/name for 2 seconds.
 * • Loads the player name from SharedPreferences (Lecture 17).
 * • If no name is saved yet, shows a dialog to enter one before proceeding.
 * • Navigates to MainActivity using an explicit Intent (Lecture 8).
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY_MS = 2500; // 2.5 seconds

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // ── Read SharedPreferences (Lecture 17) ──────────────────────────────
        sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);

        // ── Delay then decide what to do ─────────────────────────────────────
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String savedName = sharedPreferences.getString(Constants.KEY_PLAYER_NAME, "");

            if (TextUtils.isEmpty(savedName)) {
                // First launch – ask for the player's name
                showNameDialog();
            } else {
                // Name already saved – go straight to MainActivity
                goToMainMenu();
            }
        }, SPLASH_DELAY_MS);
    }

    /** Prompts the player to enter their name on first launch. */
    private void showNameDialog() {
        // Inflate an EditText programmatically for the dialog
        final EditText editText = new EditText(this);
        editText.setHint(getString(R.string.hint_enter_name));
        editText.setPadding(48, 24, 48, 24);

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_welcome_title)
                .setMessage(R.string.dialog_welcome_msg)
                .setView(editText)
                .setCancelable(false)
                .setPositiveButton(R.string.btn_lets_go, (dialog, which) -> {
                    String name = editText.getText().toString().trim();
                    if (TextUtils.isEmpty(name)) {
                        name = "Player";
                    }
                    // ── Write player name to SharedPreferences (Lecture 17) ──
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(Constants.KEY_PLAYER_NAME, name);
                    editor.apply(); // async write – recommended
                    goToMainMenu();
                })
                .show();
    }

    /** Navigates to MainActivity using an explicit Intent (Lecture 8). */
    private void goToMainMenu() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // remove splash from back stack
    }

    // ── Activity lifecycle callbacks (Lecture 4) ──────────────────────────────
    @Override
    protected void onResume() { super.onResume(); }

    @Override
    protected void onPause()  { super.onPause();  }

    @Override
    protected void onDestroy(){ super.onDestroy(); }
}
