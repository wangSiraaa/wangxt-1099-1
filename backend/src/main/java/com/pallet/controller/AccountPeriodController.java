package com.pallet.controller;

import com.pallet.common.Result;
import com.pallet.dto.AccountPeriodDTO;
import com.pallet.entity.AccountPeriod;
import com.pallet.service.AccountPeriodService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/account-periods")
@RequiredArgsConstructor
public class AccountPeriodController {

    private final AccountPeriodService accountPeriodService;

    @GetMapping
    public Result<List<AccountPeriod>> list() {
        return Result.success(accountPeriodService.list());
    }

    @GetMapping("/{id}")
    public Result<AccountPeriod> getById(@PathVariable Long id) {
        return Result.success(accountPeriodService.getById(id));
    }

    @GetMapping("/by-date")
    public Result<AccountPeriod> getByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(accountPeriodService.getByDate(date));
    }

    @PostMapping
    public Result<AccountPeriod> create(@Valid @RequestBody AccountPeriodDTO dto) {
        return Result.success(accountPeriodService.create(dto));
    }

    @PutMapping("/{id}/close")
    public Result<Void> closePeriod(@PathVariable Long id, @RequestParam Long userId) {
        accountPeriodService.closePeriod(id, userId);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        accountPeriodService.delete(id);
        return Result.success();
    }
}
