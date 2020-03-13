package com.spider.amazon.mapper;

import com.spider.amazon.model.FbaPoInfoDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FbaPoInfoDOMapper {
    int insert(FbaPoInfoDO record);

    int insertSelective(FbaPoInfoDO record);

    int insertBatch(List<FbaPoInfoDO> recordList);

    int deleteAll();
}