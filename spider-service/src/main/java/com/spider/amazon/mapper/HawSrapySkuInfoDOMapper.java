package com.spider.amazon.mapper;

import com.spider.amazon.model.HawSrapySkuInfoDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface HawSrapySkuInfoDOMapper {
    int insert(HawSrapySkuInfoDO record);

    int insertSelective(HawSrapySkuInfoDO record);

    int insertBatch(List<HawSrapySkuInfoDO> recordList);

    List<HawSrapySkuInfoDO> queryInfoByTaskId(Map<String,Object> paramMap);

}