package com.spider.amazon.service.impl;

import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.dto.ProxyDTO;
import com.spider.amazon.service.RestService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.function.Supplier;

@Service
@Slf4j
public class RestServiceImpl implements RestService {

    @Autowired
    private SpiderConfig spiderConfig;

    /**
     * Second
     */
    int timeout = 60;

    int testProxyTimes = 2;

    @Override
    public boolean testProxy(ProxyDTO proxy) {

        return testProxy(proxy, 1);

    }

    /**
     *
     * @param proxy
     * @param times the times this proxy been test
     * @return
     */
    private boolean testProxy(ProxyDTO proxy, int times) {

        String url = "http://lumtest.com/myip.json";

        try{
            getPlainJSON(url, proxy);

            return true;
        }catch (Exception ex){
            log.error("Test Proxy Failed", ex);
            // Test again
            if(proxy != null && times <= testProxyTimes){
                return testProxy(proxy, times+1);
            }
            return false;
        }

    }

    /**
     * Get plain json response
     * @param url
     * @return
     */
    @Override
    public String getPlainJSON(String url) throws Exception {

        RestTemplate restTemplate = createRestTemplate();

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);

        if(responseEntity.getStatusCode() == HttpStatus.OK){
            return responseEntity.getBody();
        }else{
            log.error("[getPlainJSON] Request failed", responseEntity.toString());
            throw new Exception("Request failed");
        }

    }

    /**
     * Get plain json response with proxy
     *
     * @param url
     * @param proxyDto
     * @return
     * @throws Exception
     */
    @Override
    public String getPlainJSON(String url, ProxyDTO proxyDto) throws Exception {

        RestTemplate restTemplate = createRestTemplate(proxyDto);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        if(response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            log.error("[getPlainJSON] url: {}, proxy: {} Request failed", url, proxyDto.toString(), response.toString());
            throw new Exception("Request failed");
        }

    }

    @Override
    public <T> T[] get(String url, Class<T> type) {
        return null;
    }

    @Override
    public <T> T[] get(String url, Class<T> type, ProxyDTO proxy) {
        return null;
    }

    /**
     * Create rest template
     * @return
     */
    private RestTemplate createRestTemplate(){

        long timeout = 60l;

        Duration timeoutSetting = Duration.ofSeconds(timeout);

        return new RestTemplateBuilder().setConnectTimeout(timeoutSetting).setReadTimeout(timeoutSetting).build();
    }

    /**
     * Create rest template with proxy setting
     * if
     *
     * @param proxyDto
     * @return
     * @throws Exception
     */
    private RestTemplate createRestTemplate(ProxyDTO proxyDto) throws Exception {

        if (proxyDto == null){
            throw new IllegalArgumentException("[createRestTemplate] create rest template proxy cannot be null");
        }

        // no auth proxy
        if(StringUtils.isEmpty(proxyDto.getUsername()) || StringUtils.isEmpty(proxyDto.getPassword())){

            Proxy proxy = new Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(proxyDto.getIp(), Integer.valueOf(proxyDto.getPort())));
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setProxy(proxy);

            requestFactory.setConnectTimeout(timeout * 1000);
            requestFactory.setReadTimeout(timeout * 1000);

            return new RestTemplate(requestFactory);

        }else{
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(
                    new AuthScope(proxyDto.getIp(), Integer.valueOf(proxyDto.getPort())),
                    new UsernamePasswordCredentials(proxyDto.getUsername(), proxyDto.getPassword())
            );

            HttpHost myProxy = new HttpHost(proxyDto.getIp(), Integer.valueOf(proxyDto.getPort()));
            HttpClientBuilder clientBuilder = HttpClientBuilder.create();

            clientBuilder.setProxy(myProxy).setDefaultCredentialsProvider(credsProvider).disableCookieManagement();

            HttpClient httpClient = clientBuilder.build();
            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
            factory.setHttpClient(httpClient);
            factory.setConnectTimeout(timeout * 1000);
            factory.setReadTimeout(timeout * 1000);
            factory.setConnectionRequestTimeout(timeout * 1000);

            return new RestTemplate(factory);

        }

    }
}
