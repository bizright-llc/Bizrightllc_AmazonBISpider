package com.spider.amazon.service;

import com.spider.amazon.dto.VcPoInfoDTO;

/**
 * @ClassName IVendorPoInfoService
 * @Description Vendor Po信息服务
 */
public interface IVendorPoInfoService {

    /**
     * 获取BOP VC的PO信息
     * @return
     */
    public void getVcPoInfo(VcPoInfoDTO vcPoInfoDTO);

}
