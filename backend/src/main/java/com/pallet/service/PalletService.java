package com.pallet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pallet.common.BusinessConstants;
import com.pallet.common.BusinessException;
import com.pallet.dto.PalletDTO;
import com.pallet.entity.Pallet;
import com.pallet.mapper.PalletMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PalletService {

    private final PalletMapper palletMapper;

    public List<Pallet> list(String status) {
        LambdaQueryWrapper<Pallet> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Pallet::getStatus, status);
        }
        wrapper.orderByAsc(Pallet::getPalletCode);
        return palletMapper.selectList(wrapper);
    }

    public Pallet getById(Long id) {
        Pallet pallet = palletMapper.selectById(id);
        if (pallet == null) {
            throw new BusinessException("托盘不存在");
        }
        return pallet;
    }

    public Pallet getByCode(String palletCode) {
        return palletMapper.selectOne(
                new LambdaQueryWrapper<Pallet>()
                        .eq(Pallet::getPalletCode, palletCode)
        );
    }

    public List<Pallet> getAvailablePallets() {
        return palletMapper.selectList(
                new LambdaQueryWrapper<Pallet>()
                        .eq(Pallet::getStatus, BusinessConstants.PALLET_STATUS_AVAILABLE)
                        .orderByAsc(Pallet::getPalletCode)
        );
    }

    @Transactional
    public Pallet createPallet(PalletDTO dto, Long userId) {
        Pallet existing = getByCode(dto.getPalletCode());
        if (existing != null) {
            throw new BusinessException("托盘编码已存在: " + dto.getPalletCode());
        }
        Pallet pallet = new Pallet();
        pallet.setPalletCode(dto.getPalletCode());
        pallet.setPalletType(dto.getPalletType());
        pallet.setDepositAmount(dto.getDepositAmount());
        pallet.setStatus(BusinessConstants.PALLET_STATUS_AVAILABLE);
        pallet.setRemark(dto.getRemark());
        pallet.setCreateBy(userId);
        palletMapper.insert(pallet);
        return pallet;
    }

    @Transactional
    public Pallet updatePallet(Long id, PalletDTO dto) {
        Pallet pallet = getById(id);
        pallet.setPalletType(dto.getPalletType());
        pallet.setDepositAmount(dto.getDepositAmount());
        pallet.setRemark(dto.getRemark());
        palletMapper.updateById(pallet);
        return pallet;
    }

    @Transactional
    public void updateStatus(Long id, String status) {
        Pallet pallet = getById(id);
        pallet.setStatus(status);
        palletMapper.updateById(pallet);
    }

    @Transactional
    public void deletePallet(Long id) {
        Pallet pallet = getById(id);
        if (!BusinessConstants.PALLET_STATUS_AVAILABLE.equals(pallet.getStatus())) {
            throw new BusinessException("只有闲置状态的托盘才能删除");
        }
        palletMapper.deleteById(id);
    }
}
