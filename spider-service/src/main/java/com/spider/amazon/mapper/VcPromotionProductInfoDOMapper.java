package com.spider.amazon.mapper;

import com.spider.amazon.model.VcPromotionProductInfoDO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface VcPromotionProductInfoDOMapper {

    int insert(VcPromotionProductInfoDO record);

    int insertSelective(VcPromotionProductInfoDO record);

    int insertBatch(List<VcPromotionProductInfoDO> recordList);

}