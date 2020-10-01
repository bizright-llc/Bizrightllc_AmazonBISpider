package com.spider.amazon.mapper;

import com.spider.amazon.entity.AmzScBuyBox;
import com.spider.amazon.entity.AmzVcDailyInventory;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * Amazon Seller central daily page sales and traffic entity mapper
 *
 */
@Repository
@Mapper
public interface AmzScBuyBoxMapper {

    void insert(AmzScBuyBox record);

}