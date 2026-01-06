import request from '@/utils/request'

// 查询我的告警列表
export function getMyAlertList(query) {
  return request({
    url: '/water/alert/myList',
    method: 'get',
    params: query
  })
}

// 获取未读告警数量
export function getUnreadCount() {
  return request({
    url: '/water/alert/unreadCount',
    method: 'get'
  })
}

// 获取告警详情
export function getAlertDetail(anomalyId) {
  return request({
    url: '/water/alert/detail/' + anomalyId,
    method: 'get'
  })
}

// 标记告警为已读
export function markAsRead(anomalyId) {
  return request({
    url: '/water/alert/read/' + anomalyId,
    method: 'post'
  })
}

// 全部标记为已读
export function markAllAsRead() {
  return request({
    url: '/water/alert/readAll',
    method: 'post'
  })
}

// 提交用户反馈
export function submitFeedback(data) {
  return request({
    url: '/water/alert/feedback',
    method: 'post',
    params: data
  })
}

