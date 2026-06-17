package com.pallet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pallet.common.BusinessConstants;
import com.pallet.common.BusinessException;
import com.pallet.dto.LoginDTO;
import com.pallet.entity.SysUser;
import com.pallet.mapper.SysUserMapper;
import com.pallet.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper sysUserMapper;

    public LoginVO login(LoginDTO dto) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUserCode, dto.getUserCode())
        );
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!user.getPassword().equals(dto.getPassword())) {
            throw new BusinessException("密码错误");
        }
        if (user.getStatus() != null && user.getStatus() != 1) {
            throw new BusinessException("用户已被禁用");
        }

        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setUserCode(user.getUserCode());
        vo.setUserName(user.getUserName());
        vo.setRoleType(user.getRoleType());
        vo.setRoleName(getRoleName(user.getRoleType()));
        vo.setToken("mock-token-" + user.getId() + "-" + System.currentTimeMillis());
        return vo;
    }

    private String getRoleName(String roleType) {
        switch (roleType) {
            case BusinessConstants.ROLE_SHIPPER:
                return "发货方";
            case BusinessConstants.ROLE_CARRIER:
                return "承运商";
            case BusinessConstants.ROLE_FINANCE:
                return "财务";
            default:
                return "未知角色";
        }
    }

    public SysUser getById(Long id) {
        return sysUserMapper.selectById(id);
    }

    public java.util.List<SysUser> listByRole(String roleType) {
        return sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getRoleType, roleType)
                        .eq(SysUser::getStatus, 1)
        );
    }

    public java.util.List<SysUser> listAll() {
        return sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getStatus, 1)
        );
    }
}
