package com.spider.amazon.service.impl;


import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.spider.amazon.config.BaZhuaYuConfiguration;
import com.spider.amazon.dto.BzyGetDataOfTaskByOffsetDTO;
import com.spider.amazon.dto.BzyGetTokenDTO;
import com.spider.amazon.dto.BzyUpdateTaskRuleDTO;
import com.spider.amazon.remote.api.BaZhuaYuAPI;
import com.spider.amazon.service.IBaZhuaYuService;
import com.spider.amazon.vo.BzyGetDataOfTaskByOffsetRepVO;
import com.spider.amazon.vo.BzyGetTokenRepVO;
import com.spider.amazon.vo.BzyUpdateTaskRuleRepVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 八爪鱼API请求服务
 */
@Service
@Slf4j
public class BaZhuaYuServiceImpl implements IBaZhuaYuService {

    @Autowired
    BaZhuaYuConfiguration baZhuaYuConfiguration;

    @Autowired
    RestTemplate restTemplate;

    /**
     * 请求令牌服务
     * @param bzyGetTokenDTO
     * @return
     */
    @Override
    public BzyGetTokenRepVO callBzyGetToken(BzyGetTokenDTO bzyGetTokenDTO) {
        //请求头设置
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        //提交参数设置
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("username", bzyGetTokenDTO.getUserName());
        params.add("password", bzyGetTokenDTO.getPassWord());
        params.add("grant_type", bzyGetTokenDTO.getGrantType());
        //提交请求
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(params, headers);
        final String uri = BaZhuaYuAPI.TOKEN;
        return restTemplate.postForObject(baZhuaYuConfiguration.getServer() + uri, entity, BzyGetTokenRepVO.class);
    }

    /**
     * 更改任务流程参数值
     * @param bzyUpdateTaskRuleDTO
     * @return
     */
    @Override
    public BzyUpdateTaskRuleRepVO callBzyUpdateTaskRule(BzyUpdateTaskRuleDTO bzyUpdateTaskRuleDTO) {
        //请求头设置
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(bzyUpdateTaskRuleDTO.getToken());
        Map<String, Object> params = new HashMap<>();
        params.put("taskId", bzyUpdateTaskRuleDTO.getTaskId());
        params.put("name", bzyUpdateTaskRuleDTO.getName());
        params.put("value", bzyUpdateTaskRuleDTO.getValue());
        //提交参数设置
        HttpEntity entity = new HttpEntity(params, headers);
        final String uri = BaZhuaYuAPI.UPDATE_TASKRULE;
        return restTemplate.postForObject(baZhuaYuConfiguration.getServer() + uri, entity, BzyUpdateTaskRuleRepVO.class);
    }

    @Override
    public BzyGetDataOfTaskByOffsetRepVO callGetDataOfTaskByOffset(BzyGetDataOfTaskByOffsetDTO bzyGetDataOfTaskByOffsetDTO) {
        //请求头设置
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bzyGetDataOfTaskByOffsetDTO.getToken());
        log.info("Token:[{}]",bzyGetDataOfTaskByOffsetDTO.getToken());
        //提交参数设置
        HttpEntity entity = new HttpEntity(headers);
        log.info("entity:[{}]",entity.toString());
        //提交参数设置
        final String uri = BaZhuaYuAPI.GET_DATAOFTASK_BY_OFFSET.replace("{taskId}",bzyGetDataOfTaskByOffsetDTO.getTaskId())
                .replace("{offset}",bzyGetDataOfTaskByOffsetDTO.getOffset())
                .replace("{size}",bzyGetDataOfTaskByOffsetDTO.getSize());
        String result = HttpRequest.get(baZhuaYuConfiguration.getServer()+uri)
                .header("Authorization", "Bearer "+bzyGetDataOfTaskByOffsetDTO.getToken())//头信息，多个头信息多次调用此方法即可
                .timeout(20000)//超时，毫秒
                .execute().body();
        return JSONUtil.toBean(result,BzyGetDataOfTaskByOffsetRepVO.class);
    }

}
