package com.ilkkalaukkanen.haavi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.google.inject.Inject;
import com.ilkkalaukkanen.haavi.model.Podcast;
import org.joda.time.format.DateTimeFormat;
import roboguice.activity.RoboFragmentActivity;
import rx.android.Properties;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;


/**
 * An activity representing a list of Episodes. This activity has different presentations for handset and tablet-size
 * devices. On handsets, the activity presents a list of items, which when touched, lead to a {@link
 * EpisodeDetailActivity} representing item details. On tablets, the activity presents the list of items and item
 * details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a {@link EpisodeListFragment} and the item details
 * (if present) is a {@link EpisodeDetailFragment}.
 * <p/>
 * This activity also implements the required {@link EpisodeListFragment.Callbacks} interface to listen for item
 * selections.
 */
public class EpisodeListActivity extends RoboFragmentActivity
        implements EpisodeListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
     */
    private boolean mTwoPane;

    @Inject
    FeedDownloader feedDownloader;
    private EpisodeListFragment   episodeListFragment;
    private ArrayAdapter<Podcast> podcastArrayAdapter;
    private ArrayList<Podcast>    podcastList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode_list);

        episodeListFragment = (EpisodeListFragment) getSupportFragmentManager().findFragmentById(R.id.episode_list);

        if (findViewById(R.id.episode_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            episodeListFragment
                    .setActivateOnItemClick(true);
        }
        podcastList = new ArrayList<Podcast>();
        podcastArrayAdapter = new PodcastArrayAdapter(podcastList);
        episodeListFragment.setListAdapter(podcastArrayAdapter);

        feedDownloader.getFeed("http://www.theskepticsguide.org/feed/sgu")
                      .subscribeOn(Schedulers.newThread())
                      .observeOn(AndroidSchedulers.mainThread())
                      .toList()
                      .subscribe(Properties.dataSetFrom(podcastArrayAdapter));

        // TODO: If exposing deep links into your app, handle intents here.
    }

    /**
     * Callback method from {@link EpisodeListFragment.Callbacks} indicating that the item with the given ID was
     * selected.
     *
     * @param position
     */
    @Override
    public void onItemSelected(int position) {
        Podcast item = podcastList.get(position);

        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(EpisodeDetailFragment.ARG_ITEM_TITLE, item.getTitle());
            arguments.putString(EpisodeDetailFragment.ARG_ITEM_DESCRIPTION, item.getDescription());
            arguments.putString(EpisodeDetailFragment.ARG_ITEM_URL, item.getUrl());
            EpisodeDetailFragment fragment = new EpisodeDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                                       .replace(R.id.episode_detail_container, fragment)
                                       .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, EpisodeDetailActivity.class);
            detailIntent.putExtra(EpisodeDetailActivity.EXTRA_ITEM_TITLE, item.getTitle());
            detailIntent.putExtra(EpisodeDetailActivity.EXTRA_ITEM_DESCRIPTION, item.getDescription());
            detailIntent.putExtra(EpisodeDetailActivity.EXTRA_ITEM_URL, item.getUrl());
            startActivity(detailIntent);
        }
    }

    private class PodcastArrayAdapter extends ArrayAdapter<Podcast> {
        public PodcastArrayAdapter(final List<Podcast> podcastList) {
            super(EpisodeListActivity.this, R.layout.episode_list_item, podcastList);
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View view = null;
            if (convertView != null) {
                if (convertView.findViewById(R.id.titleTextView) != null) {
                    view = convertView;
                }
            }
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.episode_list_item, null);
            }
            assert view != null;
            TextView titleView = (TextView) view.findViewById(R.id.titleTextView);
            TextView pubDateView = (TextView) view.findViewById(R.id.pubDateTextView);

            Podcast podcast = getItem(position);
            titleView.setText(podcast.getTitle());
            pubDateView.setText(podcast.getPubDate().toLocalDateTime().toString(DateTimeFormat.mediumDateTime()));
            return view;
        }
    }
}
