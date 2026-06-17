package com.pallet.controller;

import com.pallet.common.Result;
import com.pallet.dto.DepositPaymentDTO;
import com.pallet.entity.DepositBalance;
import com.pallet.entity.DepositPayment;
import com.pallet.service.DepositService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/deposits")
@RequiredArgsConstructor
public class DepositController {

    private final DepositService depositService;

    @GetMapping("/balances")
    public Result<List<DepositBalance>> listBalances() {
        return Result.success(depositService.listBalances());
    }

    @GetMapping("/balances/{shipperId}")
    public Result<DepositBalance> getBalance(@PathVariable Long shipperId) {
        return Result.success(depositService.getBalanceByShipper(shipperId));
    }

    @GetMapping("/payments")
    public Result<List<DepositPayment>> listPayments(
            @RequestParam(required = false) Long shipperId,
            @RequestParam(required = false) String status) {
        return Result.success(depositService.listPayments(shipperId, status));
    }

    @GetMapping("/payments/{id}")
    public Result<DepositPayment> getPaymentById(@PathVariable Long id) {
        return Result.success(depositService.getPaymentById(id));
    }

    @PostMapping("/payments")
    public Result<DepositPayment> createPayment(
            @Valid @RequestBody DepositPaymentDTO dto,
            @RequestHeader(value = "X-User-Id", defaultValue = "2") Long userId) {
        return Result.success(depositService.addDeposit(
                dto.getShipperId(), dto.getPaymentAmount(), dto.getPaymentDate(),
                dto.getPaymentMethod(), dto.getRemark(), userId));
    }
}
