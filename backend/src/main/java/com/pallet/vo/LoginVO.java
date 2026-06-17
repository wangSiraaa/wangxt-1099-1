package com.pallet.vo;

import lombok.Data;

@Data
public class LoginVO {
    private Long userId;
    private String userCode;
    private String userName;
    private String roleType;
    private String roleName;
    private String token;
}
