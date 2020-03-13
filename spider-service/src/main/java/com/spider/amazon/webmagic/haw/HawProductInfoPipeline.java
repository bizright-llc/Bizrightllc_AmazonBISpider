package com.spider.amazon.webmagic.haw;

import cn.hutool.core.util.ObjectUtil;
import com.spider.amazon.cons.TaskSts;
import com.spider.amazon.mapper.HawSrapySkuInfoDOMapper;
import com.spider.amazon.mapper.HawSrapySkuPropertyInfoDOMapper;
import com.spider.amazon.mapper.SkuScrapyTaskDOMapper;
import com.spider.amazon.model.HawSrapySkuInfoDO;
import com.spider.amazon.model.HawSrapySkuPropertyInfoDO;
import com.spider.amazon.utils.SpringContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class HawProductInfoPipeline implements Pipeline {

    private HawSrapySkuInfoDOMapper hawSrapySkuInfoDOMapper  = SpringContextUtils.getBean(HawSrapySkuInfoDOMapper.class);

    private HawSrapySkuPropertyInfoDOMapper hawSrapySkuPropertyInfoDOMapper  = SpringContextUtils.getBean(HawSrapySkuPropertyInfoDOMapper.class);

    private SkuScrapyTaskDOMapper skuScrapyTaskDOMapper  = SpringContextUtils.getBean(SkuScrapyTaskDOMapper.class);


    @Override
    public void process(ResultItems resultItems, Task task) {
        log.info("Haw数据持久化过程 resultItems=>[{}] task=>[{}]",resultItems,task);
        // 提取商品详情所有信息
        Object listObj = resultItems.get("hawSrapySkuInfoDOList");
        if(ObjectUtil.isNotEmpty(listObj)) {
            List<HawSrapySkuInfoDO> HawSrapySkuInfoDOList = (List<HawSrapySkuInfoDO>) listObj;
            if(CollectionUtils.isNotEmpty(HawSrapySkuInfoDOList)) {
                hawSrapySkuInfoDOMapper.insertBatch(HawSrapySkuInfoDOList);
            }
        }

        // 商品属性列表入库
        Object propertiesListObj = resultItems.get("hawSrapySkuPropertyInfoDOList");
        if(ObjectUtil.isNotEmpty(propertiesListObj)) {
            List<List<HawSrapySkuPropertyInfoDO>> hawSrapySkuPropertyInfoDOList = (List<List<HawSrapySkuPropertyInfoDO>>) propertiesListObj;
            for (List<HawSrapySkuPropertyInfoDO> pList: hawSrapySkuPropertyInfoDOList) {
                if(CollectionUtils.isNotEmpty(pList)) {
                    hawSrapySkuPropertyInfoDOMapper.insertBatch(pList);
                }
            }
        }

        // 更新任务为数据抓取成功
        String taskId = resultItems.get("taskId");
        Map<String,Object> params=new HashMap<>();
        params.put("taskId",taskId);
        params.put("taskSts", TaskSts.TASK_SCRAPYSUCC);
        params.put("oldTaskSts", TaskSts.TASK_SCRAPY);
        log.info("更新任务为数据获取成功 params=>[{}]",params);
        skuScrapyTaskDOMapper.updateByTaskStsAndTaskId(params);

    }
}