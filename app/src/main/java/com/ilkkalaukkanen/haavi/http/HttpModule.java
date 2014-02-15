package com.ilkkalaukkanen.haavi.http;

import com.google.inject.AbstractModule;
import org.apache.http.client.HttpClient;
import org.apache.http.nio.client.HttpAsyncClient;

public class HttpModule extends AbstractModule {
    @Override
    protected void configure() {
        final HttpClientProvider provider = new HttpClientProvider();
        bind(HttpClient.class).toProvider(provider);
        final HttpAsyncClientProvider asyncProvider = new HttpAsyncClientProvider();
        bind(HttpAsyncClient.class).toProvider(asyncProvider);
    }
}
