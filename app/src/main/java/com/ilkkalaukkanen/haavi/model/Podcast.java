package com.ilkkalaukkanen.haavi.model;

import org.joda.time.DateTime;

public class Podcast {
    private final String   title;
    private final String   description;
    private final DateTime pubDate;
    private final String   url;
    private final long     length;
    private final String   type;
    private final String   guid;
    private int duration;

    public Podcast(final String title,
                   final String description,
                   final DateTime pubDate,
                   final String url,
                   final String length,
                   final String type,
                   final String guid) {
        this.title = title;
        this.description = description;
        this.pubDate = pubDate;
        this.url = url;
        this.length = Long.parseLong(length, 10);
        this.type = type;
        this.guid = guid;
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

    public String getUrl() {
        return url;
    }

    public String getGuid() {
        return guid;
    }

    public long getSizeInBytes() {
        return length;
    }

    public String getType() {
        return type;
    }
}
