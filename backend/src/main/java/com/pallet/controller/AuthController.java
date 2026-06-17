package com.pallet.controller;

import com.pallet.common.Result;
import com.pallet.dto.LoginDTO;
import com.pallet.entity.SysUser;
import com.pallet.service.AuthService;
import com.pallet.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    @GetMapping("/users")
    public Result<List<SysUser>> listUsers(
            @RequestParam(required = false) String roleType) {
        if (roleType != null && !roleType.isEmpty()) {
            return Result.success(authService.listByRole(roleType));
        }
        return Result.success(authService.listAll());
    }

    @GetMapping("/users/shippers")
    public Result<List<SysUser>> listShippers() {
        return Result.success(authService.listByRole("SHIPPER"));
    }

    @GetMapping("/users/carriers")
    public Result<List<SysUser>> listCarriers() {
        return Result.success(authService.listByRole("CARRIER"));
    }

    @GetMapping("/users/stores")
    public Result<List<SysUser>> listStores() {
        return Result.success(authService.listByRole("STORE"));
    }
}
