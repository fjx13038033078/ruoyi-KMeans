package com.ruoyi.water.mapper;

import com.ruoyi.water.domain.WaterAnomalyRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
}

