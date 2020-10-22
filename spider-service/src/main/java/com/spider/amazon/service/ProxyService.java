package com.spider.amazon.service;

import com.common.exception.RepositoryException;
import com.spider.amazon.dto.ProxyDTO;

import java.io.IOException;
import java.util.List;

/**
 * Proxy service connect to database and proxy provider refresh
 * database proxy data
 *
 */
public interface ProxyService {

    /**
     * Refresh ip pool get from provider
     */
    public void refreshIpPool();

    /**
     * Test all active proxy in ip pool
     */
    public void testIpPool();

    /**
     * Get random active proxy not include self rotating
     * Mark used time when get proxy
     *
     * @return
     */
    public ProxyDTO getRandomActiveProxy();

    /**
     * Get random active proxy which is self rotating
     * Mark used time when get proxy
     *
     * @return
     */
    public ProxyDTO getRandomActiveSelfRotatingProxy();

    /**
     *  Get random active proxy
     *
     * @return
     */
    public List<ProxyDTO> getRandomActiveProxy(int count);

    /**
     * Get all proxy is active and last used time
     * is over cooling time
     *
     * @return
     */
    public List<ProxyDTO> getAllProxies();

    /**
     * Insert proxy
     * if the proxy alread exist in the database
     * it will update its last check time
     *
     * @param proxies
     */
    public void addProxies(List<ProxyDTO> proxies);

    /**
     * Marked last used
     * @param ipPool
     */
    public void setProxyUsed(ProxyDTO ipPool) throws RepositoryException;

    public boolean isValid(ProxyDTO proxyDTO) throws IOException;

}
