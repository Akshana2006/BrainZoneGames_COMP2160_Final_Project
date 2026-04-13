package com.example.brainzone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * ScreenOffReceiver – Dynamic BroadcastReceiver (Lecture 18).
 *
 * Listens for ACTION_SCREEN_OFF, which is sent by the Android system when
 * the user turns off the screen.  This MUST be registered dynamically
 * (cannot be declared in the manifest – system restriction).
 *
 * Registered in:   Game1Activity, Game2Activity, Game3Activity (onResume)
 * Unregistered in: Game1Activity, Game2Activity, Game3Activity (onDestroy)
 *
 * When fired, it signals the currently active game to pause,
 * preventing the countdown timer from running while the screen is off.
 */
public class ScreenOffReceiver extends BroadcastReceiver {

    /** Callback interface so the host Activity can react. */
    public interface GamePauseListener {
        void onScreenOff();
    }

    private final GamePauseListener pauseListener;

    public ScreenOffReceiver(GamePauseListener pauseListener) {
        this.pauseListener = pauseListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
            // Notify the game activity to pause
            if (pauseListener != null) {
                pauseListener.onScreenOff();
            }
            Toast.makeText(context,
                    "Screen off – game paused!", Toast.LENGTH_SHORT).show();
        }
    }
}
