package com.pallet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pallet.common.BusinessConstants;
import com.pallet.common.BusinessException;
import com.pallet.common.BusinessNoGenerator;
import com.pallet.entity.DepositBalance;
import com.pallet.entity.DepositPayment;
import com.pallet.mapper.DepositBalanceMapper;
import com.pallet.mapper.DepositPaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DepositService {

    private final DepositBalanceMapper depositBalanceMapper;
    private final DepositPaymentMapper depositPaymentMapper;
    private final AccountPeriodService accountPeriodService;
    private final BusinessNoGenerator noGenerator;
    private final AuthService authService;

    public DepositBalance getBalanceByShipper(Long shipperId) {
        return getOrCreateBalance(shipperId);
    }

    public DepositBalance getOrCreateBalance(Long shipperId) {
        DepositBalance balance = depositBalanceMapper.selectOne(
                new LambdaQueryWrapper<DepositBalance>()
                        .eq(DepositBalance::getShipperId, shipperId)
        );
        if (balance == null) {
            balance = new DepositBalance();
            balance.setShipperId(shipperId);
            var user = authService.getById(shipperId);
            if (user != null) {
                balance.setShipperName(user.getUserName());
            }
            balance.setTotalPaid(BigDecimal.ZERO);
            balance.setTotalDeducted(BigDecimal.ZERO);
            balance.setTotalRefunded(BigDecimal.ZERO);
            balance.setCurrentBalance(BigDecimal.ZERO);
            balance.setFrozenAmount(BigDecimal.ZERO);
            balance.setAvailableAmount(BigDecimal.ZERO);
            depositBalanceMapper.insert(balance);
        }
        return balance;
    }

    public void checkSufficientDeposit(Long shipperId, BigDecimal amount) {
        DepositBalance balance = getOrCreateBalance(shipperId);
        if (balance.getAvailableAmount().compareTo(amount) < 0) {
            throw new BusinessException(
                    String.format("押金余额不足，可用押金：%s，需要：%s",
                            balance.getAvailableAmount(), amount)
            );
        }
    }

    @Transactional
    public void freezeDeposit(Long shipperId, BigDecimal amount) {
        DepositBalance balance = getOrCreateBalance(shipperId);
        if (balance.getAvailableAmount().compareTo(amount) < 0) {
            throw new BusinessException("可用押金不足，无法冻结");
        }
        balance.setFrozenAmount(balance.getFrozenAmount().add(amount));
        balance.setAvailableAmount(balance.getAvailableAmount().subtract(amount));
        depositBalanceMapper.updateById(balance);
    }

    @Transactional
    public void unfreezeDeposit(Long shipperId, BigDecimal amount) {
        DepositBalance balance = getOrCreateBalance(shipperId);
        if (balance.getFrozenAmount().compareTo(amount) < 0) {
            throw new BusinessException("冻结押金不足，无法解冻");
        }
        balance.setFrozenAmount(balance.getFrozenAmount().subtract(amount));
        balance.setAvailableAmount(balance.getAvailableAmount().add(amount));
        depositBalanceMapper.updateById(balance);
    }

    @Transactional
    public void deductDeposit(Long shipperId, BigDecimal amount) {
        DepositBalance balance = getOrCreateBalance(shipperId);
        balance.setTotalDeducted(balance.getTotalDeducted().add(amount));
        balance.setCurrentBalance(balance.getCurrentBalance().subtract(amount));
        if (balance.getFrozenAmount().compareTo(amount) >= 0) {
            balance.setFrozenAmount(balance.getFrozenAmount().subtract(amount));
        } else {
            BigDecimal remain = amount.subtract(balance.getFrozenAmount());
            balance.setFrozenAmount(BigDecimal.ZERO);
            balance.setAvailableAmount(balance.getAvailableAmount().subtract(remain));
        }
        depositBalanceMapper.updateById(balance);
    }

    @Transactional
    public DepositPayment addDeposit(Long shipperId, BigDecimal amount, LocalDate paymentDate,
                                     String paymentMethod, String remark, Long userId) {
        accountPeriodService.checkPeriodOpen(paymentDate, "押金充值");
        var period = accountPeriodService.getByDate(paymentDate);
        var user = authService.getById(shipperId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        DepositPayment payment = new DepositPayment();
        payment.setPaymentNo(noGenerator.generatePaymentNo());
        payment.setShipperId(shipperId);
        payment.setShipperName(user.getUserName());
        payment.setPaymentAmount(amount);
        payment.setPaymentDate(paymentDate);
        payment.setPeriodId(period != null ? period.getId() : null);
        payment.setStatus(BusinessConstants.DEPOSIT_STATUS_CONFIRMED);
        payment.setPaymentMethod(paymentMethod);
        payment.setRemark(remark);
        payment.setCreateBy(userId);
        depositPaymentMapper.insert(payment);

        DepositBalance balance = getOrCreateBalance(shipperId);
        balance.setTotalPaid(balance.getTotalPaid().add(amount));
        balance.setCurrentBalance(balance.getCurrentBalance().add(amount));
        balance.setAvailableAmount(balance.getAvailableAmount().add(amount));
        depositBalanceMapper.updateById(balance);

        return payment;
    }

    public List<DepositPayment> listPayments(Long shipperId, String status) {
        LambdaQueryWrapper<DepositPayment> wrapper = new LambdaQueryWrapper<>();
        if (shipperId != null) {
            wrapper.eq(DepositPayment::getShipperId, shipperId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(DepositPayment::getStatus, status);
        }
        wrapper.orderByDesc(DepositPayment::getPaymentDate, DepositPayment::getCreateTime);
        return depositPaymentMapper.selectList(wrapper);
    }

    public DepositPayment getPaymentById(Long id) {
        DepositPayment payment = depositPaymentMapper.selectById(id);
        if (payment == null) {
            throw new BusinessException("押金缴纳记录不存在");
        }
        return payment;
    }

    public List<DepositBalance> listBalances() {
        return depositBalanceMapper.selectList(
                new LambdaQueryWrapper<DepositBalance>()
                        .orderByAsc(DepositBalance::getShipperId)
        );
    }
}
