package com.ilkkalaukkanen.haavi.player;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import com.ilkkalaukkanen.haavi.EpisodeDetailActivity;
import com.ilkkalaukkanen.haavi.R;

import java.io.IOException;

public class PlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
                                                      MediaPlayer.OnCompletionListener, MediaPlayer.OnInfoListener {
    public static final  String ACTION_PLAY           = "com.ilkkalaukkanen.haavi.player.action_play";
    public static final  String EXTRA_TITLE           = "extra_title";
    public static final  String PLAYER_INTERFACE_NAME = "com.ilkkalaukkanen.haavi.player.PlayerService";
    private static final String TAG                   = "PlayerService";

    MediaPlayer player;
    private String title;
    private IBinder playerBinder = new LocalPlayerBinder();
    private boolean paused       = false;

    public PlayerService() {
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        final String action = intent.getAction();
        assert action != null;
        title = intent.getStringExtra(EXTRA_TITLE);
        final Uri uri = intent.getData();
        if (action.equals(ACTION_PLAY)) {
            startPlaybackAsync(uri);
            return Service.START_STICKY;
        } else {
            return super.onStartCommand(intent, flags, startId);
        }
    }

    private void startPlaybackAsync(final Uri uri) {
        if (player != null) {
            player.release();
        }
        player = new MediaPlayer();
        try {
            player.setDataSource(getApplicationContext(), uri);
        } catch (IOException e) {
            throw new RuntimeException("Error setting data source", e);
        }
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnErrorListener(this);
        player.setOnCompletionListener(this);
        player.setOnInfoListener(this);
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.prepareAsync();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return playerBinder;
    }

    @Override
    public void onPrepared(final MediaPlayer mp) {
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                                                     new Intent(getApplicationContext(), EpisodeDetailActivity.class),
                                                     PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle(title)
                .setContentText(title)
                .setSmallIcon(R.drawable.ic_action_play)
                .setContentIntent(pi)
                .build();
        startForeground(1, notification);
        player.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
            paused = false;
        }
    }

    @Override
    public boolean onError(final MediaPlayer mp, final int what, final int extra) {
        Log.e(TAG, "Playback error " + what + ", extra " + extra);
        stopForeground(true);
        player.release();
        player = null;
        paused = false;
        return false;
    }

    @Override
    public void onCompletion(final MediaPlayer mp) {
        stopForeground(true);
        player.release();
        player = null;
    }

    @Override
    public boolean onInfo(final MediaPlayer mp, final int what, final int extra) {
        return false;
    }

    public boolean isPlaying() {
        return (player != null && player.isPlaying());
    }

    public void pause() {
        if (player != null) {
            player.pause();
            paused = true;
            stopForeground(true);
        }
    }

    public void play() {
        if (player != null && paused) {
            player.start();
            paused = false;
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public class LocalPlayerBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }
}
