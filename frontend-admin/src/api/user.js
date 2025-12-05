import request from './request'

export function login(data) {
  return request({
    url: '/api/users/login',
    method: 'post',
    data
  })
}

export function logout() {
  return request({
    url: '/api/users/logout',
    method: 'post'
  })
}

export function getCurrentUser() {
  return request({
    url: '/api/users/current',
    method: 'get'
  })
}
