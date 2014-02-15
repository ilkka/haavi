package com.ilkkalaukkanen.haavi.player;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.*;
import com.ilkkalaukkanen.haavi.EpisodeDetailActivity;
import com.ilkkalaukkanen.haavi.R;

import java.io.FileDescriptor;
import java.io.IOException;

public class PlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
                                                      MediaPlayer.OnCompletionListener, MediaPlayer.OnInfoListener {
    public static final String ACTION_PLAY = "com.ilkkalaukkanen.haavi.player.action_play";
    public static final String EXTRA_TITLE = "extra_title";

    MediaPlayer player;
    private String title;

    public PlayerService() {
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        final String action = intent.getAction();
        title = intent.getStringExtra(EXTRA_TITLE);
        assert action != null;
        if (action.equals(ACTION_PLAY)) {
            final Uri uri = intent.getData();
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
            return Service.START_STICKY;
        } else {
            return super.onStartCommand(intent, flags, startId);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new PlayerServiceInterface();
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
        player.release();
        player = null;
    }

    @Override
    public boolean onError(final MediaPlayer mp, final int what, final int extra) {
        stopForeground(true);
        return false;
    }

    @Override
    public void onCompletion(final MediaPlayer mp) {
        stopForeground(true);
        mp.release();
    }

    @Override
    public boolean onInfo(final MediaPlayer mp, final int what, final int extra) {
        return false;
    }

    private static class PlayerServiceInterface implements IBinder {
        @Override
        public String getInterfaceDescriptor() throws RemoteException {
            return "com.ilkkalaukkanen.haavi.player.PlayerService";
        }

        @Override
        public boolean pingBinder() {
            return true;
        }

        @Override
        public boolean isBinderAlive() {
            return true;
        }

        @Override
        public IInterface queryLocalInterface(final String descriptor) {
            return null;
        }

        @Override
        public void dump(final FileDescriptor fd, final String[] args) throws RemoteException {

        }

        @Override
        public void dumpAsync(final FileDescriptor fd, final String[] args) throws RemoteException {

        }

        @Override
        public boolean transact(final int code, final Parcel data, final Parcel reply, final int flags) throws
                                                                                                        RemoteException {
            return false;
        }

        @Override
        public void linkToDeath(final DeathRecipient recipient, final int flags) throws RemoteException {

        }

        @Override
        public boolean unlinkToDeath(final DeathRecipient recipient, final int flags) {
            return false;
        }
    }
}
