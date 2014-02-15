package com.ilkkalaukkanen.haavi.http;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Http client injection provider, pilfered with gratitude from http://candrews.integralblue.com/2011/09/best-way-to-use-httpclient-in-android/
 */
class HttpClientProvider implements Provider {
    public static final int CONNECTION_TIMEOUT = 60 * 1000;
    public static final int SO_TIMEOUT         = 5 * 60 * 1000;
    @Inject
    Application application;

    @Override
    public HttpClient get() {
        AbstractHttpClient client = new DefaultHttpClient() {
            @Override
            protected ClientConnectionManager createClientConnectionManager() {
                SchemeRegistry registry = new SchemeRegistry();
                registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
                registry.register(new Scheme("https", getHttpsSocketFactory(), 443));

                final HttpParams params = getParams();
                HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
                HttpConnectionParams.setSoTimeout(params, SO_TIMEOUT);
                HttpProtocolParams.setUserAgent(params, getUserAgentString(HttpProtocolParams.getUserAgent(params)));
                return new ThreadSafeClientConnManager(params, registry);
            }
        };
        return client;
    }

    private String getUserAgentString(final String defaultUserAgent) {
        PackageInfo packageInfo;
        final String packageName = application.getPackageName();
        String versionName = "";
        try {
            //noinspection ConstantConditions
            packageInfo = application.getPackageManager().getPackageInfo(packageName, 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageName +
               "/" +
               versionName +
               " (" +
               "Linux; U; Android " +
               Build.VERSION.RELEASE +
               "; " +
               Locale.getDefault() +
               "; " +
               Build.PRODUCT +
               ")" +
               (defaultUserAgent == null ? "" : " " + defaultUserAgent);
    }

    public SocketFactory getHttpsSocketFactory() {
        try {
            final Class<?> sessionCacheClass = Class.forName("android.net.SSLSessionCache");
            final Object sessionCache = sessionCacheClass.getConstructor(Context.class).newInstance(application);
            final Method getHttpSocketFactory = Class.forName("android.net.SSLCertificateSocketFactory")
                                                     .getMethod("getHttpSocketFactory", int.class, sessionCacheClass);
            return (SocketFactory) getHttpSocketFactory.invoke(null, CONNECTION_TIMEOUT, sessionCache);
        } catch (Exception e) {
            Log.e("HttpClientProvider", "Can't create https socket factory", e);
        }
        return null;
    }
}
