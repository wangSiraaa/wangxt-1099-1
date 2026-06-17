package com.pallet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pallet.common.*;
import com.pallet.entity.AccountPeriod;
import com.pallet.entity.Deduction;
import com.pallet.entity.DepositBalance;
import com.pallet.mapper.DeductionMapper;
import com.pallet.mapper.DepositBalanceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeductionService {

    private final DeductionMapper deductionMapper;
    private final DepositBalanceMapper depositBalanceMapper;
    private final AccountPeriodService accountPeriodService;
    private final DepositService depositService;
    private final BusinessNoGenerator noGenerator;

    public List<Deduction> list(Long shipperId, String deductionType) {
        LambdaQueryWrapper<Deduction> wrapper = new LambdaQueryWrapper<>();
        if (shipperId != null) {
            wrapper.eq(Deduction::getShipperId, shipperId);
        }
        if (deductionType != null && !deductionType.isEmpty()) {
            wrapper.eq(Deduction::getDeductionType, deductionType);
        }
        wrapper.orderByDesc(Deduction::getDeductionDate, Deduction::getCreateTime);
        return deductionMapper.selectList(wrapper);
    }

    public Deduction getById(Long id) {
        Deduction deduction = deductionMapper.selectById(id);
        if (deduction == null) {
            throw new BusinessException("扣款记录不存在");
        }
        return deduction;
    }

    public List<Deduction> getByReturnId(Long returnId) {
        return deductionMapper.selectList(
                new LambdaQueryWrapper<Deduction>()
                        .eq(Deduction::getReturnId, returnId)
                        .orderByAsc(Deduction::getId)
        );
    }

    @Transactional
    public Deduction createDeduction(
            Long shipperId,
            String shipperName,
            String deductionType,
            String damageType,
            BigDecimal deductionAmount,
            LocalDate deductionDate,
            Long periodId,
            Integer palletCount,
            Long returnId,
            Long userId
    ) {
        Deduction deduction = new Deduction();
        deduction.setDeductionNo(noGenerator.generateDeductionNo());
        deduction.setReturnId(returnId);
        deduction.setShipperId(shipperId);
        deduction.setShipperName(shipperName);
        deduction.setDeductionType(deductionType);
        deduction.setDamageType(damageType);
        deduction.setDeductionAmount(deductionAmount);
        deduction.setDeductionDate(deductionDate);
        deduction.setPeriodId(periodId);
        deduction.setPalletCount(palletCount);
        deduction.setStatus(BusinessConstants.DEPOSIT_STATUS_CONFIRMED);
        deduction.setCreateBy(userId);
        deductionMapper.insert(deduction);
        return deduction;
    }

    @Transactional
    public Deduction createManualDeduction(
            Long shipperId,
            String shipperName,
            String deductionType,
            BigDecimal deductionAmount,
            LocalDate deductionDate,
            String remark,
            Long userId
    ) {
        accountPeriodService.checkPeriodOpen(deductionDate, "人工扣款");
        AccountPeriod period = accountPeriodService.getByDate(deductionDate);
        Deduction deduction = createDeduction(
                shipperId,
                shipperName,
                deductionType,
                null,
                deductionAmount,
                deductionDate,
                period != null ? period.getId() : null,
                0,
                null,
                userId
        );
        deduction.setRemark(remark);
        deductionMapper.updateById(deduction);
        depositService.deductDeposit(shipperId, deductionAmount);
        return deduction;
    }

    @Transactional
    public void deleteByReturnId(Long returnId) {
        List<Deduction> deductions = getByReturnId(returnId);
        for (Deduction d : deductions) {
            if (d.getPeriodId() != null) {
                AccountPeriod period = accountPeriodService.getById(d.getPeriodId());
                if (period != null && BusinessConstants.PERIOD_STATUS_CLOSED.equals(period.getStatus())) {
                    throw new BusinessException(
                            String.format("账期[%s]已关闭，无法删除关联的扣款记录", period.getPeriodName())
                    );
                }
            }
        }
        deductionMapper.delete(
                new LambdaQueryWrapper<Deduction>()
                        .eq(Deduction::getReturnId, returnId)
        );
    }

    @Transactional
    public void delete(Long id) {
        Deduction deduction = getById(id);
        if (deduction.getPeriodId() != null) {
            AccountPeriod period = accountPeriodService.getById(deduction.getPeriodId());
            if (period != null && BusinessConstants.PERIOD_STATUS_CLOSED.equals(period.getStatus())) {
                throw new BusinessException(
                        String.format("账期[%s]已关闭，无法删除扣款记录", period.getPeriodName())
                );
            }
        }
        if (deduction.getReturnId() != null) {
            throw new BusinessException("归还记录关联的扣款记录不能单独删除，请删除归还记录");
        }
        DepositBalance balance = depositService.getOrCreateBalance(deduction.getShipperId());
        balance.setTotalDeducted(balance.getTotalDeducted().subtract(deduction.getDeductionAmount()));
        balance.setCurrentBalance(balance.getCurrentBalance().add(deduction.getDeductionAmount()));
        balance.setAvailableAmount(balance.getAvailableAmount().add(deduction.getDeductionAmount()));
        depositBalanceMapper.updateById(balance);
        deductionMapper.deleteById(id);
    }
}
