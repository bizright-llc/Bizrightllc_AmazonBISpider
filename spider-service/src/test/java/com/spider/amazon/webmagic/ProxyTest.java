package com.spider.amazon.webmagic;

import com.spider.SpiderServiceApplication;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.model.ProxyDO;
import com.spider.amazon.utils.WebDriverUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes= SpiderServiceApplication.class)
public class ProxyTest {

    @Autowired
    private SpiderConfig spiderConfig;

    /**
     * Test proxy server get request
     * @throws IOException
     */
    @Test
    public void testSimpleRequest() throws IOException {

        System.out.println("To enable your free eval account and get "
                + "CUSTOMER, YOURZONE and YOURPASS, please contact "
                + "sales@luminati.io");

        int testCount = 500;

        int totalCount = 0;
        int validCount = 0;

        List<Long> usedTimeList = new ArrayList<Long>();

        HttpHost proxy = new HttpHost("zproxy.lum-superproxy.io", 22225);
        String url = "http://www.tutorialspoint.com/";


        for (int i=0; i< testCount; i++){
            long usedTime = testProxy(proxy,url);

            totalCount++;

            if(usedTime != -1){
                validCount++;
                usedTimeList.add(usedTime);
            }
        }

        getStatusResult(usedTimeList, validCount, totalCount);

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

//        System.out.println(httpresponse.getStatusLine());
    }

    /**
     *
     * @param url
     * @return
     */
    private long testProxy(HttpHost proxy, String url){

        System.out.println("Test Proxy");

        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);

        //Client credentials
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope(proxy),
                new UsernamePasswordCredentials("lum-customer-ipower-zone-static-country-us", "38rnyeoymh2g"));

        // Create AuthCache instance
        AuthCache authCache = new BasicAuthCache();

        BasicScheme basicAuth = new BasicScheme();
        authCache.put(proxy, basicAuth);
        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credentialsProvider);
        context.setAuthCache(authCache);

        int timeout = 60;
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000)
                .setSocketTimeout(timeout * 1000).build();

        try{
            long startTime = System.currentTimeMillis();

            HttpClient httpclient = HttpClients.custom()
                    .setRoutePlanner(routePlanner)
                    .setDefaultCredentialsProvider(credentialsProvider)
                    .setDefaultRequestConfig(config)
                    .build();

            HttpGet httpget = new HttpGet(url);

            HttpResponse httpresponse = httpclient.execute(httpget);

//            System.out.println(httpresponse.getStatusLine());

            if(httpresponse.getStatusLine().getStatusCode() != HttpStatus.OK.value()){
                throw new Exception("Request failed");
            }

            long endTime = System.currentTimeMillis();

            sleep(2000);

            return endTime - startTime;
        }catch (Exception e){
            return -1;
        }

    }

    private void getStatusResult(List<Long> usedTimeList, int validCount, int totalCount){

        long sum = usedTimeList.stream().reduce(0L, Long::sum);

        long mean = sum / usedTimeList.size();

        long validPercent = validCount * 100l / totalCount;

        System.out.println(String.format("Total Count: %s", totalCount));
        System.out.println(String.format("Valid Count: %s", validCount));
        System.out.println(String.format("Valid Percent: %s", validPercent));
        System.out.println(String.format("Used Time Mean: %s", mean));

    }

    /**
     *
     */
    @Test
    public void chromedriverProxyWithAuthTest(){

        WebDriver driver = null;

        try {
            String baseUrl = "https://whatismyipaddress.com/";

            Proxy proxy = new org.openqa.selenium.Proxy();
            proxy.setSslProxy("zproxy.lum-superproxy.io" + ":" + 22225);

            proxy.setSocksUsername("lum-customer-ipower-zone-static");
            proxy.setSocksPassword("38rnyeoymh2g");

            driver = WebDriverUtils.getWebDriver(spiderConfig.getChromeDriverPath(), spiderConfig.getDownloadPath(), true, false);

            for (int i=0; i< 10; i++){

                driver.get(baseUrl);

                sleep(10000);

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(driver != null){
                driver.quit();
            }
        }

    }

    @Test
    public void firefoxDriverProxyWithAuthTest(){

        WebDriver driver = null;

        try {
            String baseUrl = "https://whatismyipaddress.com/";

            ProxyDO proxy = new ProxyDO();
            proxy.setIp("zproxy.lum-superproxy.io");
            proxy.setPort("22225");
            proxy.setUsername("lum-customer-ipower-zone-static-country-us");
            proxy.setPassword("38rnyeoymh2g");

            driver = WebDriverUtils.getFirefoxDriver(spiderConfig.getFirefoxDriverPath(), spiderConfig.getDownloadPath(), proxy, false);

            for (int i=0; i< 10; i++){

                driver.get(baseUrl);

                sleep(10000);

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(driver != null){
                driver.quit();
            }
        }

    }

    @Test
    public void phantomJsDriverProxyWithAuthTest(){

        WebDriver driver = null;

        try {
            String baseUrl = "https://whatismyipaddress.com/";

            ProxyDO proxy = new ProxyDO();
            proxy.setIp("zproxy.lum-superproxy.io");
            proxy.setPort("22225");
            proxy.setUsername("lum-customer-ipower-zone-static-country-us");
            proxy.setPassword("38rnyeoymh2g");

            driver = WebDriverUtils.getPhantomJSDriver(spiderConfig.getPhantomJsDriverPath(), spiderConfig.getDownloadPath(), proxy, false);

            for (int i=0; i< 10; i++){

                driver.get(baseUrl);

                sleep(10000);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(driver != null){
                driver.quit();
            }
        }

    }

    @Test
    public void chromedriverProxyWithoutAuthTest(){

        WebDriver driver = null;

        try {
            String baseUrl = "https://whatismyipaddress.com/";

            Proxy proxy = new org.openqa.selenium.Proxy();
            proxy.setSslProxy("zproxy.lum-superproxy.io" + ":" + 22225);

            proxy.setSocksUsername("lum-customer-ipower-zone-static-country-us");
            proxy.setSocksPassword("38rnyeoymh2g");

            driver = WebDriverUtils.getWebDriver(spiderConfig.getChromeDriverPath(), spiderConfig.getDownloadPath(), "35.233.232.84:3128", false);

            for (int i=0; i< 10; i++){

                driver.get(baseUrl);

                sleep(10000);

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(driver != null){
                driver.quit();
            }
        }

    }

}
