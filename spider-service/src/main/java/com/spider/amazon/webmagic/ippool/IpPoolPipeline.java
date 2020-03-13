package com.spider.amazon.webmagic.ippool;

import cn.hutool.core.util.ObjectUtil;
import com.spider.amazon.mapper.IpPoolDOMapper;
import com.spider.amazon.model.IpPoolDO;
import com.spider.amazon.utils.SpringContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.List;

@Slf4j
public class IpPoolPipeline implements Pipeline {

    @Autowired
    private IpPoolDOMapper ipPoolDOMapper= SpringContextUtils.getBean(IpPoolDOMapper.class);

    @Override
    public void process(ResultItems resultItems, Task task) {
        log.info("IpPool数据持久化过程 resultItems=>[{}] task=>[{}]",resultItems,task);
        // 提取有效ip列表进行入库
        Object listObj = resultItems.get("ipPoolDOList");
        if(ObjectUtil.isNotEmpty(listObj)) {
            List<IpPoolDO> ipPoolDOList = (List<IpPoolDO>) listObj;
            ipPoolDOMapper.insertBatch(ipPoolDOList);
        }

    }
}