package com.spider.amazon.webmagic.ippool;

import cn.hutool.core.util.ObjectUtil;
import com.common.exception.RepositoryException;
import com.spider.amazon.dto.ProxyDTO;
import com.spider.amazon.mapper.ProxyDOMapper;
import com.spider.amazon.model.ProxyDO;
import com.spider.amazon.service.ProxyService;
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
    private ProxyService proxyService;

    @Autowired
    private ProxyDOMapper proxyDOMapper = SpringContextUtils.getBean(ProxyDOMapper.class);

    @Override
    public void process(ResultItems resultItems, Task task) {
        log.info("IpPool数据持久化过程 resultItems=>[{}] task=>[{}]",resultItems,task);

        // 提取有效ip列表进行入库
        Object listObj = resultItems.get("ipPoolDTOList");
        if(ObjectUtil.isNotEmpty(listObj)) {
            List<ProxyDTO> proxyDTOList = (List<ProxyDTO>) listObj;
            proxyService.addProxies(proxyDTOList);
        }

    }
}