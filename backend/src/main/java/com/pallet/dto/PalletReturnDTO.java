package com.pallet.dto;

import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
public class PalletReturnDTO {
    @NotNull(message = "承运商ID不能为空")
    private Long carrierId;
    @NotNull(message = "发货方ID不能为空")
    private Long shipperId;
    @NotNull(message = "归还日期不能为空")
    private LocalDate returnDate;
    @NotEmpty(message = "归还明细不能为空")
    @Valid
    private List<PalletReturnDetailDTO> details;
    private String remark;
}
