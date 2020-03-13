package com.spider.amazon.mapper;

import com.spider.amazon.model.SkuScrapyTaskVSkuListDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface SkuScrapyTaskVSkuListDOMapper {
    int insert(SkuScrapyTaskVSkuListDO record);

    int insertSelective(SkuScrapyTaskVSkuListDO record);

    int insertBatch(List<SkuScrapyTaskVSkuListDO> recordList);

    int updateVendorSkuByTaskId(Map<String, Object> paramMap);

}