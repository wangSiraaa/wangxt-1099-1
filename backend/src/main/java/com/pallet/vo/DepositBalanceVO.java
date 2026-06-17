package com.pallet.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DepositBalanceVO {
    private Long shipperId;
    private String shipperName;
    private BigDecimal totalPaid;
    private BigDecimal totalDeducted;
    private BigDecimal totalRefunded;
    private BigDecimal currentBalance;
    private BigDecimal frozenAmount;
    private BigDecimal availableAmount;
}
