package com.spider.amazon.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spider.amazon.entity.Cookie;
import com.spider.amazon.mapper.CommonSettingDOMapper;
import com.spider.amazon.model.CommonSettingDO;
import com.spider.amazon.model.Consts;
import com.spider.amazon.service.CommonSettingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * CommonSettingServiceImpl implement {@link CommonSettingService}
 *
 *
 */
@Service
@Slf4j
public class CommonSettingServiceImpl implements CommonSettingService {

    private CommonSettingDOMapper commonSettingDOMapper;

    @Autowired
    public CommonSettingServiceImpl(CommonSettingDOMapper commonSettingDOMapper) {
        this.commonSettingDOMapper = commonSettingDOMapper;
    }


    @Override
    public String getValue(String name) {
        if(StringUtils.isEmpty(name)){
            throw new IllegalArgumentException("Cannot get empty common setting");
        }

        CommonSettingDO settingDO = commonSettingDOMapper.getByValueName(name);

        if(settingDO == null) {
            return "";
        }else{
            return settingDO.getValue();
        }
    }

    @Override
    public void setValue(String name, String value, String userId) {

        if(StringUtils.isEmpty(name)){
            throw new IllegalArgumentException("Cannot set common setting with empty name");
        }

        CommonSettingDO settingDO = commonSettingDOMapper.getByValueName(name);

        // set new value
        if(settingDO == null){

            CommonSettingDO newSetting = new CommonSettingDO();
            newSetting.setName(name);
            newSetting.setValue(value);
            newSetting.setCreatedBy(userId);
            newSetting.setUpdatedBy(userId);

            commonSettingDOMapper.insert(newSetting);

        }else{
            //update value

            settingDO.setValue(value);
            settingDO.setUpdatedBy(userId);

            commonSettingDOMapper.update(settingDO);

        }

    }

    @Override
    public List<Cookie> getAmazonVCCookies() {
        String cookiesStr = getValue(Consts.AMAZON_VC_COOKIES);

        ObjectMapper objectMapper = new ObjectMapper();

        List<Cookie> cookies = new ArrayList<>();

        if(cookiesStr == null || StringUtils.isEmpty(cookiesStr)){
            return cookies;
        }

        try{
            cookies = objectMapper.readValue(cookiesStr, new TypeReference<List<Cookie>>() {});
        }catch (Exception ex){
            log.info("[Parse amazon vc cookies failed]");
            log.info(ex.getLocalizedMessage());
        }

        return cookies;
    }

    @Override
    public List<Cookie> getAmazonSCCookies() {
        String cookiesStr = getValue(Consts.AMAZON_SC_COOKIES);

        ObjectMapper objectMapper = new ObjectMapper();

        List<Cookie> cookies = new ArrayList<>();

        if(cookiesStr == null || StringUtils.isEmpty(cookiesStr)){
            return cookies;
        }

        try{
            cookies = objectMapper.readValue(cookiesStr, new TypeReference<List<Cookie>>() {});
        }catch (Exception ex){
            log.info("[Parse amazon vc cookies failed]");
            log.info(ex.getLocalizedMessage());
        }

        return cookies;
    }
}
