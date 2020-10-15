package com.spider.amazon.service.impl;

import com.common.exception.RepositoryException;
import com.spider.amazon.dto.ProxyDTO;
import com.spider.amazon.mapper.ProxyDOMapper;
import com.spider.amazon.model.ProxyDO;
import com.spider.amazon.service.ProxyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.javassist.tools.rmi.ObjectNotFoundException;
import org.modelmapper.ModelMapper;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.session.NonUniqueSessionRepositoryException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.net.*;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

@Service
@Slf4j
public class ProxyServiceImpl implements ProxyService {

    private static String TEST_URL = "http://www.amazon.com/";

    private ProxyDOMapper proxyDOMapper;

    private ModelMapper modelMapper;

    private PlatformTransactionManager transactionManager;

    @Autowired
    public ProxyServiceImpl(ProxyDOMapper proxyDOMapper, ModelMapper modelMapper, PlatformTransactionManager transactionManager) {
        this.proxyDOMapper = proxyDOMapper;
        this.modelMapper = modelMapper;
        this.transactionManager = transactionManager;
    }

    /**
     * Check proxy valid in database
     */
    @Override
    public void refreshIpPool() {

    }

    @Override
    public List<ProxyDTO> getRandomActiveProxy(int count) {

        if (count <= 0){
            count = 100;
        }

        return proxyDOMapper.getRandomActiveProxiesCount(count).stream().map(this::doToDTO).collect(Collectors.toList());

    }

    @Override
    public List<ProxyDTO> getAllProxies() {
        return null;
    }


    /**
     *
     * @param proxies
     */
    @Override
    public void addProxies(List<ProxyDTO> proxies) {

        final TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        try{

            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    for (ProxyDTO proxyDTO: proxies) {

                        ProxyDO existProxy = proxyDOMapper.getProxyByIpAndPort(proxyDTO.getIp(), proxyDTO.getPort());
                        ProxyDO newOrUpdateProxy = dtoToDO(proxyDTO);

                        // update exist proxy
                        if (existProxy != null){

                            newOrUpdateProxy.setId(existProxy.getId());

                            proxyDOMapper.update(newOrUpdateProxy);
                        }else{
                            // insert new proxy

                            proxyDOMapper.insertSelective(newOrUpdateProxy);

                        }
                    }
                }
            });

        }catch (ExecutorException e){
            log.error("[addProxies] ERROR FIX", e);
            throw e;
        }catch (MyBatisSystemException e){
            log.error("[addProxies] ERROR FIX", e);
            throw e;
        }
        catch (Exception e){
            log.error("[addProxies] ERROR", e);
            throw e;
        }

    }

    @Override
    public void setProxyUsed(ProxyDTO proxy) throws RepositoryException {

        try{

            proxyDOMapper.markProxyUsedTime(proxy.getId());

        }catch (Exception e){

            log.error("[setProxyUsed] ERROR FIX THIS", e);

            throw new RepositoryException(e);
        }
    }

    /**
     * Check proxy is valid or not
     *
     */
    @Override
    public boolean isValid(ProxyDTO proxyDTO) throws IOException {

        if(proxyDTO == null){
            throw new IllegalArgumentException("Test proxy cannot be null");
        }

        if (StringUtils.isEmpty(proxyDTO.getIp()) || StringUtils.isEmpty(proxyDTO.getPort())){
            throw new IllegalArgumentException("Test proxy host or port cannot be empty");
        }

        if (StringUtils.isNotEmpty(proxyDTO.getUsername())){
            return testProxyWithAuth(proxyDTO);
        }else{
            return testProxyWithoutAuth(proxyDTO);
        }

    }

    private boolean testProxyWithoutAuth(ProxyDTO proxyDTO) throws IOException {

        log.debug("[testProxyWithoutAuth] test proxy host: {}, port: {}", proxyDTO.getIp(), proxyDTO.getPort());

        try {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyDTO.getIp(), Integer.valueOf(proxyDTO.getPort())));

            URLConnection httpCon = new URL("https://www.amazon.com/").openConnection(proxy);
            httpCon.setConnectTimeout(5000);
            httpCon.setReadTimeout(5000);
            int code = ((HttpURLConnection) httpCon).getResponseCode();

            return code == 200;
        } catch (IOException e) {

            log.error("[testProxyWithoutAuth] throw exception", e);

            throw e;

        }catch (Exception e){
            log.error("[testProxyWithoutAuth] throw exception", e);

            throw e;
        }
    }

    private boolean testProxyWithAuth(ProxyDTO proxyDTO) throws IOException {

        log.debug("[testProxyWithAuth] test proxy host: {}, port: {}, username: {}, password: {}", proxyDTO.getIp(), proxyDTO.getPort(), proxyDTO.getUsername(), proxyDTO.getPassword());

        try{
            HttpHost proxy = new HttpHost(proxyDTO.getIp(), Integer.valueOf(proxyDTO.getPort()));

            DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);

            //Client credentials
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(new AuthScope(proxy),
                    new UsernamePasswordCredentials(proxyDTO.getUsername(), proxyDTO.getPassword()));

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

            HttpClient httpclient = HttpClients.custom()
                    .setRoutePlanner(routePlanner)
                    .setDefaultCredentialsProvider(credentialsProvider)
                    .setDefaultRequestConfig(config)
                    .build();

            HttpGet httpget = new HttpGet(TEST_URL);

            HttpResponse httpresponse = httpclient.execute(httpget);

            if(httpresponse.getStatusLine().getStatusCode() != HttpStatus.OK.value()){
                throw new Exception("Request failed");
            }

            return httpresponse.getStatusLine().getStatusCode() == HttpStatus.OK.value();

        }catch (IOException e){

            throw e;

        } catch (Exception e){

            log.error("[testProxy] test proxy id {} failed", proxyDTO.getId(), e);

            return false;
        }
    }

    /**
     * DO to DTO
     * @param proxyDO
     * @return
     */
    private ProxyDTO doToDTO(ProxyDO proxyDO){
        return modelMapper.map(proxyDO, ProxyDTO.class);
    }

    /**
     * DTO to DO
     * @param proxyDTO
     * @return
     */
    private ProxyDO dtoToDO(ProxyDTO proxyDTO){
        return modelMapper.map(proxyDTO, ProxyDO.class);
    }
}
