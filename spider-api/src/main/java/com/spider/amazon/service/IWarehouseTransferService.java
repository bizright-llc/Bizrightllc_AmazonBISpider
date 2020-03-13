package com.spider.amazon.service;

import com.spider.amazon.dto.GetWarehouseTransferDTO;

/**
 * @ClassName IWarehouseTransferService
 * @Description Warehouse Transfer信息服务
 */
public interface IWarehouseTransferService {

    /**
     * 获取BOP的WarehouseTransfer信息
     * @return
     */
    public void getWarehouseTransfer(GetWarehouseTransferDTO requestDTO);

}
