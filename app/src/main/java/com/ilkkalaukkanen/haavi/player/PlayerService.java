package com.ilkkalaukkanen.haavi.player;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

import java.io.FileDescriptor;
import java.io.IOException;

public class PlayerService extends Service implements MediaPlayer.OnPreparedListener {
    public static final String ACTION_PLAY = "com.ilkkalaukkanen.haavi.player.action_play";

    MediaPlayer player;

    public PlayerService() {
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        final String action = intent.getAction();
        assert action != null;
        if (action.equals(ACTION_PLAY)) {
            final Uri uri = intent.getData();
            player = new MediaPlayer();
            try {
                player.setDataSource(this, uri);
            } catch (IOException e) {
                throw new RuntimeException("Error setting data source", e);
            }
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setOnPreparedListener(this);
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
        player.start();
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
