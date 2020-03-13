package com.spider.amazon.service;

import com.spider.amazon.dto.GetInvoiceDTO;

/**
 * @ClassName IInvoiceService
 * @Description BOP Invoice信息服务
 */
public interface IInvoiceService {

    /**
     * 获取BOP的Invoice信息
     * @return
     */
    public void getInvoice(GetInvoiceDTO requestDTO);

}
