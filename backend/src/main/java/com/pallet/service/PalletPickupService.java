package com.pallet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pallet.common.*;
import com.pallet.dto.PalletPickupDTO;
import com.pallet.dto.PalletPickupDetailDTO;
import com.pallet.entity.*;
import com.pallet.mapper.PalletPickupDetailMapper;
import com.pallet.mapper.PalletPickupMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PalletPickupService {

    private final PalletPickupMapper palletPickupMapper;
    private final PalletPickupDetailMapper palletPickupDetailMapper;
    private final DepositService depositService;
    private final PalletService palletService;
    private final AuthService authService;
    private final AccountPeriodService accountPeriodService;
    private final BusinessNoGenerator noGenerator;

    public List<PalletPickup> list(Long shipperId, String status) {
        LambdaQueryWrapper<PalletPickup> wrapper = new LambdaQueryWrapper<>();
        if (shipperId != null) {
            wrapper.eq(PalletPickup::getShipperId, shipperId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(PalletPickup::getStatus, status);
        }
        wrapper.orderByDesc(PalletPickup::getCreateTime);
        return palletPickupMapper.selectList(wrapper);
    }

    public PalletPickup getById(Long id) {
        PalletPickup pickup = palletPickupMapper.selectById(id);
        if (pickup == null) {
            throw new BusinessException("领用记录不存在");
        }
        return pickup;
    }

    public List<PalletPickupDetail> getDetailsByPickupId(Long pickupId) {
        return palletPickupDetailMapper.selectList(
                new LambdaQueryWrapper<PalletPickupDetail>()
                        .eq(PalletPickupDetail::getPickupId, pickupId)
                        .orderByAsc(PalletPickupDetail::getId)
        );
    }

    public List<PalletPickupDetail> getUnreturnedDetailsByShipper(Long shipperId) {
        List<PalletPickup> pickups = palletPickupMapper.selectList(
                new LambdaQueryWrapper<PalletPickup>()
                        .eq(PalletPickup::getShipperId, shipperId)
                        .ne(PalletPickup::getStatus, BusinessConstants.PICKUP_STATUS_FULLY_RETURNED)
        );
        List<Long> pickupIds = new ArrayList<>();
        for (PalletPickup p : pickups) {
            pickupIds.add(p.getId());
        }
        if (pickupIds.isEmpty()) {
            return new ArrayList<>();
        }
        return palletPickupDetailMapper.selectList(
                new LambdaQueryWrapper<PalletPickupDetail>()
                        .in(PalletPickupDetail::getPickupId, pickupIds)
                        .eq(PalletPickupDetail::getReturnStatus, "NOT_RETURNED")
                        .orderByAsc(PalletPickupDetail::getId)
        );
    }

    @Transactional
    public PalletPickup createPickup(PalletPickupDTO dto, Long userId) {
        accountPeriodService.checkPeriodOpen(dto.getPickupDate(), "托盘领用");
        AccountPeriod period = accountPeriodService.getByDate(dto.getPickupDate());

        SysUser shipper = authService.getById(dto.getShipperId());
        if (shipper == null) {
            throw new BusinessException("发货方不存在");
        }
        if (!BusinessConstants.ROLE_SHIPPER.equals(shipper.getRoleType())) {
            throw new BusinessException("只有发货方可以领用托盘");
        }

        SysUser carrier = authService.getById(dto.getCarrierId());
        if (carrier == null) {
            throw new BusinessException("承运商不存在");
        }
        if (!BusinessConstants.ROLE_CARRIER.equals(carrier.getRoleType())) {
            throw new BusinessException("领用时指定的用户不是承运商角色");
        }

        SysUser destStore = authService.getById(dto.getDestStoreId());
        if (destStore == null) {
            throw new BusinessException("目的门店不存在");
        }
        if (!BusinessConstants.ROLE_STORE.equals(destStore.getRoleType())) {
            throw new BusinessException("目的门店必须是门店角色");
        }

        List<Pallet> pallets = new ArrayList<>();
        BigDecimal totalDeposit = BigDecimal.ZERO;
        for (PalletPickupDetailDTO detailDTO : dto.getDetails()) {
            Pallet pallet = palletService.getById(detailDTO.getPalletId());
            if (!BusinessConstants.PALLET_STATUS_AVAILABLE.equals(pallet.getStatus())) {
                throw new BusinessException(
                        String.format("托盘[%s]当前状态为[%s]，无法领用，请选择闲置状态的托盘",
                                pallet.getPalletCode(), pallet.getStatus())
                );
            }
            pallets.add(pallet);
            totalDeposit = totalDeposit.add(pallet.getDepositAmount());
        }

        depositService.checkSufficientDeposit(dto.getShipperId(), totalDeposit);
        depositService.freezeDeposit(dto.getShipperId(), totalDeposit);

        PalletPickup pickup = new PalletPickup();
        pickup.setPickupNo(noGenerator.generatePickupNo());
        pickup.setShipperId(dto.getShipperId());
        pickup.setShipperName(shipper.getUserName());
        pickup.setCarrierId(dto.getCarrierId());
        pickup.setCarrierName(carrier.getUserName());
        pickup.setDestStoreId(dto.getDestStoreId());
        pickup.setDestStoreName(destStore.getUserName());
        pickup.setPalletCount(dto.getDetails().size());
        pickup.setTotalDeposit(totalDeposit);
        pickup.setPickupDate(dto.getPickupDate());
        pickup.setPeriodId(period != null ? period.getId() : null);
        pickup.setStatus(BusinessConstants.PICKUP_STATUS_IN_USE);
        pickup.setRemark(dto.getRemark());
        pickup.setCreateBy(userId);
        palletPickupMapper.insert(pickup);

        for (int i = 0; i < pallets.size(); i++) {
            Pallet pallet = pallets.get(i);
            PalletPickupDetail detail = new PalletPickupDetail();
            detail.setPickupId(pickup.getId());
            detail.setPalletId(pallet.getId());
            detail.setPalletCode(pallet.getPalletCode());
            detail.setDepositAmount(pallet.getDepositAmount());
            detail.setCurrentHolderId(carrier.getId());
            detail.setCurrentHolderName(carrier.getUserName());
            detail.setReturnStatus("NOT_RETURNED");
            detail.setRemark(dto.getDetails().get(i).getRemark());
            palletPickupDetailMapper.insert(detail);

            palletService.updateStatus(pallet.getId(), BusinessConstants.PALLET_STATUS_IN_USE);
        }

        return pickup;
    }

    @Transactional
    public void updatePickupStatus(Long pickupId) {
        PalletPickup pickup = getById(pickupId);
        List<PalletPickupDetail> details = getDetailsByPickupId(pickupId);
        long notReturned = details.stream()
                .filter(d -> "NOT_RETURNED".equals(d.getReturnStatus()))
                .count();
        if (notReturned == 0) {
            pickup.setStatus(BusinessConstants.PICKUP_STATUS_FULLY_RETURNED);
        } else if (notReturned == details.size()) {
            pickup.setStatus(BusinessConstants.PICKUP_STATUS_IN_USE);
        } else {
            pickup.setStatus(BusinessConstants.PICKUP_STATUS_PARTIAL_RETURNED);
        }
        palletPickupMapper.updateById(pickup);
    }

    @Transactional
    public void updateDetailReturnStatus(Long detailId, String returnStatus) {
        PalletPickupDetail detail = palletPickupDetailMapper.selectById(detailId);
        if (detail == null) {
            throw new BusinessException("领用明细不存在");
        }
        detail.setReturnStatus(returnStatus);
        palletPickupDetailMapper.updateById(detail);
    }
}
