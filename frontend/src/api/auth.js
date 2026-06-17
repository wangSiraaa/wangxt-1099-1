import request from '../utils/request'

export const login = (data) =>
  request.post('/auth/login', data)

export const listUsers = (roleType) =>
  request.get('/auth/users', { params: { roleType } })

export const listShippers = () =>
  request.get('/auth/users/shippers')

export const listCarriers = () =>
  request.get('/auth/users/carriers')

export const listStores = () =>
  request.get('/auth/users/stores')
