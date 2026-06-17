package com.pallet.controller;

import com.pallet.common.Result;
import com.pallet.service.DepositLifecycleService;
import com.pallet.vo.DepositLifecycleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deposit-lifecycles")
@RequiredArgsConstructor
public class DepositLifecycleController {

    private final DepositLifecycleService depositLifecycleService;

    @GetMapping("/by-pickup-detail/{pickupDetailId}")
    public Result<DepositLifecycleVO> getByPickupDetailId(@PathVariable Long pickupDetailId) {
        return Result.success(depositLifecycleService.getLifecycleByPickupDetailId(pickupDetailId));
    }

    @GetMapping("/by-pickup/{pickupId}")
    public Result<List<DepositLifecycleVO>> getByPickupId(@PathVariable Long pickupId) {
        return Result.success(depositLifecycleService.getLifecyclesByPickupId(pickupId));
    }

    @GetMapping("/by-pallet/{palletId}")
    public Result<List<DepositLifecycleVO>> getByPalletId(@PathVariable Long palletId) {
        return Result.success(depositLifecycleService.getLifecyclesByPalletId(palletId));
    }

    @GetMapping("/by-shipper/{shipperId}")
    public Result<List<DepositLifecycleVO>> getByShipperId(
            @PathVariable Long shipperId,
            @RequestParam(required = false, defaultValue = "false") Boolean onlyUnreturned) {
        return Result.success(depositLifecycleService.getLifecyclesByShipperId(shipperId, onlyUnreturned));
    }

    @GetMapping("/by-pallet-code/{palletCode}")
    public Result<DepositLifecycleVO> getByPalletCode(@PathVariable String palletCode) {
        return Result.success(depositLifecycleService.getLifecycleByPalletCode(palletCode));
    }
}
