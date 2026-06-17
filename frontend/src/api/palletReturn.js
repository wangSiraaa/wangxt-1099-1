import request from '../utils/request'

export const listReturns = (carrierId, shipperId, status) =>
  request.get('/pallet-returns', { params: { carrierId, shipperId, status } })

export const getReturn = (id) =>
  request.get(`/pallet-returns/${id}`)

export const getReturnDetails = (id) =>
  request.get(`/pallet-returns/${id}/details`)

export const createReturn = (data) =>
  request.post('/pallet-returns', data)

export const updateReturn = (id, data) =>
  request.put(`/pallet-returns/${id}`, data)

export const deleteReturn = (id) =>
  request.delete(`/pallet-returns/${id}`)
