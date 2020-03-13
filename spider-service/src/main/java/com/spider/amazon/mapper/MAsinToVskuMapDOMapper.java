package com.spider.amazon.mapper;

import com.spider.amazon.model.MAsinToVskuMapDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MAsinToVskuMapDOMapper {
    int insert(MAsinToVskuMapDO record);

    int insertSelective(MAsinToVskuMapDO record);

    int insertOrUpdateRecord(MAsinToVskuMapDO record);

}