package com.spider.amazon.mapper;

import com.spider.amazon.model.reviewsInfoDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface reviewsInfoDOMapper {
    int insert(reviewsInfoDO record);

    int insertSelective(reviewsInfoDO record);


}