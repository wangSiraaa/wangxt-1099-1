package com.pallet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("pallet_transfer")
public class PalletTransfer {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String transferNo;
    private Long pickupDetailId;
    private Long palletId;
    private String palletCode;
    private Long fromCarrierId;
    private String fromCarrierName;
    private Long toCarrierId;
    private String toCarrierName;
    private Long shipperId;
    private String shipperName;
    private Long depositBearerId;
    private String depositBearerName;
    private BigDecimal depositAmount;
    private LocalDate transferDate;
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
