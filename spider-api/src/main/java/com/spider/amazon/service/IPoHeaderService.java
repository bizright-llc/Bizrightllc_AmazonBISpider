package com.spider.amazon.service;

import com.spider.amazon.dto.GetPOHeaderDTO;

/**
 * @ClassName IPoHeaderService
 * @Description BOP PoHeader信息服务
 */
public interface IPoHeaderService {

    /**
     * 获取BOP的PO信息
     * @return
     */
    public void getPOHeader(GetPOHeaderDTO requestDTO);

}
