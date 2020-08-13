package com.spider.amazon.mapper;

import com.spider.amazon.model.AsinSkuMapDO;
import com.spider.amazon.model.FBAInventoryInfoDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

@Mapper
public interface AsinSkuMapDOMapper {
    int insert(AsinSkuMapDO record);

    int insertSelective(AsinSkuMapDO record);

//    int update(AsinSkuMapDO record);

    int insertBatch(List<AsinSkuMapDO> recordList);

}