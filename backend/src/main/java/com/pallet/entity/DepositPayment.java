package com.pallet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("deposit_payment")
public class DepositPayment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String paymentNo;
    private Long shipperId;
    private String shipperName;
    private BigDecimal paymentAmount;
    private LocalDate paymentDate;
    private Long periodId;
    private String status;
    private String paymentMethod;
    private String remark;
    private Long createBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
