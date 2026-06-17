package com.pallet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("deposit_balance")
public class DepositBalance {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long shipperId;
    private String shipperName;
    private BigDecimal totalPaid;
    private BigDecimal totalDeducted;
    private BigDecimal totalRefunded;
    private BigDecimal currentBalance;
    private BigDecimal frozenAmount;
    private BigDecimal availableAmount;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
