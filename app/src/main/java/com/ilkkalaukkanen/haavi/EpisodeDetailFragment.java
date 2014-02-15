package com.ilkkalaukkanen.haavi;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import com.ilkkalaukkanen.haavi.player.PlayerService;

/**
 * A fragment representing a single Episode detail screen. This fragment is either contained in a {@link
 * EpisodeListActivity} in two-pane mode (on tablets) or a {@link EpisodeDetailActivity} on handsets.
 */
public class EpisodeDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment represents.
     */
    public static final String ARG_ITEM_ID          = "item_id";
    public static final String ARG_ITEM_TITLE       = "item_title";
    public static final String ARG_ITEM_DESCRIPTION = "item_description";
    public static final String ARG_ITEM_URL         = "item_url";
    private String title;
    private String description;
    private String url;

    /**
     * The dummy title this fragment is presenting.
     */
//    private DummyContent.DummyItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation
     * changes).
     */
    public EpisodeDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle arguments = getArguments();
        if (arguments.containsKey(ARG_ITEM_ID)) {
            // Load the dummy title specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load title from a title provider.
            //mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
        if (arguments.containsKey(ARG_ITEM_TITLE)) {
            title = arguments.getString(ARG_ITEM_TITLE);
        }
        if (arguments.containsKey(ARG_ITEM_DESCRIPTION)) {
            description = arguments.getString(ARG_ITEM_DESCRIPTION);
        }
        if (arguments.containsKey(ARG_ITEM_URL)) {
            url = arguments.getString(ARG_ITEM_URL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_episode_detail, container, false);
        assert rootView != null;

        // display item
        ((TextView) rootView.findViewById(R.id.episode_title)).setText(title);
        ((TextView) rootView.findViewById(R.id.episode_description)).setText(description);

        // set up controls
        ImageButton playButton = (ImageButton) rootView.findViewById(R.id.controls_toggle_playback);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final Intent intent = new Intent(getActivity(), PlayerService.class)
                        .setAction(PlayerService.ACTION_PLAY)
                        .setData(Uri.parse(url))
                        .putExtra(PlayerService.EXTRA_TITLE, title);
                getActivity().startService(intent);
            }
        });

        return rootView;
    }
}
