package com.spider.amazon.webmagic;

import com.spider.SpiderServiceApplication;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.dto.ProxyDTO;
import com.spider.amazon.model.ProxyDO;
import com.spider.amazon.service.RestService;
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
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.validation.constraints.AssertTrue;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.lang.Thread.sleep;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SpiderServiceApplication.class)
public class ProxyTest {

    @Autowired
    private SpiderConfig spiderConfig;

    @Autowired
    private RestService restService;

    /**
     * Test proxy
     */
    @Test
    public void testProxy() throws Exception {

        ProxyDTO proxyDto = ProxyDTO
                .builder()
                .ip("zproxy.lum-superproxy.io")
                .port("22225")
                .username("lum-customer-ipower-zone-static-country-us")
                .password("38rnyeoymh2g")
                .build();

        boolean result = restService.testProxy(proxyDto);

        Assert.assertTrue(result);

    }

    /**
     * Test proxy server get request
     *
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


        for (int i = 0; i < testCount; i++) {
            long usedTime = testProxy(proxy, url);

            totalCount++;

            if (usedTime != -1) {
                validCount++;
                usedTimeList.add(usedTime);
            }
        }

        getStatusResult(usedTimeList, validCount, totalCount);

    }

    /**
     * @param url
     * @return
     */
    private long testProxy(HttpHost proxy, String url) {

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

        try {
            long startTime = System.currentTimeMillis();

            HttpClient httpclient = HttpClients.custom()
                    .setRoutePlanner(routePlanner)
                    .setDefaultCredentialsProvider(credentialsProvider)
                    .setDefaultRequestConfig(config)
                    .build();

            HttpGet httpGet = new HttpGet(url);

            HttpResponse httpresponse = httpclient.execute(httpGet);

            if (httpresponse.getStatusLine().getStatusCode() != HttpStatus.OK.value()) {
                throw new Exception("Request failed");
            }

            long endTime = System.currentTimeMillis();

            sleep(2000);

            return endTime - startTime;
        } catch (Exception e) {
            return -1;
        }

    }

    private void getStatusResult(List<Long> usedTimeList, int validCount, int totalCount) {

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
    public void chromedriverProxyWithAuthTest() {

        WebDriver driver = null;

        try {
            String baseUrl = "http://www.amazon.com";

            driver = WebDriverUtils.getWebDriverWithProxy(spiderConfig.getChromeDriverPath(), spiderConfig.getDownloadPath(), spiderConfig.getChromeProxyFilepath(), false);

            for (int i = 0; i < 10; i++) {

                driver.get(baseUrl);

                sleep(10000);

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    @Test
    public void chromedriverProxyWithoutAuthTest() {

        WebDriver driver = null;

        try {
            String baseUrl = "http://lumtest.com/myip.json";

            ProxyDTO proxyDTO = new ProxyDTO();

            proxyDTO.setIp("108.59.14.203");
            proxyDTO.setPort("13010");

            driver = WebDriverUtils.getWebDriverWithProxy(spiderConfig.getChromeDriverPath(), spiderConfig.getDownloadPath(), proxyDTO, false);

            for (int i = 0; i < 10; i++) {

                driver.get(baseUrl);

                sleep(10000);

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }

    }

}
