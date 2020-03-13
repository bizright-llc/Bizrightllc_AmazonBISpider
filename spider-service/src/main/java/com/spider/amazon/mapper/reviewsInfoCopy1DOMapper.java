package com.spider.amazon.mapper;

import com.spider.amazon.model.reviewsInfoCopy1DO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface reviewsInfoCopy1DOMapper {
    int insert(reviewsInfoCopy1DO record);

    int insertSelective(reviewsInfoCopy1DO record);

    int insertBatch(List<reviewsInfoCopy1DO> recordList);

}