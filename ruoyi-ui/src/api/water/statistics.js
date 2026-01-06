import request from '@/utils/request'

// 获取首页统计数据（根据角色自动返回全局或个人统计）
export function getHomeStatistics() {
  return request({
    url: '/water/statistics/home',
    method: 'get'
  })
}

// 获取全局统计数据（管理员/业务操作员）
export function getGlobalStatistics() {
  return request({
    url: '/water/statistics/global',
    method: 'get'
  })
}

// 获取个人统计数据（普通用户）
export function getMyStatistics() {
  return request({
    url: '/water/statistics/my',
    method: 'get'
  })
}

