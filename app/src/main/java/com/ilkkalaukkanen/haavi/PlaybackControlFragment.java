package com.ilkkalaukkanen.haavi;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.util.concurrent.Callable;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Activities that contain this fragment must implement the
 * {@link com.ilkkalaukkanen.haavi.PlaybackControlFragment.PlaybackControlListener} interface to handle interaction
 * events. Use the {@link PlaybackControlFragment#newInstance} factory method to create an instance of this fragment.
 */
public class PlaybackControlFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PLAYING = "playing";
    private final TogglePlaybackCallback playbackToggledCallback;

    private boolean playing;

    private PlaybackControlListener listener;
    private ImageButton             togglePlaybackButton;
    private ImageButton             skipForwardButton;
    private ImageButton             skipBackwardButton;

    public PlaybackControlFragment() {
        playbackToggledCallback = new TogglePlaybackCallback(this);
    }

    /**
     * Use this factory method to create a new instance of this fragment using the provided parameters.
     *
     * @param playing Parameter 1.
     *
     * @return A new instance of fragment PlaybackControlFragment.
     */
    public static PlaybackControlFragment newInstance(boolean playing) {
        PlaybackControlFragment fragment = new PlaybackControlFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PLAYING, playing);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            playing = getArguments().getBoolean(ARG_PLAYING);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playback_control, container, false);
        assert view != null;
        togglePlaybackButton = (ImageButton) view.findViewById(R.id.controls_toggle_playback);
        if (playing) {
            togglePlaybackButton.setImageResource(R.drawable.ic_action_pause);
        }
        togglePlaybackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onTogglePlaybackButtonPressed();
            }
        });
        skipForwardButton = (ImageButton) view.findViewById(R.id.controls_skip_forward);
        skipForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onSkipForwardButtonPressed();
            }
        });
        skipBackwardButton = (ImageButton) view.findViewById(R.id.controls_skip_backward);
        skipBackwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onSkipBackwardButtonPressed();
            }
        });
        return view;
    }

    private void onSkipBackwardButtonPressed() {
        if (listener != null) {
            listener.onSkipBackward();
        }
    }

    private void onSkipForwardButtonPressed() {
        if (listener != null) {
            listener.onSkipForward();
        }
    }

    public void onTogglePlaybackButtonPressed() {
        if (listener != null) {
            listener.onTogglePlayback(playbackToggledCallback);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (PlaybackControlListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement PlaybackControlListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    /**
     * This interface must be implemented by activities that contain this fragment to allow an interaction in this
     * fragment to be communicated to the activity and potentially other fragments contained in that activity.
     * <p/>
     * See the Android Training lesson <a href= "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface PlaybackControlListener {
        public void onSkipBackward();

        public void onSkipForward();

        public void onTogglePlayback(Callable<Void> playbackToggledCallback);
    }

    private static class TogglePlaybackCallback implements Callable<Void> {
        private PlaybackControlFragment playbackControlFragment;

        public TogglePlaybackCallback(final PlaybackControlFragment playbackControlFragment) {
            this.playbackControlFragment = playbackControlFragment;
        }

        @Override
        public Void call() throws Exception {
            int newIcon = playbackControlFragment.playing ? R.drawable.ic_action_play : R.drawable.ic_action_pause;
            playbackControlFragment.togglePlaybackButton.setImageResource(newIcon);
            playbackControlFragment.playing = !playbackControlFragment.playing;
            return null;
        }
    }
}
