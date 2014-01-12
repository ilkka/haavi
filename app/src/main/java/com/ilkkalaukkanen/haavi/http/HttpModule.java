package com.ilkkalaukkanen.haavi.http;

import com.google.inject.AbstractModule;
import org.apache.http.client.HttpClient;

/**
 * Created by ilau on 2014-01-12.
 */
public class HttpModule extends AbstractModule {
  @Override
  protected void configure() {
    final HttpClientProvider provider = new HttpClientProvider();
    bind(HttpClient.class).toProvider(provider);
  }
}
