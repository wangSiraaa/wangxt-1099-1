package com.pallet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pallet_pickup_detail")
public class PalletPickupDetail {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long pickupId;
    private Long palletId;
    private String palletCode;
    private BigDecimal depositAmount;
    private String returnStatus;
    private String remark;
    private Long currentHolderId;
    private String currentHolderName;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
