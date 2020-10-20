package com.spider.amazon.service.impl;

import com.spider.amazon.dto.ProviderProxyDTO;
import com.spider.amazon.dto.ProxyDTO;
import com.spider.amazon.model.ProxyProvider;
import com.spider.amazon.service.ProxyProviderService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * This is example proxy provider service
 */
public class TestProxyProviderService implements ProxyProviderService {

    @Override
    public List<ProviderProxyDTO> getActiveProxies() {

        Random rand = new Random();

        int randInt = rand.nextInt(2000);


        List<ProviderProxyDTO> results = new ArrayList<>();
        for (int i =0; i<randInt; i++){

            ProviderProxyDTO proxy = new ProviderProxyDTO();
            proxy.setIp(String.format("0.0.%s.%s", i/255, i%255));
            proxy.setPort(String.valueOf(i));
            proxy.setProvider(ProxyProvider.TEST);
            proxy.setSelfRotating(i%2 == 1);

            results.add(proxy);
        }

        return results;

    }

    @Override
    public List<ProviderProxyDTO> getActiveRotatingProxies() {
        return new ArrayList<>();
    }

}
