package com.spider.amazon.service;

import com.spider.amazon.dto.BzyGetDataOfTaskByOffsetDTO;
import com.spider.amazon.dto.BzyGetTokenDTO;
import com.spider.amazon.dto.BzyUpdateTaskRuleDTO;
import com.spider.amazon.vo.BzyGetDataOfTaskByOffsetRepVO;
import com.spider.amazon.vo.BzyGetTokenRepVO;
import com.spider.amazon.vo.BzyUpdateTaskRuleRepVO;

/**
 * @ClassName IBaZhuaYuService
 * @Description 八爪鱼请求服务
 */
public interface IBaZhuaYuService {

    /**
     * 获取令牌
     * @param bzyGetTokenDTO
     * @return
     */
    public BzyGetTokenRepVO callBzyGetToken(BzyGetTokenDTO bzyGetTokenDTO);


    /**
     * 更改任务流程参数值
     * @param bzyUpdateTaskRuleDTO
     * @return
     */
    public BzyUpdateTaskRuleRepVO callBzyUpdateTaskRule(BzyUpdateTaskRuleDTO bzyUpdateTaskRuleDTO);


    /**
     * 获取任务结果，偏移量获取
     */
    public BzyGetDataOfTaskByOffsetRepVO callGetDataOfTaskByOffset(BzyGetDataOfTaskByOffsetDTO bzyGetDataOfTaskByOffsetDTO);


}
