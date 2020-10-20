package com.spider.amazon.service.impl;

import com.spider.amazon.dto.ProviderProxyDTO;
import com.spider.amazon.service.ProxyProviderService;

import java.util.ArrayList;
import java.util.List;

/**
 * Luminati proxy provider service
 */
public class LuminatiProxyProviderService implements ProxyProviderService {

    @Override
    public List<ProviderProxyDTO> getActiveProxies() {
        return new ArrayList<>();
    }

    @Override
    public List<ProviderProxyDTO> getActiveRotatingProxies() {
        return new ArrayList<>();
    }

}
