package com.spider.amazon.mapper;

import com.spider.amazon.entity.AmzVcDailySales;
import com.spider.amazon.model.AsinSkuMapDO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface AmzVcDailySalesMapper {

    void insert(AmzVcDailySales record);

//    int insertSelective(AmzVcDailySales record);
//
//    int insertBatch(List<AmzVcDailySales> recordList);

}