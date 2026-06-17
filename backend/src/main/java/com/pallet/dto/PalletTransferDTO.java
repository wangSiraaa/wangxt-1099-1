package com.pallet.dto;

import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
public class PalletTransferDTO {
    @NotNull(message = "转出承运商ID不能为空")
    private Long fromCarrierId;
    @NotNull(message = "转入承运商ID不能为空")
    private Long toCarrierId;
    @NotNull(message = "发货方ID不能为空")
    private Long shipperId;
    private Long depositBearerId;
    @NotNull(message = "转运日期不能为空")
    private LocalDate transferDate;
    @NotEmpty(message = "转运托盘明细不能为空")
    @Valid
    private List<PalletTransferDetailDTO> details;
    private String remark;

    @Data
    public static class PalletTransferDetailDTO {
        @NotNull(message = "领用明细ID不能为空")
        private Long pickupDetailId;
        @NotNull(message = "托盘ID不能为空")
        private Long palletId;
        private String palletCode;
        private String remark;
    }
}
