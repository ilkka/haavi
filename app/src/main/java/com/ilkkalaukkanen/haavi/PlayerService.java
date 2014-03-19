package com.ilkkalaukkanen.haavi;

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
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;

public class PlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
                                                      MediaPlayer.OnCompletionListener, MediaPlayer.OnInfoListener {
    public static final  String ACTION_PLAY           = "action_play";
    public static final  String ACTION_PAUSE          = "action_pause";
    public static final  String EXTRA_TITLE           = "extra_title";
    public static final  String PLAYER_INTERFACE_NAME = "com.ilkkalaukkanen.haavi.PlayerService";
    private static final String TAG                   = "PlayerService";

    MediaPlayer player;
    private String title;
    private IBinder playerBinder = new LocalPlayerBinder();
    private boolean paused       = false;

    // empty implementation
    private PlaybackStateListener listener = new PlaybackStateListener() {
        @Override
        public void playbackPaused() {
            // nop
        }

        @Override
        public void playbackStarted() {
            // nop
        }
    };

    public PlayerService() {
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        final String action = intent.getAction();
        assert action != null;
        final Uri uri = intent.getData();
        if (action.equals(ACTION_PLAY)) {
            title = intent.getStringExtra(EXTRA_TITLE);
            startPlaybackAsync(uri);
            return Service.START_STICKY;
        } else if (action.equals(ACTION_PAUSE)) {
            pause();
            listener.playbackPaused();
            return Service.START_NOT_STICKY;
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
        final String text = "Description goes here";
        final NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle()
                .bigText(text)
                .setBigContentTitle(title)
                .setSummaryText(text);
        final Intent pauseIntent = new Intent(getApplicationContext(), PlayerService.class)
                .setAction(ACTION_PAUSE);
        final PendingIntent pauseAction = PendingIntent.getService(getApplicationContext(),
                                                                   0,
                                                                   pauseIntent,
                                                                   PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle(title)
                .setContentText(title)
                .setTicker(title)
                .setSmallIcon(R.drawable.ic_action_play)
                .setContentIntent(pi)
                        //.setLargeIcon() // set thumbnail here
                .setStyle(bigText)
                .addAction(R.drawable.ic_action_pause,
                           "Pause",
                           pauseAction)
                .setAutoCancel(false)
                .build();
        startForeground(1, notification);
        player.start();
        listener.playbackStarted();
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

    /**
     * This interface can be used by clients to listen to state changes triggered by the service.
     */
    public interface PlaybackStateListener {
        public void playbackPaused();

        public void playbackStarted();
    }

    public class LocalPlayerBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }

        public void setPlaybackStateListener(final PlaybackStateListener l) {
            listener = l;
        }
    }
}
