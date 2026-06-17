package com.pallet.controller;

import com.pallet.common.Result;
import com.pallet.dto.PalletReturnDTO;
import com.pallet.entity.PalletReturn;
import com.pallet.entity.PalletReturnDetail;
import com.pallet.service.PalletReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/pallet-returns")
@RequiredArgsConstructor
public class PalletReturnController {

    private final PalletReturnService palletReturnService;

    @GetMapping
    public Result<List<PalletReturn>> list(
            @RequestParam(required = false) Long carrierId,
            @RequestParam(required = false) Long shipperId,
            @RequestParam(required = false) String status) {
        return Result.success(palletReturnService.list(carrierId, shipperId, status));
    }

    @GetMapping("/{id}")
    public Result<PalletReturn> getById(@PathVariable Long id) {
        return Result.success(palletReturnService.getById(id));
    }

    @GetMapping("/{id}/details")
    public Result<List<PalletReturnDetail>> getDetails(@PathVariable Long id) {
        return Result.success(palletReturnService.getDetailsByReturnId(id));
    }

    @PostMapping
    public Result<PalletReturn> create(
            @Valid @RequestBody PalletReturnDTO dto,
            @RequestHeader(value = "X-User-Id", defaultValue = "3") Long userId) {
        return Result.success(palletReturnService.createReturn(dto, userId));
    }

    @PutMapping("/{id}")
    public Result<PalletReturn> update(
            @PathVariable Long id,
            @Valid @RequestBody PalletReturnDTO dto,
            @RequestHeader(value = "X-User-Id", defaultValue = "3") Long userId) {
        return Result.success(palletReturnService.updateReturn(id, dto, userId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "3") Long userId) {
        palletReturnService.deleteReturn(id, userId);
        return Result.success();
    }
}
