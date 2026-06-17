package com.pallet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pallet.common.BusinessConstants;
import com.pallet.common.BusinessException;
import com.pallet.dto.AccountPeriodDTO;
import com.pallet.entity.AccountPeriod;
import com.pallet.mapper.AccountPeriodMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountPeriodService {

    private final AccountPeriodMapper accountPeriodMapper;

    public List<AccountPeriod> list() {
        return accountPeriodMapper.selectList(
                new LambdaQueryWrapper<AccountPeriod>()
                        .orderByDesc(AccountPeriod::getPeriodCode)
        );
    }

    public AccountPeriod getById(Long id) {
        AccountPeriod period = accountPeriodMapper.selectById(id);
        if (period == null) {
            throw new BusinessException("账期不存在");
        }
        return period;
    }

    public AccountPeriod getByDate(LocalDate date) {
        return accountPeriodMapper.selectOne(
                new LambdaQueryWrapper<AccountPeriod>()
                        .le(AccountPeriod::getStartDate, date)
                        .ge(AccountPeriod::getEndDate, date)
        );
    }

    public AccountPeriod getOpenPeriodByDate(LocalDate date) {
        return accountPeriodMapper.selectOne(
                new LambdaQueryWrapper<AccountPeriod>()
                        .le(AccountPeriod::getStartDate, date)
                        .ge(AccountPeriod::getEndDate, date)
                        .eq(AccountPeriod::getStatus, BusinessConstants.PERIOD_STATUS_OPEN)
        );
    }

    public void checkPeriodOpen(LocalDate date, String operation) {
        AccountPeriod period = getOpenPeriodByDate(date);
        if (period == null) {
            throw new BusinessException(operation + "失败：日期[" + date + "]所属账期已关闭，不能进行" + operation);
        }
    }

    @Transactional
    public AccountPeriod create(AccountPeriodDTO dto) {
        AccountPeriod existsCode = accountPeriodMapper.selectOne(
                new LambdaQueryWrapper<AccountPeriod>()
                        .eq(AccountPeriod::getPeriodCode, dto.getPeriodCode())
        );
        if (existsCode != null) {
            throw new BusinessException("账期编码已存在");
        }
        AccountPeriod overlap = accountPeriodMapper.selectOne(
                new LambdaQueryWrapper<AccountPeriod>()
                        .and(w -> w
                                .between(AccountPeriod::getStartDate, dto.getStartDate(), dto.getEndDate())
                                .or()
                                .between(AccountPeriod::getEndDate, dto.getStartDate(), dto.getEndDate())
                                .or()
                                .le(AccountPeriod::getStartDate, dto.getStartDate())
                                .ge(AccountPeriod::getEndDate, dto.getEndDate())
                        )
        );
        if (overlap != null) {
            throw new BusinessException("账期日期范围与现有账期重叠");
        }
        AccountPeriod period = new AccountPeriod();
        period.setPeriodCode(dto.getPeriodCode());
        period.setPeriodName(dto.getPeriodName());
        period.setStartDate(dto.getStartDate());
        period.setEndDate(dto.getEndDate());
        period.setStatus(BusinessConstants.PERIOD_STATUS_OPEN);
        period.setRemark(dto.getRemark());
        accountPeriodMapper.insert(period);
        return period;
    }

    @Transactional
    public void closePeriod(Long id, Long userId) {
        AccountPeriod period = getById(id);
        if (BusinessConstants.PERIOD_STATUS_CLOSED.equals(period.getStatus())) {
            throw new BusinessException("账期已关闭，不能重复关闭");
        }
        period.setStatus(BusinessConstants.PERIOD_STATUS_CLOSED);
        period.setCloseTime(LocalDateTime.now());
        period.setCloseBy(userId);
        accountPeriodMapper.updateById(period);
    }

    @Transactional
    public void reopenPeriod(Long id) {
        AccountPeriod period = getById(id);
        if (BusinessConstants.PERIOD_STATUS_OPEN.equals(period.getStatus())) {
            throw new BusinessException("账期未关闭，无需重开");
        }
        period.setStatus(BusinessConstants.PERIOD_STATUS_OPEN);
        period.setCloseTime(null);
        period.setCloseBy(null);
        accountPeriodMapper.updateById(period);
    }

    @Transactional
    public void delete(Long id) {
        AccountPeriod period = getById(id);
        if (BusinessConstants.PERIOD_STATUS_CLOSED.equals(period.getStatus())) {
            throw new BusinessException("已关闭的账期不能删除");
        }
        accountPeriodMapper.deleteById(id);
    }
}
