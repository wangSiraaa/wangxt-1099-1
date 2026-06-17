import request from '../utils/request'

export const listDepositBalances = () =>
  request.get('/deposits/balances')

export const getDepositBalance = (shipperId) =>
  request.get(`/deposits/balances/${shipperId}`)

export const listDepositPayments = (shipperId) =>
  request.get('/deposits/payments', { params: { shipperId } })

export const payDeposit = (data) =>
  request.post('/deposits/payments', data)
