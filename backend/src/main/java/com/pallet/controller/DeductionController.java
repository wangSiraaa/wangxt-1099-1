package com.pallet.controller;

import com.pallet.common.Result;
import com.pallet.entity.Deduction;
import com.pallet.service.DeductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/deductions")
@RequiredArgsConstructor
public class DeductionController {

    private final DeductionService deductionService;

    @GetMapping
    public Result<List<Deduction>> list(
            @RequestParam(required = false) Long shipperId,
            @RequestParam(required = false) String deductionType) {
        return Result.success(deductionService.list(shipperId, deductionType));
    }

    @GetMapping("/{id}")
    public Result<Deduction> getById(@PathVariable Long id) {
        return Result.success(deductionService.getById(id));
    }

    @GetMapping("/by-return/{returnId}")
    public Result<List<Deduction>> getByReturnId(@PathVariable Long returnId) {
        return Result.success(deductionService.getByReturnId(returnId));
    }

    @PostMapping("/manual")
    public Result<Deduction> createManualDeduction(
            @RequestParam Long shipperId,
            @RequestParam String shipperName,
            @RequestParam String deductionType,
            @RequestParam BigDecimal deductionAmount,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deductionDate,
            @RequestParam(required = false) String remark,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        return Result.success(deductionService.createManualDeduction(
                shipperId, shipperName, deductionType, deductionAmount, deductionDate, remark, userId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deductionService.delete(id);
        return Result.success();
    }
}
