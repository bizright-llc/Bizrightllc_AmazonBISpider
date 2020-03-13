package com.spider.amazon.mapper;

import com.spider.amazon.model.SkuInfoNewDO;
import com.spider.amazon.model.SkuInfoNewDOKey;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SkuInfoNewDOMapper {
    int deleteByPrimaryKey(SkuInfoNewDOKey key);

    int insert(SkuInfoNewDO record);

    int insertSelective(SkuInfoNewDO record);

    SkuInfoNewDO selectByPrimaryKey(SkuInfoNewDOKey key);

    int updateByPrimaryKeySelective(SkuInfoNewDO record);

    int updateByPrimaryKey(SkuInfoNewDO record);

    List<SkuInfoNewDO> getAllSkuList();
}