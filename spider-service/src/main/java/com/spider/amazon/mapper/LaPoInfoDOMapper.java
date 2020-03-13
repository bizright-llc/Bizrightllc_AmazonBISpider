package com.spider.amazon.mapper;

import com.spider.amazon.model.LaPoInfoDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LaPoInfoDOMapper {
    int insert(LaPoInfoDO record);

    int insertSelective(LaPoInfoDO record);


    int insertBatch(List<LaPoInfoDO> recordList);
}