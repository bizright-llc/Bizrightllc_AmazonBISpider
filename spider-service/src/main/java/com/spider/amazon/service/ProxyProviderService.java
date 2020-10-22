package com.spider.amazon.service;

import com.spider.amazon.dto.ProviderProxyDTO;
import com.spider.amazon.dto.ProxyDTO;
import com.spider.amazon.model.ProxyProvider;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;

import java.io.IOException;
import java.net.*;
import java.util.List;

/**
 * This is the interface connect to proxy provider
 * System use the proxy from provider update the status of the database proxy pool
 *
 */
public interface ProxyProviderService {

    /**
     * Get the active proxies from the provider
     *
     * @return
     */
    List<ProviderProxyDTO> getActiveProxies();

    List<ProviderProxyDTO> getActiveRotatingProxies();

    /**
     * Test proxy server get request
     * @param args
     * @throws IOException
     */
//    public static void main(String[] args) throws IOException {
//
//        System.out.println("To enable your free eval account and get "
//                + "CUSTOMER, YOURZONE and YOURPASS, please contact "
//                + "sales@luminati.io");
//        HttpHost proxy = new HttpHost("zproxy.lum-superproxy.io", 22225);
//
//        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
//
//        //Client credentials
//        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
//        credentialsProvider.setCredentials(new AuthScope(proxy),
//                new UsernamePasswordCredentials("lum-customer-ipower-zone-static-country-us", "38rnyeoymh2g"));
//
//        // Create AuthCache instance
//        AuthCache authCache = new BasicAuthCache();
//
//        BasicScheme basicAuth = new BasicScheme();
//        authCache.put(proxy, basicAuth);
//        HttpClientContext context = HttpClientContext.create();
//        context.setCredentialsProvider(credentialsProvider);
//        context.setAuthCache(authCache);
//
//        HttpClient httpclient = HttpClients.custom()
//                .setRoutePlanner(routePlanner)
//                .setDefaultCredentialsProvider(credentialsProvider)
//                .build();
//
//        HttpGet httpget = new HttpGet("http://www.tutorialspoint.com/");
//
//        HttpResponse httpresponse = httpclient.execute(httpget);
//
//        System.out.println(httpresponse.getStatusLine());
//    }

}
