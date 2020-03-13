package com.spider.amazon.mapper;

import com.spider.amazon.model.VskuToPidMapDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VskuToPidMapDOMapper {
    int insert(VskuToPidMapDO record);

    int insertSelective(VskuToPidMapDO record);

    int insertOrUpdateRecord(VskuToPidMapDO record);

}