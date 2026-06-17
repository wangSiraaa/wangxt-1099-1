package com.pallet.controller;

import com.pallet.common.Result;
import com.pallet.dto.PalletPickupDTO;
import com.pallet.entity.PalletPickup;
import com.pallet.entity.PalletPickupDetail;
import com.pallet.service.PalletPickupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/pallet-pickups")
@RequiredArgsConstructor
public class PalletPickupController {

    private final PalletPickupService palletPickupService;

    @GetMapping
    public Result<List<PalletPickup>> list(
            @RequestParam(required = false) Long shipperId,
            @RequestParam(required = false) String status) {
        return Result.success(palletPickupService.list(shipperId, status));
    }

    @GetMapping("/{id}")
    public Result<PalletPickup> getById(@PathVariable Long id) {
        return Result.success(palletPickupService.getById(id));
    }

    @GetMapping("/{id}/details")
    public Result<List<PalletPickupDetail>> getDetails(@PathVariable Long id) {
        return Result.success(palletPickupService.getDetailsByPickupId(id));
    }

    @GetMapping("/unreturned/{shipperId}")
    public Result<List<PalletPickupDetail>> getUnreturnedDetails(@PathVariable Long shipperId) {
        return Result.success(palletPickupService.getUnreturnedDetailsByShipper(shipperId));
    }

    @PostMapping
    public Result<PalletPickup> create(
            @Valid @RequestBody PalletPickupDTO dto,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        return Result.success(palletPickupService.createPickup(dto, userId));
    }
}
