import request from '@/utils/request'
import upload from '@/utils/upload'

// 地块下拉
export function plotOptions() {
  return request({ url: '/trace/plot/options', method: 'get' })
}

// 新增地块
export function addPlot(data) {
  return request({ url: '/trace/plot', method: 'post', data })
}

// 批次列表（分页）
export function listBatch(params) {
  return request({ url: '/trace/batch/list', method: 'get', params })
}

// 批次下拉（排除已归档）
export function batchOptions() {
  return request({ url: '/trace/batch/options', method: 'get' })
}

// 批次详情（含 qrContent）
export function getBatch(batchId) {
  return request({ url: '/trace/batch/' + batchId, method: 'get' })
}

// 新增批次（服务端自动生成溯源码和二维码）
export function addBatch(data) {
  return request({ url: '/trace/batch', method: 'post', data })
}

// 重新生成二维码
export function genQrcode(batchId) {
  return request({ url: '/trace/batch/' + batchId + '/qrcode', method: 'post' })
}

// 农事记录列表
export function listFarm(params) {
  return request({ url: '/trace/farm/list', method: 'get', params })
}

// 品控记录列表
export function listQc(params) {
  return request({ url: '/trace/qc/list', method: 'get', params })
}

// 预判等级
export function evaluateGrade(params) {
  return request({ url: '/trace/qc/evaluate', method: 'get', params })
}

// 离线队列批量同步
export function syncBatch(items) {
  return request({ url: '/trace/sync/batch', method: 'post', data: items, timeout: 30000, silent: true })
}

// 消费者扫码查询（匿名）
export function publicTrace(code) {
  return request({ url: '/trace/public/' + encodeURIComponent(code), method: 'get', headers: { isToken: false } })
}

// 上传图片，返回 fileName（/profile/upload/...）
export function uploadImage(filePath) {
  return upload({ url: '/common/upload', name: 'file', filePath })
}
