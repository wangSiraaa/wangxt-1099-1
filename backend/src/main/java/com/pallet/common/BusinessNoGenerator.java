package com.pallet.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class BusinessNoGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final AtomicInteger COUNTER = new AtomicInteger(1);

    @Value("${pallet.deposit.default-amount:100.00}")
    private BigDecimal defaultDepositAmount;

    @Value("${pallet.deposit.damaged-rate:0.50}")
    private BigDecimal damagedRate;

    @Value("${pallet.deposit.lost-rate:1.00}")
    private BigDecimal lostRate;

    public String generatePaymentNo() {
        return "DP" + LocalDate.now().format(DATE_FORMATTER) + String.format("%03d", COUNTER.getAndIncrement());
    }

    public String generatePickupNo() {
        return "PK" + LocalDate.now().format(DATE_FORMATTER) + String.format("%03d", COUNTER.getAndIncrement());
    }

    public String generateReturnNo() {
        return "RT" + LocalDate.now().format(DATE_FORMATTER) + String.format("%03d", COUNTER.getAndIncrement());
    }

    public String generateDeductionNo() {
        return "DD" + LocalDate.now().format(DATE_FORMATTER) + String.format("%03d", COUNTER.getAndIncrement());
    }

    public BigDecimal getDefaultDepositAmount() {
        return defaultDepositAmount;
    }

    public BigDecimal getDamagedRate() {
        return damagedRate;
    }

    public BigDecimal getLostRate() {
        return lostRate;
    }

    public BigDecimal calculateDamagedDeduction(BigDecimal depositAmount) {
        return depositAmount.multiply(damagedRate);
    }

    public BigDecimal calculateLostDeduction(BigDecimal depositAmount) {
        return depositAmount.multiply(lostRate);
    }
}
