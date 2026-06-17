import request from '../utils/request'

export const listDeductions = (shipperId, deductionType) =>
  request.get('/deductions', { params: { shipperId, deductionType } })

export const getDeduction = (id) =>
  request.get(`/deductions/${id}`)

export const getDeductionsByReturn = (returnId) =>
  request.get(`/deductions/by-return/${returnId}`)

export const createManualDeduction = (params) =>
  request.post('/deductions/manual', null, { params })

export const deleteDeduction = (id) =>
  request.delete(`/deductions/${id}`)
