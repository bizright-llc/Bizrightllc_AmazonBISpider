package com.spider.amazon.mapper;

import com.spider.amazon.dto.AmazonAdConsumeSettingDTO;
import com.spider.amazon.model.AmazonAdConsumeItemDO;
import com.spider.amazon.model.AmazonAdConsumeLogDO;
import com.spider.amazon.model.AmazonAdConsumeSettingDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The class store the amazon ad consume
 *
 */
@Repository
@Mapper
public interface AmazonAdConsumeLogDOMapper {

    /**
     * Get all Log
     * @return
     */
    List<AmazonAdConsumeLogDO> getAll();

    /**
     *
     * @param settingId
     * @return
     */
    List<AmazonAdConsumeLogDO> getAllBySettingId(@Param("settingId") Long settingId);

    /**
     *
     * @param record
     */
    void insert(AmazonAdConsumeLogDO record);

}
