import request from './request'

export function submitFeedback(data) {
  return request({
    url: '/api/feedback/public/submit',
    method: 'post',
    data
  })
}
