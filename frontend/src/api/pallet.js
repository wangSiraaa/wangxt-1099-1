import request from '../utils/request'

export const listPallets = (status) =>
  request.get('/pallets', { params: { status } })

export const listAvailablePallets = () =>
  request.get('/pallets/available')

export const getPallet = (id) =>
  request.get(`/pallets/${id}`)

export const createPallet = (data) =>
  request.post('/pallets', data)

export const updatePallet = (id, data) =>
  request.put(`/pallets/${id}`, data)

export const deletePallet = (id) =>
  request.delete(`/pallets/${id}`)
