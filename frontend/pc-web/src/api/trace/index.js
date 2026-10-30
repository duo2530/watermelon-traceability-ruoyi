import request from '@/utils/request'

function crud(base) {
  return {
    list: query => request({ url: base + '/list', method: 'get', params: query }),
    get: id => request({ url: base + '/' + id, method: 'get' }),
    add: data => request({ url: base, method: 'post', data: data }),
    update: data => request({ url: base, method: 'put', data: data }),
    del: ids => request({ url: base + '/' + ids, method: 'delete' })
  }
}

// 地块
export const plotApi = {
  ...crud('/trace/plot'),
  options: () => request({ url: '/trace/plot/options', method: 'get' })
}

// 种植批次
export const batchApi = {
  ...crud('/trace/batch'),
  options: query => request({ url: '/trace/batch/options', method: 'get', params: query }),
  qrcode: id => request({ url: '/trace/batch/' + id + '/qrcode', method: 'post' }),
  archive: ids => request({ url: '/trace/archive/' + ids, method: 'put' })
}

// 农事记录
export const farmApi = crud('/trace/farm')

// 品控记录
export const qcApi = {
  ...crud('/trace/qc'),
  evaluate: params => request({ url: '/trace/qc/evaluate', method: 'get', params: params })
}

// 分级标准
export const gradeApi = crud('/trace/grade')

// 品控统计
export function getStats(query) {
  return request({ url: '/trace/stats', method: 'get', params: query })
}

// 公开溯源（预览）
export function getPublicTrace(code) {
  return request({ url: '/trace/public/' + code, method: 'get', headers: { isToken: false } })
}
