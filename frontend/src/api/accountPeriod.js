import request from '../utils/request'

export const listAccountPeriods = () =>
  request.get('/account-periods')

export const getAccountPeriod = (id) =>
  request.get(`/account-periods/${id}`)

export const getPeriodByDate = (date) =>
  request.get('/account-periods/by-date', { params: { date } })

export const createAccountPeriod = (data) =>
  request.post('/account-periods', data)

export const closePeriod = (id, userId) =>
  request.put(`/account-periods/${id}/close`, null, { params: { userId } })

export const reopenPeriod = (id) =>
  request.put(`/account-periods/${id}/reopen`)

export const deletePeriod = (id) =>
  request.delete(`/account-periods/${id}`)
