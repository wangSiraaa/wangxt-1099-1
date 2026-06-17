package com.pallet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("deduction")
public class Deduction {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String deductionNo;
    private Long returnId;
    private Long shipperId;
    private String shipperName;
    private String deductionType;
    private BigDecimal deductionAmount;
    private String damageType;
    private LocalDate deductionDate;
    private Long periodId;
    private Integer palletCount;
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
