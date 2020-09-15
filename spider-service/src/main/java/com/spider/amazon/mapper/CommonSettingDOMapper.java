package com.spider.amazon.mapper;

import com.spider.amazon.model.AsinSkuMapDO;
import com.spider.amazon.model.CommonSettingDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The mapper for CommenSettingDOMapper
 * check xml file {@link #CommonSettingDOMapper.xml}
 */
@Repository
@Mapper
public interface CommonSettingDOMapper {

    CommonSettingDO getByValueName(@Param("name") String name);

    int insert(CommonSettingDO record);

    int update(CommonSettingDO record);

    int insertBatch(List<CommonSettingDO> recordList);

}