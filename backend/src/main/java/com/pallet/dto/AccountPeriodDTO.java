package com.pallet.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AccountPeriodDTO {
    @NotBlank(message = "账期编码不能为空")
    private String periodCode;
    @NotBlank(message = "账期名称不能为空")
    private String periodName;
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;
    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;
    private String remark;
}
