import request from '@/utils/request'

export function getLifecycleByPickupDetailId(pickupDetailId) {
  return request({
    url: `/deposit-lifecycles/by-pickup-detail/${pickupDetailId}`,
    method: 'get'
  })
}

export function getLifecyclesByPickupId(pickupId) {
  return request({
    url: `/deposit-lifecycles/by-pickup/${pickupId}`,
    method: 'get'
  })
}

export function getLifecyclesByPalletId(palletId) {
  return request({
    url: `/deposit-lifecycles/by-pallet/${palletId}`,
    method: 'get'
  })
}

export function getLifecyclesByShipperId(shipperId, onlyUnreturned) {
  return request({
    url: `/deposit-lifecycles/by-shipper/${shipperId}`,
    method: 'get',
    params: { onlyUnreturned }
  })
}

export function getLifecycleByPalletCode(palletCode) {
  return request({
    url: `/deposit-lifecycles/by-pallet-code/${palletCode}`,
    method: 'get'
  })
}
