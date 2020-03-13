package com.spider.amazon.service.impl;

import com.spider.amazon.cons.GetDataOfTaskByOffsetOperaTypeEnum;
import com.spider.amazon.cons.UpdateBzyTaskRuleOperaTypeEnum;
import com.spider.amazon.handler.abs.AbstractGetDataByOffsetHandler;
import com.spider.amazon.handler.abs.AbstractUpdSpiderHandler;
import com.spider.amazon.handler.context.GetSpiderDataHandlerContext;
import com.spider.amazon.handler.context.UpdSpiderHandlerContext;
import com.spider.amazon.service.IBaZhuaYuOperatringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class BaZhuaYuOperatringServiceImpl implements IBaZhuaYuOperatringService{

    @Autowired
    private UpdSpiderHandlerContext handlerContext;

    @Autowired
    private GetSpiderDataHandlerContext getSpiderDataHandlerContext;
    /**
     * 根据所有SKU来更新任务流程参数值
     * 涉及到的爬虫任务类型
     * 1.商品通用信息 2.商品评论信息 3.商品购物车信息 4.商品排名信息 5.商品卖家列表信息
     * (策略者模式)
     */
    @Override
    public void updateBzyTaskRuleForAllSku(UpdateBzyTaskRuleOperaTypeEnum operaTypeEnum) {

        AbstractUpdSpiderHandler handler =handlerContext.getInstance(operaTypeEnum);
        handler.updateBzyTaskRuleForAllSku(operaTypeEnum);

    }

    @Override
    public void getBzyTaskDataByByOffset(GetDataOfTaskByOffsetOperaTypeEnum operaTypeEnum ,Object objDTO) {
        AbstractGetDataByOffsetHandler handler =getSpiderDataHandlerContext.getInstance(operaTypeEnum);
        handler.getAllBzyDataByOffset(operaTypeEnum,objDTO);
    }
}
