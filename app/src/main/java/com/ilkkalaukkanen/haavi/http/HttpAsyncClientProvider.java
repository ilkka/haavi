package com.ilkkalaukkanen.haavi.http;

import com.google.inject.Provider;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.HttpAsyncClient;

public class HttpAsyncClientProvider implements Provider {
    @Override
    public HttpAsyncClient get() {
        return HttpAsyncClients.createDefault();
    }
}
