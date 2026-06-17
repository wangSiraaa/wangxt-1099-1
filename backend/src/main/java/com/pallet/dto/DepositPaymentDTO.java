package com.pallet.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DepositPaymentDTO {
    @NotNull(message = "发货方ID不能为空")
    private Long shipperId;
    @NotNull(message = "押金金额不能为空")
    @Positive(message = "押金金额必须大于0")
    private BigDecimal paymentAmount;
    @NotNull(message = "缴纳日期不能为空")
    private LocalDate paymentDate;
    private String paymentMethod;
    private String remark;
}
