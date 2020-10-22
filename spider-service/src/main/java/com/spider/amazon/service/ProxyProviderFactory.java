package com.spider.amazon.service;

import com.spider.amazon.model.ProxyProvider;
import com.spider.amazon.service.impl.LuminatiProxyProviderService;
import com.spider.amazon.service.impl.TestProxyProviderService;

public class ProxyProviderFactory {

    /**
     * Get proxy provider service
     *
     * @param provider
     * @return
     */
    public static ProxyProviderService getProvider(ProxyProvider provider){
        if (provider == null){
            return null;
        }

        if(provider == ProxyProvider.LUMINATI){
            return new LuminatiProxyProviderService();
        }

        if (provider == ProxyProvider.TEST){
            return new TestProxyProviderService();
        }

        return null;
    }

}
