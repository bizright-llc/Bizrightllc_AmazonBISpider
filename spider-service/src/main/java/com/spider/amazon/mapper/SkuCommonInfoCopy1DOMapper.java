package com.spider.amazon.mapper;

import com.spider.amazon.model.SkuCommonInfoCopy1DO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SkuCommonInfoCopy1DOMapper {
    int insert(SkuCommonInfoCopy1DO record);

    int insertSelective(SkuCommonInfoCopy1DO record);

    int updateIntoFlg(@Param("oldIntoFlg") String oldIntoFlg,@Param("newIntoFlg") String newIntoFlg);

}