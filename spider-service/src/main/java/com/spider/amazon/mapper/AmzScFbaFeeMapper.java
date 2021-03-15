package com.spider.amazon.mapper;

import com.spider.amazon.entity.AmzScFbaFee;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * Amazon Seller central daily page sales and traffic entity mapper
 *
 */
@Repository
@Mapper
public interface AmzScFbaFeeMapper {

    void insert(AmzScFbaFee record);

}