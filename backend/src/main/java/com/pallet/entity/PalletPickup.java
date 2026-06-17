package com.pallet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("pallet_pickup")
public class PalletPickup {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String pickupNo;
    private Long shipperId;
    private String shipperName;
    private Integer palletCount;
    private BigDecimal totalDeposit;
    private LocalDate pickupDate;
    private Long periodId;
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
