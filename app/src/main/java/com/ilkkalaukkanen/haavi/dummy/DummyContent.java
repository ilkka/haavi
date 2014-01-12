package com.ilkkalaukkanen.haavi.dummy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample title for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static List<DummyItem> ITEMS = new ArrayList<DummyItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

    static {
        // Add 3 sample items.
      try {
        addItem(new DummyItem("1", "Item 1", "First item description", new URL("http://www.example.com/1.mp3")));
        addItem(new DummyItem("2", "Item 2", "Second item description", new URL("http://www.example.com/2.mp3")));
        addItem(new DummyItem("3", "Item 3", "Third item description", new URL("http://www.example.com/3.mp3")));
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }

    private static void addItem(DummyItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    /**
     * A dummy item representing a piece of title.
     */
    public static class DummyItem {
      public final  String id;
      public final  String title;
      public final String description;
      public final URL    url;

      public DummyItem(String id, String content, String description, URL url) {
        this.id = id;
        this.title = content;
        this.description = description;
        this.url = url;
      }

      @Override
      public String toString() {
        return title + " " + description + " " + url.toString();
      }
    }
}
