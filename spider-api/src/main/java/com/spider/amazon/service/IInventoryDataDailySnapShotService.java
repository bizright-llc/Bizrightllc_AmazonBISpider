package com.spider.amazon.service;

import com.spider.amazon.dto.GetInventoryDataDailySnapShotDTO;

/**
 * @ClassName IInventoryDataDailySnapShotService
 * @Description InventoryDataDailySnapShot信息服务
 */
public interface IInventoryDataDailySnapShotService {

    /**
     * 获取BOP的InventoryDataDailySnapShot信息
     * @return
     */
    public void getInventoryDataDailySnapShot(GetInventoryDataDailySnapShotDTO requestDTO);



}
