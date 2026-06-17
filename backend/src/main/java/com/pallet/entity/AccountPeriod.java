package com.pallet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("account_period")
public class AccountPeriod {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String periodCode;
    private String periodName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private LocalDateTime closeTime;
    private Long closeBy;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
