package com.pallet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pallet.common.*;
import com.pallet.dto.PalletTransferDTO;
import com.pallet.entity.*;
import com.pallet.mapper.PalletPickupDetailMapper;
import com.pallet.mapper.PalletTransferMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PalletTransferService {

    private final PalletTransferMapper palletTransferMapper;
    private final PalletPickupDetailMapper palletPickupDetailMapper;
    private final AuthService authService;
    private final AccountPeriodService accountPeriodService;
    private final BusinessNoGenerator noGenerator;

    public List<PalletTransfer> list(Long fromCarrierId, Long toCarrierId, Long shipperId, String status) {
        LambdaQueryWrapper<PalletTransfer> wrapper = new LambdaQueryWrapper<>();
        if (fromCarrierId != null) {
            wrapper.eq(PalletTransfer::getFromCarrierId, fromCarrierId);
        }
        if (toCarrierId != null) {
            wrapper.eq(PalletTransfer::getToCarrierId, toCarrierId);
        }
        if (shipperId != null) {
            wrapper.eq(PalletTransfer::getShipperId, shipperId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(PalletTransfer::getStatus, status);
        }
        wrapper.orderByDesc(PalletTransfer::getCreateTime);
        return palletTransferMapper.selectList(wrapper);
    }

    public PalletTransfer getById(Long id) {
        PalletTransfer transfer = palletTransferMapper.selectById(id);
        if (transfer == null) {
            throw new BusinessException("转运记录不存在");
        }
        return transfer;
    }

    public List<PalletTransfer> getByPickupDetailId(Long pickupDetailId) {
        return palletTransferMapper.selectList(
                new LambdaQueryWrapper<PalletTransfer>()
                        .eq(PalletTransfer::getPickupDetailId, pickupDetailId)
                        .orderByAsc(PalletTransfer::getCreateTime)
        );
    }

    public List<PalletTransfer> getByPalletId(Long palletId) {
        return palletTransferMapper.selectList(
                new LambdaQueryWrapper<PalletTransfer>()
                        .eq(PalletTransfer::getPalletId, palletId)
                        .orderByAsc(PalletTransfer::getCreateTime)
        );
    }

    @Transactional
    public List<PalletTransfer> createTransfer(PalletTransferDTO dto, Long userId) {
        accountPeriodService.checkPeriodOpen(dto.getTransferDate(), "托盘转运");
        AccountPeriod period = accountPeriodService.getByDate(dto.getTransferDate());

        SysUser fromCarrier = authService.getById(dto.getFromCarrierId());
        if (fromCarrier == null) {
            throw new BusinessException("转出承运商不存在");
        }
        if (!BusinessConstants.ROLE_CARRIER.equals(fromCarrier.getRoleType())) {
            throw new BusinessException("转出方必须是承运商角色");
        }

        SysUser toCarrier = authService.getById(dto.getToCarrierId());
        if (toCarrier == null) {
            throw new BusinessException("转入承运商不存在");
        }
        if (!BusinessConstants.ROLE_CARRIER.equals(toCarrier.getRoleType())) {
            throw new BusinessException("转入方必须是承运商角色");
        }

        if (dto.getFromCarrierId().equals(dto.getToCarrierId())) {
            throw new BusinessException("转出和转入承运商不能相同");
        }

        SysUser shipper = authService.getById(dto.getShipperId());
        if (shipper == null) {
            throw new BusinessException("发货方不存在");
        }

        Long depositBearerId = dto.getDepositBearerId() != null ? dto.getDepositBearerId() : dto.getToCarrierId();
        SysUser depositBearer = authService.getById(depositBearerId);
        if (depositBearer == null) {
            throw new BusinessException("押金承接方不存在");
        }

        List<PalletTransfer> transfers = new ArrayList<>();
        for (PalletTransferDTO.PalletTransferDetailDTO detailDTO : dto.getDetails()) {
            PalletPickupDetail pickupDetail = palletPickupDetailMapper.selectById(detailDTO.getPickupDetailId());
            if (pickupDetail == null) {
                throw new BusinessException("领用明细不存在");
            }
            if (!"NOT_RETURNED".equals(pickupDetail.getReturnStatus())) {
                throw new BusinessException(
                        String.format("托盘[%s]已归还，不能转运", pickupDetail.getPalletCode())
                );
            }
            if (pickupDetail.getCurrentHolderId() == null || !pickupDetail.getCurrentHolderId().equals(dto.getFromCarrierId())) {
                throw new BusinessException(
                        String.format("托盘[%s]当前持有人不是转出承运商[%s]，不能转运",
                                pickupDetail.getPalletCode(), fromCarrier.getUserName())
                );
            }

            PalletTransfer transfer = new PalletTransfer();
            transfer.setTransferNo(noGenerator.generateTransferNo());
            transfer.setPickupDetailId(detailDTO.getPickupDetailId());
            transfer.setPalletId(detailDTO.getPalletId());
            transfer.setPalletCode(pickupDetail.getPalletCode());
            transfer.setFromCarrierId(dto.getFromCarrierId());
            transfer.setFromCarrierName(fromCarrier.getUserName());
            transfer.setToCarrierId(dto.getToCarrierId());
            transfer.setToCarrierName(toCarrier.getUserName());
            transfer.setShipperId(dto.getShipperId());
            transfer.setShipperName(shipper.getUserName());
            transfer.setDepositBearerId(depositBearerId);
            transfer.setDepositBearerName(depositBearer.getUserName());
            transfer.setDepositAmount(pickupDetail.getDepositAmount());
            transfer.setTransferDate(dto.getTransferDate());
            transfer.setPeriodId(period != null ? period.getId() : null);
            transfer.setStatus(BusinessConstants.TRANSFER_STATUS_CONFIRMED);
            transfer.setRemark(detailDTO.getRemark() != null ? detailDTO.getRemark() : dto.getRemark());
            transfer.setCreateBy(userId);
            palletTransferMapper.insert(transfer);

            pickupDetail.setCurrentHolderId(dto.getToCarrierId());
            pickupDetail.setCurrentHolderName(toCarrier.getUserName());
            palletPickupDetailMapper.updateById(pickupDetail);

            transfers.add(transfer);
        }

        return transfers;
    }

    @Transactional
    public void cancelTransfer(Long id, Long userId) {
        PalletTransfer transfer = getById(id);
        if (BusinessConstants.TRANSFER_STATUS_CANCELLED.equals(transfer.getStatus())) {
            throw new BusinessException("转运记录已取消，不能重复取消");
        }

        if (transfer.getPeriodId() != null) {
            AccountPeriod period = accountPeriodService.getById(transfer.getPeriodId());
            if (period != null && BusinessConstants.PERIOD_STATUS_CLOSED.equals(period.getStatus())) {
                throw new BusinessException(
                        String.format("账期[%s]已关闭，不能取消转运记录", period.getPeriodName())
                );
            }
        }

        PalletPickupDetail pickupDetail = palletPickupDetailMapper.selectById(transfer.getPickupDetailId());
        if (pickupDetail == null) {
            throw new BusinessException("关联的领用明细不存在");
        }

        if (pickupDetail.getCurrentHolderId() == null || !pickupDetail.getCurrentHolderId().equals(transfer.getToCarrierId())) {
            throw new BusinessException("托盘当前持有人已变更，不能取消此转运记录");
        }

        if (!"NOT_RETURNED".equals(pickupDetail.getReturnStatus())) {
            throw new BusinessException("托盘已归还，不能取消转运记录");
        }

        List<PalletTransfer> laterTransfers = palletTransferMapper.selectList(
                new LambdaQueryWrapper<PalletTransfer>()
                        .eq(PalletTransfer::getPickupDetailId, transfer.getPickupDetailId())
                        .gt(PalletTransfer::getId, transfer.getId())
                        .eq(PalletTransfer::getStatus, BusinessConstants.TRANSFER_STATUS_CONFIRMED)
        );
        if (!laterTransfers.isEmpty()) {
            throw new BusinessException("存在后续转运记录，不能取消此转运");
        }

        pickupDetail.setCurrentHolderId(transfer.getFromCarrierId());
        pickupDetail.setCurrentHolderName(transfer.getFromCarrierName());
        palletPickupDetailMapper.updateById(pickupDetail);

        transfer.setStatus(BusinessConstants.TRANSFER_STATUS_CANCELLED);
        palletTransferMapper.updateById(transfer);
    }
}
