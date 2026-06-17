import request from '../utils/request'

export const listPickups = (shipperId, status) =>
  request.get('/pallet-pickups', { params: { shipperId, status } })

export const getPickup = (id) =>
  request.get(`/pallet-pickups/${id}`)

export const getPickupDetails = (id) =>
  request.get(`/pallet-pickups/${id}/details`)

export const getUnreturnedDetails = (shipperId) =>
  request.get(`/pallet-pickups/unreturned/${shipperId}`)

export const createPickup = (data) =>
  request.post('/pallet-pickups', data)
