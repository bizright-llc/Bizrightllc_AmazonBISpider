package com.spider.amazon.mapper;

import com.spider.amazon.model.AmazonAdConsumeItemDO;
import com.spider.amazon.model.AmazonAdConsumeLogDO;
import com.spider.amazon.model.AmazonAdConsumeSettingDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * This class let user change the ad consume setting
 * and save the setting in database
 *
 */
@Repository
@Mapper
public interface AmazonAdConsumeSettingDOMapper {

    /**
     * Get all setting
     * @return
     */
    List<AmazonAdConsumeSettingDO> getAllSetting();

    /**
     * Get all active setting
     * @return
     */
    List<AmazonAdConsumeSettingDO> getAllActiveSetting();

    /**
     * Get Setting by id
     * @param settingId
     * @return
     */
    AmazonAdConsumeSettingDO getSettingById(@Param("userId") Long settingId);

    /**
     * Get all setting by created user id
     * @param userId
     * @return
     */
    List<AmazonAdConsumeSettingDO> getAllSettingByCreatedUser(@Param("userId") Long userId);

    /**
     * Get all active setting by created user id
     *
     * @param userId
     * @return
     */
    List<AmazonAdConsumeSettingDO> getAllActiveSettingByCreatedUser(@Param("userId") Long userId);

    /**
     * Create new setting
     *
     * @param newSetting
     */
    void insertSetting(AmazonAdConsumeSettingDO newSetting);

    /**
     * Update setting
     *
     * @param updateSetting
     */
    void updateSetting(AmazonAdConsumeSettingDO updateSetting);

    /**
     * Active ad consume
     *
     * @param settingId
     */
    void startAdConsume(@Param("userId")Long userId, @Param("settingId")Long settingId);

    /**
     * Disactive the ad consume
     *
     * @param settingId
     */
    void stopAdConsume(@Param("userId")Long userId, @Param("settingId") Long settingId);

    /**
     *
     * @param itemId
     * @return
     */
    AmazonAdConsumeItemDO getSettingItemById(Long itemId);

    /**
     * Create new item in setting
     *
     * @param newItem
     */
    void insertAdConsumeItem(AmazonAdConsumeItemDO newItem);

    /**
     * update item setting
     *
     * @param item
     */
    void updateAdConsumeItem(AmazonAdConsumeItemDO item);

    /**
     * Set item in while list
     *
     * @param itemId
     */
    void whileListItem(@Param("userId")Long userId, @Param("itemId") Long itemId);

    /**
     * Set item in black list
     *
     * @param itemId
     */
    void blackListItem(@Param("userId")Long userId, @Param("itemId") Long itemId);

    /**
     * Remove setting
     *
     * @param settingId
     */
    void removeSetting(@Param("userId")Long userId,@Param("settingId") Long settingId);

    /**
     * Remove item
     * @param itemId
     */
    void removeItem(@Param("userId") Long userId, @Param("itemId") Long itemId);

}
