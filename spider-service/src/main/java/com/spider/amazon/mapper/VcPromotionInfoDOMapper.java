package com.spider.amazon.mapper;

import com.spider.amazon.model.VcPromotionInfoDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface VcPromotionInfoDOMapper {
    int insert(VcPromotionInfoDO record);

    int insertSelective(VcPromotionInfoDO record);

    int insertBatch(List<VcPromotionInfoDO> recordList);

}