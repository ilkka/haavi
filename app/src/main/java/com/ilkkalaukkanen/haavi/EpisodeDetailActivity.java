package com.ilkkalaukkanen.haavi;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import com.google.inject.Inject;
import com.ilkkalaukkanen.haavi.player.PlayerService;
import roboguice.activity.RoboFragmentActivity;

import java.util.concurrent.Callable;

/**
 * An activity representing a single Episode detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link EpisodeListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link EpisodeDetailFragment}.
 */
public class EpisodeDetailActivity extends RoboFragmentActivity implements PlaybackControlFragment.PlaybackControlListener,
                                                                           AudioManager.OnAudioFocusChangeListener {

    public static final  String EXTRA_ITEM_TITLE       = "item_title";
    public static final  String EXTRA_ITEM_DESCRIPTION = "item_description";
    public static final  String EXTRA_ITEM_URL         = "item_url";
    private static final String TAG                    = "EpisodeDetailActivity";

    private String title;
    private String description;
    private String url;

    @Inject
    AudioManager audioManager;

    private PlayerService.LocalPlayerBinder playerInterface;
    private boolean playerBound = false;

    private final ServiceConnection playerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            Log.d(TAG, "onServiceConnected: " + name);
            if (PlayerService.PLAYER_INTERFACE_NAME.equals(name.getClassName())) {
                playerInterface = (PlayerService.LocalPlayerBinder) service;
                playerBound = true;
                Log.d(TAG, "Player interface bound");
            } else {
                Log.w(TAG, "Unknown interface");
            }
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            playerBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode_detail);

        final Intent intent = getIntent();
        title = intent.getStringExtra(EXTRA_ITEM_TITLE);
        description = intent.getStringExtra(EXTRA_ITEM_DESCRIPTION);
        url = intent.getStringExtra(EXTRA_ITEM_URL);

        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(EpisodeDetailFragment.ARG_ITEM_TITLE, title);
            arguments.putString(EpisodeDetailFragment.ARG_ITEM_DESCRIPTION, description);
            arguments.putString(EpisodeDetailFragment.ARG_ITEM_URL, url);
//            arguments.putString(EpisodeDetailFragment.ARG_ITEM_ID,
//                                intent.getStringExtra(EpisodeDetailFragment.ARG_ITEM_ID));
            EpisodeDetailFragment fragment = new EpisodeDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.episode_detail_container, fragment)
                    .commit();
        } else {
            // TODO: get back playback progress probably?
        }

        // bind to playback service
        if (!bindService(new Intent(this, PlayerService.class), playerServiceConnection, BIND_AUTO_CREATE)) {
            throw new AssertionError();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(playerServiceConnection);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        // TODO: save playback progress probably?
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, EpisodeListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSkipBackward() {

    }

    @Override
    public void onSkipForward() {

    }

    @Override
    public void onTogglePlayback(final Callable<Void> playbackToggledCallback) {
        if (!playerBound) {
            Log.e(TAG, "No player interface in onTogglePlayback");
            return;
        }
        final PlayerService service = playerInterface.getService();
        if (!service.isPlaying()) {
            final int result = audioManager.requestAudioFocus(this,
                                                              AudioManager.STREAM_MUSIC,
                                                              AudioManager.AUDIOFOCUS_GAIN);
            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                // TODO: what to do?
            }
            if (service.isPaused()) {
                service.play();
            } else {
                final Intent intent = new Intent(EpisodeDetailActivity.this, PlayerService.class)
                        .setAction(PlayerService.ACTION_PLAY)
                        .setData(Uri.parse(url))
                        .putExtra(PlayerService.EXTRA_TITLE, title);
                startService(intent);
            }
        } else {
            service.pause();
        }
        try {
            playbackToggledCallback.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAudioFocusChange(final int focusChange) {
        if (!playerBound) {
            Log.e(TAG, "No player interface in onTogglePlayback");
            return;
        }
        playerInterface.getService().pause();
    }

}
