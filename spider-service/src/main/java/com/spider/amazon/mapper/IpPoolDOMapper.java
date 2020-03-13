package com.spider.amazon.mapper;

import com.spider.amazon.model.IpPoolDO;
import com.spider.amazon.model.IpPoolDOKey;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IpPoolDOMapper {
    int deleteByPrimaryKey(IpPoolDOKey key);

    int insert(IpPoolDO record);

    int insertSelective(IpPoolDO record);

    int insertBatch(List<IpPoolDO> recordList);

}