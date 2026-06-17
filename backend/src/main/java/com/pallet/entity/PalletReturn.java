package com.pallet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("pallet_return")
public class PalletReturn {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String returnNo;
    private Long carrierId;
    private String carrierName;
    private Long shipperId;
    private String shipperName;
    private Integer returnCount;
    private Integer normalCount;
    private Integer damagedCount;
    private Integer lostCount;
    private LocalDate returnDate;
    private Long periodId;
    private BigDecimal deductionAmount;
    private String status;
    private String remark;
    private Long createBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
