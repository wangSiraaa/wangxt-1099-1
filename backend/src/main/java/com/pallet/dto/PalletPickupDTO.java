package com.pallet.dto;

import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
public class PalletPickupDTO {
    @NotNull(message = "发货方ID不能为空")
    private Long shipperId;
    @NotNull(message = "承运商ID不能为空")
    private Long carrierId;
    @NotNull(message = "目的门店ID不能为空")
    private Long destStoreId;
    @NotNull(message = "领取日期不能为空")
    private LocalDate pickupDate;
    @NotEmpty(message = "领取托盘明细不能为空")
    @Valid
    private List<PalletPickupDetailDTO> details;
    private String remark;
}
