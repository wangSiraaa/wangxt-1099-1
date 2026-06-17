package com.pallet.dto;

import lombok.Data;
import javax.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class PalletDTO {
    @NotBlank(message = "托盘编码不能为空")
    private String palletCode;
    private String palletName;
    private String specification;
    @NotNull(message = "押金金额不能为空")
    @Positive(message = "押金金额必须大于0")
    private BigDecimal depositAmount;
    private String status;
    private String remark;
}
