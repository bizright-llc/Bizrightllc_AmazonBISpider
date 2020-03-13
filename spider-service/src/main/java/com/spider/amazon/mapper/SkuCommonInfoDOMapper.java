package com.spider.amazon.mapper;

import com.spider.amazon.model.SkuCommonInfoDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SkuCommonInfoDOMapper {
    int insert(SkuCommonInfoDO record);

    int insertSelective(SkuCommonInfoDO record);

    int insertByCopyTableIntoFlg (@Param("intoFlg") String intoFlg);

}