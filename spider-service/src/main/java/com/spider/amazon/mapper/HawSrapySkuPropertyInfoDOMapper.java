package com.spider.amazon.mapper;

import com.spider.amazon.model.HawSrapySkuPropertyInfoDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface HawSrapySkuPropertyInfoDOMapper {
    int insert(HawSrapySkuPropertyInfoDO record);

    int insertSelective(HawSrapySkuPropertyInfoDO record);

    int insertBatch(List<HawSrapySkuPropertyInfoDO> recordList);

    List<HawSrapySkuPropertyInfoDO> queryItemListByTaskId(Map<String,Object> paramMap);

}