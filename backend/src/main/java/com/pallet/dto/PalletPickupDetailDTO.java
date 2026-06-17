package com.pallet.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class PalletPickupDetailDTO {
    @NotNull(message = "托盘ID不能为空")
    private Long palletId;
    private String palletCode;
    private String remark;
}
