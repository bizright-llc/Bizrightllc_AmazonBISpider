package com.spider.amazon.mapper;

import com.spider.amazon.entity.AmzVcDailyInventory;
import com.spider.amazon.entity.AmzVcDailySales;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * Amazon Vendor central daily inventory entity mapper
 *
 */
@Repository
@Mapper
public interface AmzVcDailyInventoryMapper {

    void insert(AmzVcDailyInventory record);

}