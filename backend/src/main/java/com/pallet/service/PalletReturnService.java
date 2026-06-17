package com.pallet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pallet.common.*;
import com.pallet.dto.PalletReturnDTO;
import com.pallet.dto.PalletReturnDetailDTO;
import com.pallet.entity.*;
import com.pallet.mapper.PalletReturnDetailMapper;
import com.pallet.mapper.PalletReturnMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PalletReturnService {

    private final PalletReturnMapper palletReturnMapper;
    private final PalletReturnDetailMapper palletReturnDetailMapper;
    private final PalletPickupService palletPickupService;
    private final PalletService palletService;
    private final DepositService depositService;
    private final DeductionService deductionService;
    private final AuthService authService;
    private final AccountPeriodService accountPeriodService;
    private final BusinessNoGenerator noGenerator;

    public List<PalletReturn> list(Long carrierId, Long shipperId, String status) {
        LambdaQueryWrapper<PalletReturn> wrapper = new LambdaQueryWrapper<>();
        if (carrierId != null) {
            wrapper.eq(PalletReturn::getCarrierId, carrierId);
        }
        if (shipperId != null) {
            wrapper.eq(PalletReturn::getShipperId, shipperId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(PalletReturn::getStatus, status);
        }
        wrapper.orderByDesc(PalletReturn::getCreateTime);
        return palletReturnMapper.selectList(wrapper);
    }

    public PalletReturn getById(Long id) {
        PalletReturn ret = palletReturnMapper.selectById(id);
        if (ret == null) {
            throw new BusinessException("归还记录不存在");
        }
        return ret;
    }

    public List<PalletReturnDetail> getDetailsByReturnId(Long returnId) {
        return palletReturnDetailMapper.selectList(
                new LambdaQueryWrapper<PalletReturnDetail>()
                        .eq(PalletReturnDetail::getReturnId, returnId)
                        .orderByAsc(PalletReturnDetail::getId)
        );
    }

    private void checkPeriodEditable(Long returnId, String operation) {
        PalletReturn ret = getById(returnId);
        if (ret.getPeriodId() != null) {
            AccountPeriod period = accountPeriodService.getById(ret.getPeriodId());
            if (period != null && BusinessConstants.PERIOD_STATUS_CLOSED.equals(period.getStatus())) {
                throw new BusinessException(
                        String.format("账期[%s]已关闭，%s失败", period.getPeriodName(), operation)
                );
            }
        }
    }

    @Transactional
    public PalletReturn createReturn(PalletReturnDTO dto, Long userId) {
        accountPeriodService.checkPeriodOpen(dto.getReturnDate(), "托盘归还");
        AccountPeriod period = accountPeriodService.getByDate(dto.getReturnDate());

        SysUser carrier = authService.getById(dto.getCarrierId());
        if (carrier == null) {
            throw new BusinessException("承运商不存在");
        }
        if (!BusinessConstants.ROLE_CARRIER.equals(carrier.getRoleType())) {
            throw new BusinessException("只有承运商可以归还托盘");
        }

        SysUser shipper = authService.getById(dto.getShipperId());
        if (shipper == null) {
            throw new BusinessException("发货方不存在");
        }

        List<PalletPickupDetail> unreturnedDetails = palletPickupService.getUnreturnedDetailsByShipper(dto.getShipperId());
        Map<Long, PalletPickupDetail> unreturnedMap = new HashMap<>();
        for (PalletPickupDetail d : unreturnedDetails) {
            unreturnedMap.put(d.getPalletId(), d);
        }

        int normalCount = 0;
        int damagedCount = 0;
        int lostCount = 0;
        BigDecimal totalDeduction = BigDecimal.ZERO;
        List<PalletReturnDetail> returnDetails = new ArrayList<>();
        List<PalletPickupDetail> toUpdatePickupDetails = new ArrayList<>();
        Set<Long> pickupIds = new HashSet<>();

        for (PalletReturnDetailDTO detailDTO : dto.getDetails()) {
            Pallet pallet = palletService.getById(detailDTO.getPalletId());
            PalletPickupDetail pickupDetail = unreturnedMap.get(detailDTO.getPalletId());

            if (pickupDetail == null) {
                throw new BusinessException(
                        String.format("托盘[%s]在发货方[%s]处没有未归还的领用记录",
                                pallet.getPalletCode(), shipper.getUserName())
                );
            }

            PalletReturnDetail retDetail = new PalletReturnDetail();
            retDetail.setPalletId(pallet.getId());
            retDetail.setPalletCode(pallet.getPalletCode());
            retDetail.setReturnStatus(detailDTO.getReturnStatus());
            retDetail.setDepositAmount(pallet.getDepositAmount());
            retDetail.setPickupId(pickupDetail.getPickupId());
            retDetail.setRemark(detailDTO.getRemark());
            pickupIds.add(pickupDetail.getPickupId());

            BigDecimal deduction = BigDecimal.ZERO;
            switch (detailDTO.getReturnStatus()) {
                case BusinessConstants.RETURN_STATUS_NORMAL:
                    normalCount++;
                    pickupDetail.setReturnStatus(BusinessConstants.RETURN_STATUS_NORMAL);
                    toUpdatePickupDetails.add(pickupDetail);
                    break;
                case BusinessConstants.RETURN_STATUS_DAMAGED:
                    damagedCount++;
                    deduction = noGenerator.calculateDamagedDeduction(pallet.getDepositAmount());
                    pickupDetail.setReturnStatus(BusinessConstants.RETURN_STATUS_DAMAGED);
                    toUpdatePickupDetails.add(pickupDetail);
                    break;
                case BusinessConstants.RETURN_STATUS_LOST:
                    lostCount++;
                    deduction = noGenerator.calculateLostDeduction(pallet.getDepositAmount());
                    pickupDetail.setReturnStatus(BusinessConstants.RETURN_STATUS_LOST);
                    toUpdatePickupDetails.add(pickupDetail);
                    break;
                default:
                    throw new BusinessException("未知的归还状态: " + detailDTO.getReturnStatus());
            }
            retDetail.setDeductionAmount(deduction);
            totalDeduction = totalDeduction.add(deduction);
            returnDetails.add(retDetail);
        }

        PalletReturn ret = new PalletReturn();
        ret.setReturnNo(noGenerator.generateReturnNo());
        ret.setCarrierId(dto.getCarrierId());
        ret.setCarrierName(carrier.getUserName());
        ret.setShipperId(dto.getShipperId());
        ret.setShipperName(shipper.getUserName());
        ret.setReturnCount(dto.getDetails().size());
        ret.setNormalCount(normalCount);
        ret.setDamagedCount(damagedCount);
        ret.setLostCount(lostCount);
        ret.setReturnDate(dto.getReturnDate());
        ret.setPeriodId(period != null ? period.getId() : null);
        ret.setDeductionAmount(totalDeduction);
        ret.setStatus(BusinessConstants.RETURN_STATUS_NORMAL);
        ret.setRemark(dto.getRemark());
        ret.setCreateBy(userId);
        palletReturnMapper.insert(ret);

        for (PalletReturnDetail rd : returnDetails) {
            rd.setReturnId(ret.getId());
            palletReturnDetailMapper.insert(rd);
        }

        for (PalletPickupDetail pd : toUpdatePickupDetails) {
            palletPickupService.updateDetailReturnStatus(pd.getId(), pd.getReturnStatus());
        }
        for (Long pickupId : pickupIds) {
            palletPickupService.updatePickupStatus(pickupId);
        }

        BigDecimal unfreezeDeposit = returnDetails.stream()
                .map(PalletReturnDetail::getDepositAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        depositService.unfreezeDeposit(dto.getShipperId(), unfreezeDeposit);

        if (damagedCount > 0 || lostCount > 0) {
            if (damagedCount > 0) {
                deductionService.createDeduction(
                        dto.getShipperId(),
                        shipper.getUserName(),
                        BusinessConstants.DEDUCTION_TYPE_DAMAGED,
                        returnDetails.stream()
                                .filter(d -> BusinessConstants.RETURN_STATUS_DAMAGED.equals(d.getReturnStatus()))
                                .map(PalletReturnDetail::getDeductionAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add),
                        dto.getReturnDate(),
                        period != null ? period.getId() : null,
                        damagedCount,
                        ret.getId(),
                        userId
                );
            }
            if (lostCount > 0) {
                deductionService.createDeduction(
                        dto.getShipperId(),
                        shipper.getUserName(),
                        BusinessConstants.DEDUCTION_TYPE_LOST,
                        returnDetails.stream()
                                .filter(d -> BusinessConstants.RETURN_STATUS_LOST.equals(d.getReturnStatus()))
                                .map(PalletReturnDetail::getDeductionAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add),
                        dto.getReturnDate(),
                        period != null ? period.getId() : null,
                        lostCount,
                        ret.getId(),
                        userId
                );
            }
            depositService.deductDeposit(dto.getShipperId(), totalDeduction);
        }

        for (PalletReturnDetail rd : returnDetails) {
            String status = rd.getReturnStatus();
            if (BusinessConstants.RETURN_STATUS_NORMAL.equals(status)) {
                palletService.updateStatus(rd.getPalletId(), BusinessConstants.PALLET_STATUS_AVAILABLE);
            } else if (BusinessConstants.RETURN_STATUS_DAMAGED.equals(status)) {
                palletService.updateStatus(rd.getPalletId(), BusinessConstants.PALLET_STATUS_DAMAGED);
            } else if (BusinessConstants.RETURN_STATUS_LOST.equals(status)) {
                palletService.updateStatus(rd.getPalletId(), BusinessConstants.PALLET_STATUS_LOST);
            }
        }

        return ret;
    }

    @Transactional
    public PalletReturn updateReturn(Long id, PalletReturnDTO dto, Long userId) {
        checkPeriodEditable(id, "修改归还记录");

        PalletReturn oldReturn = getById(id);
        deleteReturn(id, userId);
        return createReturn(dto, userId);
    }

    @Transactional
    public void deleteReturn(Long id, Long userId) {
        checkPeriodEditable(id, "删除归还记录");

        PalletReturn ret = getById(id);
        List<PalletReturnDetail> details = getDetailsByReturnId(id);

        for (PalletReturnDetail rd : details) {
            PalletPickupDetail pickupDetail = palletPickupService.getDetailsByPickupId(rd.getPickupId())
                    .stream()
                    .filter(d -> d.getPalletId().equals(rd.getPalletId()))
                    .findFirst()
                    .orElse(null);
            if (pickupDetail != null) {
                palletPickupService.updateDetailReturnStatus(pickupDetail.getId(), "NOT_RETURNED");
            }
            palletService.updateStatus(rd.getPalletId(), BusinessConstants.PALLET_STATUS_IN_USE);
        }

        BigDecimal restoreDeposit = details.stream()
                .map(PalletReturnDetail::getDepositAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal restoreDeduction = ret.getDeductionAmount();

        if (restoreDeduction.compareTo(BigDecimal.ZERO) > 0) {
            DepositBalance balance = depositService.getOrCreateBalance(ret.getShipperId());
            balance.setTotalDeducted(balance.getTotalDeducted().subtract(restoreDeduction));
            balance.setCurrentBalance(balance.getCurrentBalance().add(restoreDeduction));
            deductionService.deleteByReturnId(id);
        }
        depositService.freezeDeposit(ret.getShipperId(), restoreDeposit);

        Set<Long> pickupIds = new HashSet<>();
        for (PalletReturnDetail rd : details) {
            if (rd.getPickupId() != null) {
                pickupIds.add(rd.getPickupId());
            }
        }
        for (Long pickupId : pickupIds) {
            palletPickupService.updatePickupStatus(pickupId);
        }

        palletReturnMapper.deleteById(id);
    }
}
