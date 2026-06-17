package com.pallet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pallet.common.*;
import com.pallet.entity.*;
import com.pallet.mapper.*;
import com.pallet.vo.DepositLifecycleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepositLifecycleService {

    private final PalletPickupMapper palletPickupMapper;
    private final PalletPickupDetailMapper palletPickupDetailMapper;
    private final PalletTransferMapper palletTransferMapper;
    private final PalletReturnMapper palletReturnMapper;
    private final PalletReturnDetailMapper palletReturnDetailMapper;
    private final DeductionMapper deductionMapper;
    private final PalletMapper palletMapper;

    public DepositLifecycleVO getLifecycleByPickupDetailId(Long pickupDetailId) {
        PalletPickupDetail pickupDetail = palletPickupDetailMapper.selectById(pickupDetailId);
        if (pickupDetail == null) {
            throw new BusinessException("领用明细不存在");
        }
        return buildLifecycleVO(pickupDetail);
    }

    public List<DepositLifecycleVO> getLifecyclesByPickupId(Long pickupId) {
        PalletPickup pickup = palletPickupMapper.selectById(pickupId);
        if (pickup == null) {
            throw new BusinessException("领用记录不存在");
        }
        List<PalletPickupDetail> details = palletPickupDetailMapper.selectList(
                new LambdaQueryWrapper<PalletPickupDetail>()
                        .eq(PalletPickupDetail::getPickupId, pickupId)
                        .orderByAsc(PalletPickupDetail::getId)
        );
        return details.stream()
                .map(this::buildLifecycleVO)
                .collect(Collectors.toList());
    }

    public List<DepositLifecycleVO> getLifecyclesByPalletId(Long palletId) {
        List<PalletPickupDetail> details = palletPickupDetailMapper.selectList(
                new LambdaQueryWrapper<PalletPickupDetail>()
                        .eq(PalletPickupDetail::getPalletId, palletId)
                        .orderByDesc(PalletPickupDetail::getCreateTime)
        );
        return details.stream()
                .map(this::buildLifecycleVO)
                .collect(Collectors.toList());
    }

    public List<DepositLifecycleVO> getLifecyclesByShipperId(Long shipperId, Boolean onlyUnreturned) {
        List<PalletPickup> pickups = palletPickupMapper.selectList(
                new LambdaQueryWrapper<PalletPickup>()
                        .eq(PalletPickup::getShipperId, shipperId)
                        .ne(PalletPickup::getStatus, BusinessConstants.PICKUP_STATUS_FULLY_RETURNED)
        );
        List<Long> pickupIds = pickups.stream().map(PalletPickup::getId).collect(Collectors.toList());
        if (pickupIds.isEmpty()) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<PalletPickupDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(PalletPickupDetail::getPickupId, pickupIds);
        if (Boolean.TRUE.equals(onlyUnreturned)) {
            wrapper.eq(PalletPickupDetail::getReturnStatus, "NOT_RETURNED");
        }
        wrapper.orderByDesc(PalletPickupDetail::getCreateTime);
        List<PalletPickupDetail> details = palletPickupDetailMapper.selectList(wrapper);
        return details.stream()
                .map(this::buildLifecycleVO)
                .collect(Collectors.toList());
    }

    public DepositLifecycleVO getLifecycleByPalletCode(String palletCode) {
        Pallet pallet = palletMapper.selectOne(
                new LambdaQueryWrapper<Pallet>()
                        .eq(Pallet::getPalletCode, palletCode)
                        .last("LIMIT 1")
        );
        if (pallet == null) {
            throw new BusinessException("托盘编码不存在");
        }
        List<PalletPickupDetail> details = palletPickupDetailMapper.selectList(
                new LambdaQueryWrapper<PalletPickupDetail>()
                        .eq(PalletPickupDetail::getPalletId, pallet.getId())
                        .orderByDesc(PalletPickupDetail::getCreateTime)
                        .last("LIMIT 1")
        );
        if (details.isEmpty()) {
            throw new BusinessException("该托盘无领用记录");
        }
        return buildLifecycleVO(details.get(0));
    }

    private DepositLifecycleVO buildLifecycleVO(PalletPickupDetail pickupDetail) {
        DepositLifecycleVO vo = new DepositLifecycleVO();
        vo.setPickupDetailId(pickupDetail.getId());
        vo.setPalletId(pickupDetail.getPalletId());
        vo.setPalletCode(pickupDetail.getPalletCode());

        PalletPickup pickup = palletPickupMapper.selectById(pickupDetail.getPickupId());
        if (pickup != null) {
            vo.setShipperId(pickup.getShipperId());
            vo.setShipperName(pickup.getShipperName());
            vo.setInitialCarrierId(pickup.getCarrierId());
            vo.setInitialCarrierName(pickup.getCarrierName());
            vo.setDestStoreId(pickup.getDestStoreId());
            vo.setDestStoreName(pickup.getDestStoreName());
            vo.setPickupTime(pickup.getCreateTime());
        }

        vo.setInitialDepositAmount(pickupDetail.getDepositAmount());
        vo.setCurrentDepositAmount(pickupDetail.getDepositAmount());
        vo.setCurrentStatus(getStatusText(pickupDetail.getReturnStatus()));

        List<DepositLifecycleVO.DepositLifecycleEventVO> events = new ArrayList<>();

        events.add(buildPickupEvent(pickup, pickupDetail));

        List<PalletTransfer> transfers = palletTransferMapper.selectList(
                new LambdaQueryWrapper<PalletTransfer>()
                        .eq(PalletTransfer::getPickupDetailId, pickupDetail.getId())
                        .eq(PalletTransfer::getStatus, BusinessConstants.TRANSFER_STATUS_CONFIRMED)
                        .orderByAsc(PalletTransfer::getCreateTime)
        );
        for (PalletTransfer transfer : transfers) {
            events.add(buildTransferEvent(transfer));
        }

        List<PalletReturnDetail> returnDetails = palletReturnDetailMapper.selectList(
                new LambdaQueryWrapper<PalletReturnDetail>()
                        .eq(PalletReturnDetail::getPickupId, pickupDetail.getPickupId())
                        .eq(PalletReturnDetail::getPalletId, pickupDetail.getPalletId())
        );
        for (PalletReturnDetail returnDetail : returnDetails) {
            PalletReturn palletReturn = palletReturnMapper.selectById(returnDetail.getReturnId());
            events.add(buildReturnEvent(palletReturn, returnDetail));
            if (returnDetail.getDeductionAmount() != null 
                && returnDetail.getDeductionAmount().compareTo(BigDecimal.ZERO) > 0) {
                List<Deduction> deductions = deductionMapper.selectList(
                        new LambdaQueryWrapper<Deduction>()
                                .eq(Deduction::getReturnId, returnDetail.getReturnId())
                );
                for (Deduction deduction : deductions) {
                    events.add(buildDeductionEvent(deduction, returnDetail));
                }
            }
        }

        events.sort(Comparator.comparing(DepositLifecycleVO.DepositLifecycleEventVO::getEventTime));

        vo.setEvents(events);
        if (events.isEmpty()) {
            vo.setLatestEventTime(pickupDetail.getCreateTime());
        } else {
            vo.setLatestEventTime(events.get(events.size() - 1).getEventTime());
        }

        BigDecimal totalDeducted = events.stream()
                .filter(e -> BusinessConstants.LIFECYCLE_TYPE_DEDUCTION.equals(e.getEventType()))
                .map(DepositLifecycleVO.DepositLifecycleEventVO::getDeductionAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalDeductedAmount(totalDeducted);
        vo.setCurrentDepositAmount(pickupDetail.getDepositAmount().subtract(totalDeducted));

        return vo;
    }

    private DepositLifecycleVO.DepositLifecycleEventVO buildPickupEvent(PalletPickup pickup, PalletPickupDetail detail) {
        DepositLifecycleVO.DepositLifecycleEventVO event = new DepositLifecycleVO.DepositLifecycleEventVO();
        event.setEventType(BusinessConstants.LIFECYCLE_TYPE_PICKUP);
        event.setEventTypeName("托盘领用");
        event.setEventNo(pickup.getPickupNo());
        event.setEventId(pickup.getId());
        event.setEventTime(pickup.getCreateTime());
        event.setOperatorId(pickup.getCreateBy());
        event.setToHolderId(pickup.getCarrierId());
        event.setToHolderName(pickup.getCarrierName());
        event.setDepositBearerId(pickup.getShipperId());
        event.setDepositBearerName(pickup.getShipperName());
        event.setDepositAmount(detail.getDepositAmount());
        event.setRemark("托盘领用，押金冻结");
        return event;
    }

    private DepositLifecycleVO.DepositLifecycleEventVO buildTransferEvent(PalletTransfer transfer) {
        DepositLifecycleVO.DepositLifecycleEventVO event = new DepositLifecycleVO.DepositLifecycleEventVO();
        event.setEventType(BusinessConstants.LIFECYCLE_TYPE_TRANSFER);
        event.setEventTypeName("托盘转运");
        event.setEventNo(transfer.getTransferNo());
        event.setEventId(transfer.getId());
        event.setEventTime(transfer.getCreateTime());
        event.setOperatorId(transfer.getCreateBy());
        event.setFromHolderId(transfer.getFromCarrierId());
        event.setFromHolderName(transfer.getFromCarrierName());
        event.setToHolderId(transfer.getToCarrierId());
        event.setToHolderName(transfer.getToCarrierName());
        event.setDepositBearerId(transfer.getDepositBearerId());
        event.setDepositBearerName(transfer.getDepositBearerName());
        event.setDepositAmount(transfer.getDepositAmount());
        event.setRemark(transfer.getRemark());
        return event;
    }

    private DepositLifecycleVO.DepositLifecycleEventVO buildReturnEvent(PalletReturn palletReturn, PalletReturnDetail returnDetail) {
        DepositLifecycleVO.DepositLifecycleEventVO event = new DepositLifecycleVO.DepositLifecycleEventVO();
        event.setEventType(BusinessConstants.LIFECYCLE_TYPE_RETURN);
        event.setEventTypeName("托盘归还");
        event.setEventNo(palletReturn.getReturnNo());
        event.setEventId(palletReturn.getId());
        event.setEventTime(palletReturn.getCreateTime());
        event.setOperatorId(palletReturn.getCreateBy());
        event.setFromHolderId(palletReturn.getCarrierId());
        event.setFromHolderName(palletReturn.getCarrierName());
        event.setReturnStatus(returnDetail.getReturnStatus());
        event.setReturnStatusName(getReturnStatusText(returnDetail.getReturnStatus()));
        event.setDamageType(returnDetail.getDamageType());
        event.setDamageTypeName(getDamageTypeText(returnDetail.getDamageType()));
        event.setDepositAmount(returnDetail.getDepositAmount());
        event.setDeductionAmount(returnDetail.getDeductionAmount());
        event.setRemark(returnDetail.getRemark());
        return event;
    }

    private DepositLifecycleVO.DepositLifecycleEventVO buildDeductionEvent(Deduction deduction, PalletReturnDetail returnDetail) {
        DepositLifecycleVO.DepositLifecycleEventVO event = new DepositLifecycleVO.DepositLifecycleEventVO();
        event.setEventType(BusinessConstants.LIFECYCLE_TYPE_DEDUCTION);
        event.setEventTypeName("押金扣款");
        event.setEventNo(deduction.getDeductionNo());
        event.setEventId(deduction.getId());
        event.setEventTime(deduction.getCreateTime());
        event.setOperatorId(deduction.getCreateBy());
        event.setDepositBearerId(deduction.getShipperId());
        event.setDepositBearerName(deduction.getShipperName());
        event.setDeductionAmount(deduction.getDeductionAmount());
        event.setDamageType(deduction.getDamageType());
        event.setDamageTypeName(getDamageTypeText(deduction.getDamageType()));
        event.setRemark(deduction.getRemark());
        return event;
    }

    private String getStatusText(String returnStatus) {
        if ("NOT_RETURNED".equals(returnStatus)) {
            return "使用中";
        }
        return getReturnStatusText(returnStatus);
    }

    private String getReturnStatusText(String returnStatus) {
        if (returnStatus == null) return "";
        switch (returnStatus) {
            case BusinessConstants.RETURN_STATUS_NORMAL:
                return "正常归还";
            case BusinessConstants.RETURN_STATUS_MISSING_PART:
                return "缺件";
            case BusinessConstants.RETURN_STATUS_STAIN:
                return "污损";
            case BusinessConstants.RETURN_STATUS_SCRAPPED:
                return "整托报废";
            case BusinessConstants.RETURN_STATUS_LOST:
                return "丢失";
            case BusinessConstants.RETURN_STATUS_DAMAGED:
                return "破损";
            default:
                return returnStatus;
        }
    }

    private String getDamageTypeText(String damageType) {
        if (damageType == null) return "";
        switch (damageType) {
            case BusinessConstants.DAMAGE_TYPE_MISSING_PART:
                return "缺件";
            case BusinessConstants.DAMAGE_TYPE_STAIN:
                return "污损";
            case BusinessConstants.DAMAGE_TYPE_SCRAPPED:
                return "整托报废";
            default:
                return damageType;
        }
    }
}
