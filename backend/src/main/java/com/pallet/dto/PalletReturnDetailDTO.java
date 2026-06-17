package com.pallet.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class PalletReturnDetailDTO {
    @NotNull(message = "托盘ID不能为空")
    private Long palletId;
    private String palletCode;
    private Long pickupDetailId;
    @NotBlank(message = "归还状态不能为空")
    private String returnStatus;
    private String damageType;
    private String remark;
}
