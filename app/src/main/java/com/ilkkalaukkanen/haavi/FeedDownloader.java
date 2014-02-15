package com.ilkkalaukkanen.haavi;

import android.util.Xml;
import com.google.inject.Inject;
import com.ilkkalaukkanen.haavi.model.Podcast;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import rx.Observable;
import rx.Subscriber;

import java.io.IOException;
import java.util.Locale;

public class FeedDownloader {
    public static final String            TAG_TITLE             = "title";
    public static final String            TAG_DESCRIPTION       = "description";
    public static final String            TAG_PUBDATE           = "pubDate";
    public static final String TAG_ENCLOSURE = "enclosure";
    public static final String TAG_GUID      = "guid";
    public static final DateTimeFormatter RSS_PUBDATE_FORMATTER =
            DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss Z").withLocale(Locale.ENGLISH);
    @Inject
    HttpClient client;

    @Inject
    public FeedDownloader() {
    }

    public Observable<Podcast> getFeed(final String url) {
        return Observable.create(new Observable.OnSubscribe<Podcast>() {
            @Override
            public void call(final Subscriber<? super Podcast> subscriber) {
                try {
                    final HttpResponse response = client.execute(new HttpGet(url));
                    final HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        final XmlPullParser parser = Xml.newPullParser();
                        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                        parser.setInput(entity.getContent(), null);
                        parser.nextTag();
                        parseEntries(parser, subscriber);
                    }
                    subscriber.onCompleted();
                } catch (IOException e) {
                    subscriber.onError(e);
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void parseEntries(final XmlPullParser parser,
                              final Subscriber<? super Podcast> subscriber)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "rss");
        // skip to channel start
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String name = parser.getName();
            if ("channel".equals(name)) {
                break;
            }
        }
        // parse items
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String name = parser.getName();
            if ("item".equals(name)) {
                parseItem(parser, subscriber);
            } else {
                skip(parser);
            }
        }
    }

    private void parseItem(final XmlPullParser parser,
                           final Subscriber<? super Podcast> subscriber)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "item");
        String title = null;
        String description = null;
        DateTime pubDate = null;
        String url = null;
        String length = null;
        String type = null;
        String guid = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String tagName = parser.getName();
            if (TAG_TITLE.equals(tagName)) {
                title = readNodeAsText(parser, TAG_TITLE);
            } else if (TAG_DESCRIPTION.equals(tagName)) {
                description = readNodeAsText(parser, TAG_DESCRIPTION);
            } else if (TAG_PUBDATE.equals(tagName)) {
                pubDate = DateTime.parse(readNodeAsText(parser, TAG_PUBDATE),
                                         RSS_PUBDATE_FORMATTER);
            } else if (TAG_ENCLOSURE.equals(tagName)) {
                url = parser.getAttributeValue(null, "url");
                length = parser.getAttributeValue(null, "length");
                type = parser.getAttributeValue(null, "type");
            } else if (TAG_GUID.equals(tagName)) {
                guid = readNodeAsText(parser, TAG_GUID);
            } else {
                skip(parser);
            }
        }
        subscriber.onNext(new Podcast(title, description, pubDate, url, length, type, guid));
    }

    private String readNodeAsText(final XmlPullParser parser, final String tagName)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, tagName);
        final String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, tagName);
        return title;
    }

    private String readText(final XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(final XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException("Not on a start tag");
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
