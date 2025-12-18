import request from '@/utils/request'

// 查询用水记录列表（业务操作员查看所有）
export function listRecord(query) {
  return request({
    url: '/water/record/list',
    method: 'get',
    params: query
  })
}

// 获取用水记录详情
export function getRecordById(recordId) {
  return request({
    url: '/water/record/detail',
    method: 'get',
    params: { recordId }
  })
}

// 获取当前用户的用水记录列表
export function getMyRecords(query) {
  return request({
    url: '/water/record/myRecords',
    method: 'get',
    params: query
  })
}

// 新增用水记录
export function addRecord(data) {
  return request({
    url: '/water/record/add',
    method: 'post',
    data: data
  })
}

// 修改用水记录
export function updateRecord(data) {
  return request({
    url: '/water/record/update',
    method: 'post',
    data: data
  })
}

// 删除用水记录
export function deleteRecord(recordId) {
  return request({
    url: '/water/record/delete',
    method: 'get',
    params: { recordId }
  })
}

// 批量删除用水记录
export function deleteRecordBatch(recordIds) {
  return request({
    url: '/water/record/deleteBatch',
    method: 'post',
    data: recordIds
  })
}

// 查询用水类型分布统计
export function getUsageTypeStatistics(userId) {
  return request({
    url: '/water/record/typeStatistics',
    method: 'get',
    params: { userId }
  })
}

// 查询时段分布统计
export function getTimePeriodStatistics(userId) {
  return request({
    url: '/water/record/timePeriodStatistics',
    method: 'get',
    params: { userId }
  })
}

// 获取当前用户的用水类型分布统计
export function getMyUsageTypeStatistics() {
  return request({
    url: '/water/record/myTypeStatistics',
    method: 'get'
  })
}

// 获取当前用户的时段分布统计
export function getMyTimePeriodStatistics() {
  return request({
    url: '/water/record/myTimePeriodStatistics',
    method: 'get'
  })
}

