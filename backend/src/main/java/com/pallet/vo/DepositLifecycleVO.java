package com.pallet.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DepositLifecycleVO {
    private Long pickupDetailId;
    private Long palletId;
    private String palletCode;
    private Long shipperId;
    private String shipperName;
    private Long initialCarrierId;
    private String initialCarrierName;
    private Long destStoreId;
    private String destStoreName;
    private BigDecimal initialDepositAmount;
    private BigDecimal currentDepositAmount;
    private BigDecimal totalDeductedAmount;
    private String currentStatus;
    private LocalDateTime pickupTime;
    private LocalDateTime latestEventTime;
    private List<DepositLifecycleEventVO> events;

    @Data
    public static class DepositLifecycleEventVO {
        private String eventType;
        private String eventTypeName;
        private String eventNo;
        private Long eventId;
        private LocalDateTime eventTime;
        private Long operatorId;
        private String operatorName;
        private Long fromHolderId;
        private String fromHolderName;
        private Long toHolderId;
        private String toHolderName;
        private Long depositBearerId;
        private String depositBearerName;
        private BigDecimal depositAmount;
        private BigDecimal deductionAmount;
        private String damageType;
        private String damageTypeName;
        private String returnStatus;
        private String returnStatusName;
        private String remark;
    }
}
