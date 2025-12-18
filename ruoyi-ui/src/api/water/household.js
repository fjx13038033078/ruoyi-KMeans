import request from '@/utils/request'

// 查询住户信息列表
export function listHousehold(query) {
  return request({
    url: '/water/household/list',
    method: 'get',
    params: query
  })
}

// 获取住户信息详情
export function getHouseholdById(householdId) {
  return request({
    url: '/water/household/detail',
    method: 'get',
    params: { householdId }
  })
}

// 获取当前用户的住户信息
export function getMyHouseholdInfo() {
  return request({
    url: '/water/household/myInfo',
    method: 'get'
  })
}

// 新增住户信息
export function addHousehold(data) {
  return request({
    url: '/water/household/add',
    method: 'post',
    data: data
  })
}

// 修改住户信息
export function updateHousehold(data) {
  return request({
    url: '/water/household/update',
    method: 'post',
    data: data
  })
}

// 删除住户信息
export function deleteHousehold(householdId) {
  return request({
    url: '/water/household/delete',
    method: 'get',
    params: { householdId }
  })
}

// 批量删除住户信息
export function deleteHouseholdBatch(householdIds) {
  return request({
    url: '/water/household/deleteBatch',
    method: 'post',
    data: householdIds
  })
}

// 更新住户状态
export function updateHouseholdStatus(householdId, status) {
  return request({
    url: '/water/household/updateStatus',
    method: 'get',
    params: { householdId, status }
  })
}

