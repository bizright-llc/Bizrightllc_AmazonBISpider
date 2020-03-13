package com.spider.amazon.handler;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.exception.ServiceException;
import com.spider.amazon.config.BaZhuaYuConfiguration;
import com.spider.amazon.cons.BzyTaskParamEnum;
import com.spider.amazon.cons.GrantType;
import com.spider.amazon.cons.StrCons;
import com.spider.amazon.cons.UpdateBzyTaskRuleOperaTypeEnum;
import com.spider.amazon.cusinterface.SpiderUpdType;
import com.spider.amazon.dto.BzyGetTokenDTO;
import com.spider.amazon.dto.BzyUpdateTaskRuleDTO;
import com.spider.amazon.handler.abs.AbstractUpdSpiderHandler;
import com.spider.amazon.mapper.SkuInfoNewDOMapper;
import com.spider.amazon.model.SkuInfoNewDO;
import com.spider.amazon.service.impl.BaZhuaYuServiceImpl;
import com.spider.amazon.vo.BzyGetTokenRepVO;
import com.spider.amazon.vo.BzyUpdateTaskRuleRepVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@SpiderUpdType(UpdateBzyTaskRuleOperaTypeEnum.SKU_COMMON_INFO_UPD_RULE)
public class SkuCommonInfoUpdSpiderHandler extends AbstractUpdSpiderHandler {

    @Value("${bazhuayu.task.taskId.skucommoninfo}")
    private String skucommoninfo;
    @Value("${bazhuayu.task.taskId.skucommoninfo.urllistname}")
    private String urllistname;

    @Autowired
    private BaZhuaYuConfiguration baZhuaYuConfiguration;

    @Autowired
    private BaZhuaYuServiceImpl baZhuaYuServiceImpl;

    @Autowired
    private SkuInfoNewDOMapper skuInfoNewDOMapper;

    @Override
    public void updateBzyTaskRuleForAllSku(UpdateBzyTaskRuleOperaTypeEnum updateBzyTaskRuleOperaTypeEnum) {

        // TODO 处理商品通用消息更新任务列表值
        // 1.查询所有sku列表信息
        List<SkuInfoNewDO> resultList = skuInfoNewDOMapper.getAllSkuList();

        // 2.所有列表信息处理
        String value = dealResultList(resultList);

        // 3.调用八爪鱼更新任务API
        // 获取令牌值
        BzyGetTokenDTO bzyGetTokenDTO=BzyGetTokenDTO.builder().userName(baZhuaYuConfiguration.getUsername()).passWord(baZhuaYuConfiguration.getPassword()).grantType(GrantType.BY_PASSWORD).build();
        BzyGetTokenRepVO callBzyGetToken=baZhuaYuServiceImpl.callBzyGetToken(bzyGetTokenDTO);
        // TODO 目前暂时是直接用任务ID更新，完整流程为 获取令牌值＝》获取任务组状态＝》筛选任务组＝》获取任务组内任务列表＝》选择任务，获取ID＝》更新任务值
        // 更新任务值
        BzyUpdateTaskRuleDTO bzyUpdateTaskRuleDTO=BzyUpdateTaskRuleDTO.builder()
                .taskId(skucommoninfo)
                .name(StrUtil.concat(true,urllistname,BzyTaskParamEnum.URLLIST.getKey()))
                .value(value)
                .token(callBzyGetToken.getAccess_token())
                .build();
        BzyUpdateTaskRuleRepVO result=baZhuaYuServiceImpl.callBzyUpdateTaskRule(bzyUpdateTaskRuleDTO);
        if (log.isInfoEnabled()) {
            log.info("result:"+result);
        }

    }

    private String dealResultList(List<SkuInfoNewDO> resultList) {
        StringBuffer strbuff = new StringBuffer();
        int index = 0;
        strbuff.append(StrCons.PREFIX_1);
        for (SkuInfoNewDO result : resultList) {
            index++;
            if (index!=1) {
                strbuff.append(StrCons.SEPARATE_1);
            }
            strbuff.append(baZhuaYuConfiguration.getSkuDetailAddr()).append(result.getAsin());

        }
        strbuff.append(StrCons.SUFFIX_1);

        return strbuff.toString();
    }
}
