import request from '@/utils/request'

// ==================== 特征数据生成 ====================

// 生成用水特征数据
export function generateFeatureData(statPeriod) {
  return request({
    url: '/water/cluster/generateFeature',
    method: 'get',
    params: { statPeriod }
  })
}

// ==================== K-Means聚类分析 ====================

// 执行K-means聚类分析
export function executeKMeans(statPeriod, k) {
  return request({
    url: '/water/cluster/execute',
    method: 'get',
    params: { statPeriod, k }
  })
}

// 执行K-means聚类分析（使用自定义配置）
export function executeKMeansWithConfig(statPeriod, config) {
  return request({
    url: '/water/cluster/executeWithConfig',
    method: 'post',
    params: { statPeriod },
    data: config
  })
}

// 获取聚类配置
export function getClusterConfig() {
  return request({
    url: '/water/cluster/config',
    method: 'get'
  })
}

// 更新聚类配置
export function updateClusterConfig(config) {
  return request({
    url: '/water/cluster/config',
    method: 'post',
    data: config
  })
}

// 获取聚类结果（分页）
export function getClusterResult(query) {
  return request({
    url: '/water/cluster/result',
    method: 'get',
    params: query
  })
}

// 获取聚类统计信息
export function getClusterStatistics(statPeriod) {
  return request({
    url: '/water/cluster/statistics',
    method: 'get',
    params: { statPeriod }
  })
}

// ==================== 异常检测 ====================

// 执行异常检测
export function executeAnomalyDetection(statPeriod) {
  return request({
    url: '/water/cluster/detectAnomaly',
    method: 'get',
    params: { statPeriod }
  })
}

// 获取异常检测规则列表
export function getAnomalyRules() {
  return request({
    url: '/water/cluster/rules',
    method: 'get'
  })
}

// 更新异常检测规则列表
export function updateAnomalyRules(rules) {
  return request({
    url: '/water/cluster/rules',
    method: 'post',
    data: rules
  })
}

// 添加异常检测规则
export function addAnomalyRule(rule) {
  return request({
    url: '/water/cluster/rules/add',
    method: 'post',
    data: rule
  })
}

// 删除异常检测规则
export function removeAnomalyRule(ruleCode) {
  return request({
    url: '/water/cluster/rules/delete',
    method: 'get',
    params: { ruleCode }
  })
}

// ==================== 异常记录管理 ====================

// 查询异常记录列表（分页）
export function listAnomalyRecords(query) {
  return request({
    url: '/water/cluster/anomaly/list',
    method: 'get',
    params: query
  })
}

// 处理异常记录
export function handleAnomalyRecord(anomalyId, status, handleRemark) {
  return request({
    url: '/water/cluster/anomaly/handle',
    method: 'get',
    params: { anomalyId, status, handleRemark }
  })
}

// ==================== 一键分析 ====================

// 一键分析（生成特征 + 聚类 + 异常检测）
export function analyzeAll(statPeriod, k) {
  return request({
    url: '/water/cluster/analyzeAll',
    method: 'get',
    params: { statPeriod, k }
  })
}

