package com.spider.amazon.webmagic.amzvc;

import cn.hutool.core.util.ObjectUtil;
import com.spider.amazon.mapper.VcPromotionInfoDOMapper;
import com.spider.amazon.mapper.VcPromotionProductInfoDOMapper;
import com.spider.amazon.model.VcPromotionInfoDO;
import com.spider.amazon.model.VcPromotionProductInfoDO;
import com.spider.amazon.utils.SpringContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.List;

@Slf4j
public class AmazonVcPromotionsPipeline implements Pipeline {

    private VcPromotionInfoDOMapper vcPromotionInfoDOMapper  = SpringContextUtils.getBean(VcPromotionInfoDOMapper.class);

    private VcPromotionProductInfoDOMapper vcPromotionProductInfoDOMapper  = SpringContextUtils.getBean(VcPromotionProductInfoDOMapper.class);

    @Override
    public void process(ResultItems resultItems, Task task) {
        log.info("promotions数据持久化过程 resultItems=>[{}] task=>[{}]",resultItems,task);

        Object listObj = resultItems.get("vcPromotionInfoDOList");
        if(ObjectUtil.isNotEmpty(listObj)) {
            List<VcPromotionInfoDO> vcPromotionInfoDOList = (List<VcPromotionInfoDO>) listObj;
            if(CollectionUtils.isNotEmpty(vcPromotionInfoDOList)) {
                vcPromotionInfoDOMapper.insertBatch(vcPromotionInfoDOList);
            }
        }

        Object listObj1 = resultItems.get("vcPromotionProductInfoDOList");
        if(ObjectUtil.isNotEmpty(listObj1)) {
            List<VcPromotionProductInfoDO> vcPromotionProductInfoDOList = (List<VcPromotionProductInfoDO>) listObj1;
            if(CollectionUtils.isNotEmpty(vcPromotionProductInfoDOList)) {
                vcPromotionProductInfoDOMapper.insertBatch(vcPromotionProductInfoDOList);
            }
        }


    }
}