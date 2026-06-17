import request from '@/utils/request'

export function listTransfers(params) {
  return request({
    url: '/pallet-transfers',
    method: 'get',
    params
  })
}

export function getTransferById(id) {
  return request({
    url: `/pallet-transfers/${id}`,
    method: 'get'
  })
}

export function getTransfersByPickupDetailId(pickupDetailId) {
  return request({
    url: `/pallet-transfers/by-pickup-detail/${pickupDetailId}`,
    method: 'get'
  })
}

export function getTransfersByPalletId(palletId) {
  return request({
    url: `/pallet-transfers/by-pallet/${palletId}`,
    method: 'get'
  })
}

export function createTransfer(data) {
  return request({
    url: '/pallet-transfers',
    method: 'post',
    data
  })
}

export function cancelTransfer(id, userId) {
  return request({
    url: `/pallet-transfers/${id}/cancel`,
    method: 'put',
    params: { userId }
  })
}
