package com.spider.amazon.service;

import com.spider.amazon.dto.*;
import com.spider.amazon.vo.*;

/**
 * @ClassName IBopService
 * @Description BOP请求服务
 */
public interface IBopService {

    /**
     * 调用BOP[VcPoInfo]
     * @return
     */
    public VcPoInfoRepVO callBopVcPoInfo(VcPoInfoDTO requestDTO);

    /**
     * 调用BOP[getPOHeader]
     * @return
     */
    public GetPOHeaderRepVO callBopGetPOHeader(GetPOHeaderDTO requestDTO);


    /**
     * 调用BOP[getWarehouseTransfer]
     * @return
     */
    public GetWarehouseTransferRepVO callBopGetWarehouseTransfer(GetWarehouseTransferDTO requestDTO);


    /**
     * 调用BOP[getInventoryDataDailySnapShot]
     * @return
     * TODO 目前BOP该API报错
     */
    public GetInventoryDataDailySnapShotRepVO callBopGetInventoryDataDailySnapShot(GetInventoryDataDailySnapShotDTO requestDTO);


    /**
     * 调用BOP[getInvoice]
     * @return
     */
    public GetInvoiceRepVO callBopGetInvoice(GetInvoiceDTO requestDTO);


}
