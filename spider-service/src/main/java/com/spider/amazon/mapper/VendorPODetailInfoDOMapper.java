package com.spider.amazon.mapper;

import com.spider.amazon.model.VendorPODetailInfoDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface VendorPODetailInfoDOMapper {

    int insert(VendorPODetailInfoDO record);

    int insertSelective(VendorPODetailInfoDO record);

    int insertBatch(List<VendorPODetailInfoDO> recordList);

}