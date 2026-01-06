package com.ruoyi.water.mapper;

import com.ruoyi.water.domain.WaterAnomalyRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 用水异常记录 Mapper 接口
 *
 * @Author 范佳兴
 * @Date 2024/12/18
 */
@Mapper
public interface WaterAnomalyRecordMapper {

    /**
     * 查询异常记录列表
     *
     * @param waterAnomalyRecord 查询条件
     * @return 异常记录列表
     */
    List<WaterAnomalyRecord> selectWaterAnomalyRecordList(WaterAnomalyRecord waterAnomalyRecord);

    /**
     * 根据ID查询异常记录
     *
     * @param anomalyId 异常记录ID
     * @return 异常记录
     */
    WaterAnomalyRecord selectWaterAnomalyRecordById(Long anomalyId);

    /**
     * 根据用户ID和统计周期查询异常记录
     *
     * @param userId 用户ID
     * @param statPeriod 统计周期
     * @return 异常记录列表
     */
    List<WaterAnomalyRecord> selectByUserAndPeriod(@Param("userId") Long userId, @Param("statPeriod") String statPeriod);

    /**
     * 新增异常记录
     *
     * @param waterAnomalyRecord 异常记录
     * @return 影响行数
     */
    int insertWaterAnomalyRecord(WaterAnomalyRecord waterAnomalyRecord);

    /**
     * 批量新增异常记录
     *
     * @param list 异常记录列表
     * @return 影响行数
     */
    int batchInsertWaterAnomalyRecord(@Param("list") List<WaterAnomalyRecord> list);

    /**
     * 修改异常记录
     *
     * @param waterAnomalyRecord 异常记录
     * @return 影响行数
     */
    int updateWaterAnomalyRecord(WaterAnomalyRecord waterAnomalyRecord);

    /**
     * 删除异常记录
     *
     * @param anomalyId 异常记录ID
     * @return 影响行数
     */
    int deleteWaterAnomalyRecordById(Long anomalyId);

    /**
     * 批量删除异常记录
     *
     * @param anomalyIds 异常记录ID数组
     * @return 影响行数
     */
    int deleteWaterAnomalyRecordByIds(Long[] anomalyIds);

    /**
     * 根据统计周期删除异常记录
     *
     * @param statPeriod 统计周期
     * @return 影响行数
     */
    int deleteByStatPeriod(@Param("statPeriod") String statPeriod);

    /**
     * 根据统计周期删除待处理的异常记录（保留已处理和已忽略的记录）
     *
     * @param statPeriod 统计周期
     * @return 影响行数
     */
    int deletePendingByStatPeriod(@Param("statPeriod") String statPeriod);

    /**
     * 检查异常记录是否已存在（根据用户、周期、规则编码）
     *
     * @param userId 用户ID
     * @param statPeriod 统计周期
     * @param ruleCode 规则编码
     * @return 是否存在
     */
    int checkAnomalyExists(@Param("userId") Long userId, @Param("statPeriod") String statPeriod, @Param("ruleCode") String ruleCode);

    /**
     * 更新处理状态
     *
     * @param anomalyId 异常记录ID
     * @param status 状态
     * @param handleBy 处理人
     * @param handleRemark 处理意见
     * @return 影响行数
     */
    int updateStatus(@Param("anomalyId") Long anomalyId, @Param("status") Integer status,
                     @Param("handleBy") String handleBy, @Param("handleRemark") String handleRemark);

    /**
     * 统计各状态异常数量
     *
     * @param statPeriod 统计周期
     * @return 统计结果
     */
    List<WaterAnomalyRecord> countByStatus(@Param("statPeriod") String statPeriod);

    /**
     * 查询用户的告警列表
     *
     * @param waterAnomalyRecord 查询条件（需包含userId）
     * @return 告警列表
     */
    List<WaterAnomalyRecord> selectMyAlertList(WaterAnomalyRecord waterAnomalyRecord);

    /**
     * 统计用户未读告警数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    int countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 标记告警为已读
     *
     * @param anomalyId 告警ID
     * @param userId    用户ID
     * @return 影响行数
     */
    int markAsRead(@Param("anomalyId") Long anomalyId, @Param("userId") Long userId);

    /**
     * 批量标记告警为已读
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int batchMarkAsRead(@Param("userId") Long userId);

    /**
     * 用户提交反馈
     *
     * @param anomalyId      告警ID
     * @param userId         用户ID
     * @param userFeedback   用户反馈（1-确认属实，2-认为误报）
     * @param feedbackRemark 反馈说明
     * @return 影响行数
     */
    int submitFeedback(@Param("anomalyId") Long anomalyId, @Param("userId") Long userId,
                       @Param("userFeedback") Integer userFeedback, @Param("feedbackRemark") String feedbackRemark);

    /**
     * 查询全局异常统计
     *
     * @return 统计数据（总数、待处理、已处理、已忽略）
     */
    Map<String, Object> selectGlobalAnomalyStats();

    /**
     * 查询用户告警统计
     *
     * @param userId 用户ID
     * @return 统计数据（总数、未读数、待反馈数）
     */
    Map<String, Object> selectUserAlertStats(@Param("userId") Long userId);
}

