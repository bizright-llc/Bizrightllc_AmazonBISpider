package com.spider.amazon.mapper;

import com.spider.amazon.model.FBAInventoryInfoDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

@Mapper
public interface FBAInventoryInfoDOMapper {
    int insert(FBAInventoryInfoDO record);

    int insertSelective(FBAInventoryInfoDO record);

    int selectCountByDate(Date date);

    int insertBatch(List<FBAInventoryInfoDO> recordList);

}