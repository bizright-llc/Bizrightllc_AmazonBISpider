package com.spider.amazon.service.impl;


import cn.hutool.json.JSONUtil;
import com.spider.amazon.config.BopConfiguration;
import com.spider.amazon.dto.*;
import com.spider.amazon.remote.api.BopAPI;
import com.spider.amazon.service.IBopService;
import com.spider.amazon.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class BopServiceImpl implements IBopService {

    @Autowired
    BopConfiguration bopConfiguration;

    @Autowired
    RestTemplate restTemplate;

    @Override
    public VcPoInfoRepVO callBopVcPoInfo(VcPoInfoDTO requestDTO) {
        final String uri = BopAPI.VC_PO_INFO.replace("{pageNo}", String.valueOf(requestDTO.getPageNo()))
                .replace("{pageSize}", String.valueOf(requestDTO.getPageSize()))
                .replace("{enterDate}", requestDTO.getEnterDate())
                .replace("{lastUpdate}", requestDTO.getLastUpdate())
                .replace("{asin}", requestDTO.getAsin())
                .replace("{poNum}", requestDTO.getPoNum())
                .replace("{vendor}", requestDTO.getVendor());
        return JSONUtil.parseObj(restTemplate.postForObject(bopConfiguration.getServer() + uri, null, String.class)).toBean(VcPoInfoRepVO.class);
    }

    @Override
    public GetPOHeaderRepVO callBopGetPOHeader(GetPOHeaderDTO requestDTO) {
        final String uri = BopAPI.GET_PO_HEADER.replace("{pageNo}", String.valueOf(requestDTO.getPageNo()))
                .replace("{pageSize}", String.valueOf(requestDTO.getPageSize()))
                .replace("{asin}", requestDTO.getAsin())
                .replace("{poDate}", requestDTO.getPoDate());
        return JSONUtil.parseObj(restTemplate.postForObject(bopConfiguration.getServer() + uri, null, String.class)).toBean(GetPOHeaderRepVO.class);
    }

    @Override
    public GetWarehouseTransferRepVO callBopGetWarehouseTransfer(GetWarehouseTransferDTO requestDTO) {
        final String uri = BopAPI.GET_WAREHOUSE_TRANSFER.replace("{pageNo}", String.valueOf(requestDTO.getPageNo()))
                .replace("{pageSize}", String.valueOf(requestDTO.getPageSize()));
        return JSONUtil.parseObj(restTemplate.postForObject(bopConfiguration.getServer() + uri, null, String.class)).toBean(GetWarehouseTransferRepVO.class) ;
    }

    @Override
    public GetInventoryDataDailySnapShotRepVO callBopGetInventoryDataDailySnapShot(GetInventoryDataDailySnapShotDTO requestDTO) {
        // 更改超时时间
        HttpComponentsClientHttpRequestFactory rf =
                ((HttpComponentsClientHttpRequestFactory) restTemplate.getRequestFactory());
        rf.setConnectTimeout(60000);
        rf.setReadTimeout(60000);
        final String uri = BopAPI.GET_INVENTORY_DATA_DAILY_SNAPSHOT.replace("{PageNo}", String.valueOf(requestDTO.getPageNo()))
                .replace("{PageSize}", String.valueOf(requestDTO.getPageSize()));
        return JSONUtil.parseObj(restTemplate.postForObject(bopConfiguration.getServer() + uri, null, String.class)).toBean(GetInventoryDataDailySnapShotRepVO.class);
    }

    @Override
    public GetInvoiceRepVO callBopGetInvoice(GetInvoiceDTO requestDTO) {
        final String uri = BopAPI.GET_INVOICE.replace("{pageNo}", String.valueOf(requestDTO.getPageNo()))
                .replace("{pageSize}", String.valueOf(requestDTO.getPageSize()))
                .replace("{asin}", String.valueOf(requestDTO.getAsin()))
                .replace("{channel}", String.valueOf(requestDTO.getChannel()))
                .replace("{invoiceDate}", String.valueOf(requestDTO.getInvoiceDate()));
        return JSONUtil.parseObj(restTemplate.postForObject(bopConfiguration.getServer() + uri, null, String.class)).toBean(GetInvoiceRepVO.class);
    }

}
