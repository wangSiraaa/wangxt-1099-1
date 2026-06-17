package com.pallet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pallet_return_detail")
public class PalletReturnDetail {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long returnId;
    private Long pickupId;
    private Long palletId;
    private String palletCode;
    private String returnStatus;
    private BigDecimal depositAmount;
    private String damageType;
    private BigDecimal deductionAmount;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
