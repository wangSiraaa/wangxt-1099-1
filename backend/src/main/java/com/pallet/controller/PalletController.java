package com.pallet.controller;

import com.pallet.common.Result;
import com.pallet.dto.PalletDTO;
import com.pallet.entity.Pallet;
import com.pallet.service.PalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/pallets")
@RequiredArgsConstructor
public class PalletController {

    private final PalletService palletService;

    @GetMapping
    public Result<List<Pallet>> list(@RequestParam(required = false) String status) {
        return Result.success(palletService.list(status));
    }

    @GetMapping("/{id}")
    public Result<Pallet> getById(@PathVariable Long id) {
        return Result.success(palletService.getById(id));
    }

    @GetMapping("/code/{palletCode}")
    public Result<Pallet> getByCode(@PathVariable String palletCode) {
        return Result.success(palletService.getByCode(palletCode));
    }

    @GetMapping("/available")
    public Result<List<Pallet>> getAvailablePallets() {
        return Result.success(palletService.getAvailablePallets());
    }

    @PostMapping
    public Result<Pallet> create(
            @Valid @RequestBody PalletDTO dto,
            @RequestHeader(value = "X-User-Id", defaultValue = "2") Long userId) {
        return Result.success(palletService.createPallet(dto, userId));
    }

    @PutMapping("/{id}")
    public Result<Pallet> update(@PathVariable Long id, @Valid @RequestBody PalletDTO dto) {
        return Result.success(palletService.updatePallet(id, dto));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        palletService.updateStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        palletService.deletePallet(id);
        return Result.success();
    }
}
