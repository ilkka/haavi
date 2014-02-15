package com.ilkkalaukkanen.haavi;

import android.util.Xml;
import com.google.inject.Inject;
import com.ilkkalaukkanen.haavi.model.Podcast;
import org.apache.http.nio.client.HttpAsyncClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import rx.Observable;
import rx.Subscriber;
import rx.apache.http.ObservableHttp;
import rx.apache.http.ObservableHttpResponse;
import rx.util.functions.Func1;

import java.io.IOException;

public class FeedDownloader {
    @Inject
    HttpAsyncClient client;

    @Inject
    public FeedDownloader() {
    }

    public Observable<Podcast> getFeed(final String url) {
        ObservableHttp.createGet(url, client)
                      .toObservable()
                      .flatMap(new Func1<ObservableHttpResponse, Observable<Podcast>>() {
                          @Override
                          public Observable<Podcast> call(final ObservableHttpResponse response) {
                              return Observable.create(new Observable.OnSubscribe<Podcast>() {
                                  @Override
                                  public void call(final Subscriber<? super Podcast> subscriber) {
                                      final XmlPullParser parser = Xml.newPullParser();
                                      try {
                                          parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                                          parser.setInput(response.getResponse().getEntity().getContent(), null);
                                          parser.nextTag();
                                          parseEntries(parser, subscriber);
                                      } catch (XmlPullParserException e) {
                                          subscriber.onError(e);
                                      } catch (IOException e) {
                                          subscriber.onError(e);
                                      }
                                  }
                              });
                          }
                      });
        return null;
    }

    private void parseEntries(final XmlPullParser parser, final Subscriber<? super Podcast> subscriber) throws
                                                                                                        IOException,
                                                                                                        XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "rss");
        parser.next();
        parser.require(XmlPullParser.START_TAG, null, "channel");
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

    private void parseItem(final XmlPullParser parser, final Subscriber<? super Podcast> subscriber) throws
                                                                                                     IOException,
                                                                                                     XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "item");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String title = null;
            if ("title".equals(parser.getName())) {
                title = readItemTitle(parser);
            }
            subscriber.onNext(new Podcast(title));
        }
    }

    private String readItemTitle(final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "title");
        final String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "title");
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
