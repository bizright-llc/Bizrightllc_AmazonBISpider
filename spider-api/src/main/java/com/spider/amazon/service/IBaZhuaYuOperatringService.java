package com.spider.amazon.service;

import com.spider.amazon.cons.GetDataOfTaskByOffsetOperaTypeEnum;
import com.spider.amazon.cons.UpdateBzyTaskRuleOperaTypeEnum;

/**
 * @ClassName IBaZhuaYuOperatringService
 * @Description 业务对八爪鱼的操作服务
 */
public interface IBaZhuaYuOperatringService {
    /**
     * 根据类型来区分应该更换的任务列表信息
      */
    public void updateBzyTaskRuleForAllSku(UpdateBzyTaskRuleOperaTypeEnum operaTypeEnum);

    /**
     * 根据获取类型，偏移量接口来全量获取八爪鱼对应任务数据
     */
    public void getBzyTaskDataByByOffset(GetDataOfTaskByOffsetOperaTypeEnum operaTypeEnum,Object objDTO);

}