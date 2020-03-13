package com.spider.amazon.mapper;

import com.spider.amazon.model.InvoiceInfoInfoDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface InvoiceInfoInfoDOMapper {
    int insert(InvoiceInfoInfoDO record);

    int insertSelective(InvoiceInfoInfoDO record);

    int insertBatch(List<InvoiceInfoInfoDO> recordList);
}