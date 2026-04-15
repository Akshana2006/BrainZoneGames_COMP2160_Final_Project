package com.example.brainzone;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

/**
 * BackgroundMusicService – plays looping background music during gameplay.
 *
 * Implemented as a Foreground Service (Lecture 16) so Android does not
 * kill it when the app is in the background.
 *
 * Lifecycle methods implemented (Lecture 15):
 *   onCreate()        – one-time setup; creates the notification channel
 *   onStartCommand()  – called each time startService() is used; starts music
 *   onBind()          – returns null (not a bound service)
 *   onDestroy()       – releases MediaPlayer resources
 *
 * HOW TO ADD MUSIC:
 *   1. Create folder:  app/src/main/res/raw/
 *   2. Copy your mp3:  app/src/main/res/raw/background_music.mp3
 *   The service will automatically detect and play it.
 */
public class BackgroundMusicService extends Service {

    private MediaPlayer mediaPlayer;

    // ── onCreate: called once when the service is first created ──────────────
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel(); // Required before startForeground() on API 26+
    }

    // ── onStartCommand: called every time startService() is invoked ──────────
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Read the music toggle setting from SharedPreferences (Lecture 17)
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        boolean musicEnabled = prefs.getBoolean(Constants.KEY_MUSIC_ENABLED, true);

        if (musicEnabled) {
            // Promote this service to foreground with a visible notification
            startForeground(Constants.MUSIC_NOTIF_ID, buildNotification());
            startMusic();
        } else {
            // Music is disabled – stop the service immediately
            stopSelf();
        }

        // START_STICKY: if killed by OS, restart automatically
        return START_STICKY;
    }

    /** Initialises MediaPlayer and begins looping playback. */
    private void startMusic() {
        if (mediaPlayer == null) {
            int resId = getResources().getIdentifier("background_music", "raw", getPackageName());
            if (resId != 0) {
                mediaPlayer = MediaPlayer.create(this, resId);
            }
        }

        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }
    }

    /** Builds the mandatory foreground notification (Lecture 16). */
    private Notification buildNotification() {
        // PendingIntent opens MainActivity when the notification is tapped (Lecture 16)
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                10,                            // request code
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE   // FLAG_IMMUTABLE (Lecture 16)
        );

        return new NotificationCompat.Builder(this, Constants.MUSIC_CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notif_music_text))
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentIntent(pendingIntent)
                .setSilent(true)
                .build();
    }

    /** Creates the notification channel required on API 26+ (Lecture 16). */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    Constants.MUSIC_CHANNEL_ID,
                    "BrainZone Music",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Background music for BrainZone");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    // ── onBind: return null – we are using a Started (unbound) service ────────
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // ── onDestroy: release all resources (Lecture 15) ─────────────────────────
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
