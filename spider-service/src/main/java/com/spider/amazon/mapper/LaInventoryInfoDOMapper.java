package com.spider.amazon.mapper;

import com.spider.amazon.model.LaInventoryInfoDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LaInventoryInfoDOMapper {
    int insert(LaInventoryInfoDO record);

    int insertSelective(LaInventoryInfoDO record);

    int insertBatch(List<LaInventoryInfoDO> recordList);

}