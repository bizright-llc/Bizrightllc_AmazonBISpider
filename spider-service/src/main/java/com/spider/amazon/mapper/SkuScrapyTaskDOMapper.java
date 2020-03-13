package com.spider.amazon.mapper;

import com.spider.amazon.model.SkuScrapyTaskDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface SkuScrapyTaskDOMapper {
    int insert(SkuScrapyTaskDO record);

    int insertSelective(SkuScrapyTaskDO record);

    List<SkuScrapyTaskDO> selectTaskListByTaskSts(Map<String, Object> paramMap);

    int updateByTaskStsAndTaskId(Map<String, Object> paramMap);

    List<Map<String, Object>> selectByTaskId(Map<String, Object> paramMap);


    SkuScrapyTaskDO queryItemInfoByTaskId(Map<String, Object> paramMap);

}