package com.ilkkalaukkanen.haavi.test;

import android.test.ActivityInstrumentationTestCase2;
import com.robotium.solo.Solo;

/**
 * Created by ilau on 2014-01-12.
 */
public class EpisodeListTestCase extends ActivityInstrumentationTestCase2 {
  private Solo solo;
  private static Class episodeListActivityClass;
  static {
    try {
      episodeListActivityClass = Class.forName("com.ilkkalaukkanen.haavi.EpisodeListActivity");
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public EpisodeListTestCase() {
    super(episodeListActivityClass);
  }

  @Override
  protected void setUp() throws Exception {
    solo = new Solo(getInstrumentation(), getActivity());
  }

  public void testSelectEpisode() {
    solo.clickInList(0);
  }

  @Override
  public void tearDown() throws Exception {
    solo.finishOpenedActivities();
  }
}
