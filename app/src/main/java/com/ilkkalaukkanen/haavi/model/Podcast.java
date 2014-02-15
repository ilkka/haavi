package com.ilkkalaukkanen.haavi.model;

import org.joda.time.DateTime;

public class Podcast {
    private final String   title;
    private final String   description;
    private final DateTime pubDate;

    public Podcast(final String title, final String description, final DateTime pubDate) {
        this.title = title;
        this.description = description;
        this.pubDate = pubDate;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public DateTime getPubDate() {
        return pubDate;
    }
}
