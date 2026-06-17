package com.pallet.controller;

import com.pallet.common.Result;
import com.pallet.dto.PalletTransferDTO;
import com.pallet.entity.PalletTransfer;
import com.pallet.service.PalletTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/pallet-transfers")
@RequiredArgsConstructor
public class PalletTransferController {

    private final PalletTransferService palletTransferService;

    @GetMapping
    public Result<List<PalletTransfer>> list(
            @RequestParam(required = false) Long fromCarrierId,
            @RequestParam(required = false) Long toCarrierId,
            @RequestParam(required = false) Long shipperId,
            @RequestParam(required = false) String status) {
        return Result.success(palletTransferService.list(fromCarrierId, toCarrierId, shipperId, status));
    }

    @GetMapping("/{id}")
    public Result<PalletTransfer> getById(@PathVariable Long id) {
        return Result.success(palletTransferService.getById(id));
    }

    @GetMapping("/pickup-detail/{pickupDetailId}")
    public Result<List<PalletTransfer>> getByPickupDetailId(@PathVariable Long pickupDetailId) {
        return Result.success(palletTransferService.getByPickupDetailId(pickupDetailId));
    }

    @GetMapping("/pallet/{palletId}")
    public Result<List<PalletTransfer>> getByPalletId(@PathVariable Long palletId) {
        return Result.success(palletTransferService.getByPalletId(palletId));
    }

    @PostMapping
    public Result<List<PalletTransfer>> create(
            @Valid @RequestBody PalletTransferDTO dto,
            @RequestHeader(value = "X-User-Id", defaultValue = "2") Long userId) {
        return Result.success(palletTransferService.createTransfer(dto, userId));
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "2") Long userId) {
        palletTransferService.cancelTransfer(id, userId);
        return Result.success();
    }
}
