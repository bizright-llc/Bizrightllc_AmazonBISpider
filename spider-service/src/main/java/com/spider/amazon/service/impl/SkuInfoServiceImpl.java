package com.spider.amazon.service.impl;

import com.common.exception.ServiceException;
import com.spider.amazon.cons.RespErrorEnum;
import com.spider.amazon.cons.SqlResult;
import com.spider.amazon.mapper.SkuCommonInfoCopy1DOMapper;
import com.spider.amazon.mapper.SkuCommonInfoDOMapper;
import com.spider.amazon.mapper.SkuInfoNewDOMapper;
import com.spider.amazon.model.SkuInfoNewDO;
import com.spider.amazon.service.ISkuInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class SkuInfoServiceImpl implements  ISkuInfoService {
    @Autowired
    private SkuInfoNewDOMapper skuInfoNewDOMapper;

    @Autowired
    private SkuCommonInfoCopy1DOMapper skuCommonInfoCopy1DOMapper;

    @Autowired
    private SkuCommonInfoDOMapper skuCommonInfoDOMapper;

    /**
     * 获取所有SKU信息
     * @return
     */
    @Override
    public List<SkuInfoNewDO> getAllSkuList() {

        // 1.获取所有SKU列表
        List<SkuInfoNewDO> resultList=skuInfoNewDOMapper.getAllSkuList();

        return resultList;
    }

    /**
     * 从复制表入库商品通用信息表
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int intoSkuCommonInfoByCopy() {

        // 1.更新要入库记录标志为W
        int result=skuCommonInfoCopy1DOMapper.updateIntoFlg("N" ,"W");
        if (result== SqlResult.NO_RECORD) {
            return SqlResult.NO_RECORD;
        } else if (result== SqlResult.FAILD) {
            throw new ServiceException(RespErrorEnum.SERVICE_DATA_EXPC.getSubStatusCode(),RespErrorEnum.SERVICE_DATA_EXPC.getSubStatusMsg());
        }

        // 2.获取入库标志为W的记录进行入库
        int result1=skuCommonInfoDOMapper.insertByCopyTableIntoFlg("W");
        if (result1== SqlResult.NO_RECORD) {
            return SqlResult.NO_RECORD;
        } else if (result1== SqlResult.FAILD) {
            throw new ServiceException(RespErrorEnum.SERVICE_DATA_EXPC.getSubStatusCode(),RespErrorEnum.SERVICE_DATA_EXPC.getSubStatusMsg());
        }

        // 3.入库成功,标志更新
        int result2=skuCommonInfoCopy1DOMapper.updateIntoFlg("W" ,"Y");
        if (result2<=SqlResult.NO_RECORD) {
            throw new ServiceException(RespErrorEnum.SERVICE_DATA_EXPC.getSubStatusCode(),RespErrorEnum.SERVICE_DATA_EXPC.getSubStatusMsg());
        }

        return result1;
    }


}
