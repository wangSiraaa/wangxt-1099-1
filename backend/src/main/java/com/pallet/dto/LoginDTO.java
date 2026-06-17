package com.pallet.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class LoginDTO {
    @NotBlank(message = "用户编码不能为空")
    private String userCode;
    @NotBlank(message = "密码不能为空")
    private String password;
}
