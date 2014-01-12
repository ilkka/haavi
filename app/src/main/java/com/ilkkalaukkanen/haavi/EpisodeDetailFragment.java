package com.ilkkalaukkanen.haavi;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ilkkalaukkanen.haavi.dummy.DummyContent;

/**
 * A fragment representing a single Episode detail screen. This fragment is either contained in a {@link
 * EpisodeListActivity} in two-pane mode (on tablets) or a {@link EpisodeDetailActivity} on handsets.
 */
public class EpisodeDetailFragment extends Fragment {
  /**
   * The fragment argument representing the item ID that this fragment represents.
   */
  public static final String ARG_ITEM_ID = "item_id";

  /**
   * The dummy title this fragment is presenting.
   */
  private DummyContent.DummyItem mItem;

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation
   * changes).
   */
  public EpisodeDetailFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments().containsKey(ARG_ITEM_ID)) {
      // Load the dummy title specified by the fragment
      // arguments. In a real-world scenario, use a Loader
      // to load title from a title provider.
      mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_episode_detail, container, false);

    // display item
    if (mItem != null) {
      ((TextView) rootView.findViewById(R.id.episode_title)).setText(mItem.title);
      ((TextView) rootView.findViewById(R.id.episode_description)).setText(mItem.description);
    }

    return rootView;
  }
}
