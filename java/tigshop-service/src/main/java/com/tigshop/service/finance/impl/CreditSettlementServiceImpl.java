package com.tigshop.service.finance.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.finance.CreditSettlementCancelDTO;
import com.tigshop.bean.dto.finance.CreditSettlementListDTO;
import com.tigshop.bean.dto.finance.CreditSettlementSubmitDTO;
import com.tigshop.bean.dto.finance.CreditSettlementUserListDTO;
import com.tigshop.bean.enums.finance.CreditOrderSettlementStatusEnum;
import com.tigshop.bean.enums.finance.CreditSettlementStatusEnum;
import com.tigshop.bean.enums.finance.CreditUserStatusEnum;
import com.tigshop.bean.enums.finance.SettlementTypeEnum;
import com.tigshop.bean.enums.user.UserCompanyType;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.finance.CreditOrder;
import com.tigshop.bean.model.finance.CreditSettlement;
import com.tigshop.bean.model.finance.CreditUser;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.user.UserCompany;
import com.tigshop.bean.vo.finance.CreditSettlementUserVO;
import com.tigshop.bean.vo.finance.CreditSettlementVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.authority.AdminUserMapper;
import com.tigshop.mapper.finance.CreditOrderMapper;
import com.tigshop.mapper.finance.CreditSettlementMapper;
import com.tigshop.mapper.finance.CreditUserMapper;
import com.tigshop.mapper.user.UserCompanyMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.CreditOrderService;
import com.tigshop.service.finance.CreditSettlementService;
import com.tigshop.service.finance.CreditUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 账期结算服务实现类
 *
 * @author Tigshop团队
 * @create 2026-01-14
 */
@RequiredArgsConstructor
@Service
public class CreditSettlementServiceImpl extends BaseServiceImpl<CreditSettlementMapper, CreditSettlement> implements CreditSettlementService {

    private final CreditUserMapper creditUserMapper;
    private final CreditOrderMapper creditOrderMapper;
    private final UserMapper userMapper;
    private final UserCompanyMapper userCompanyMapper;
    private final AdminUserMapper adminUserMapper;
    private final CreditOrderService creditOrderService;
    private final CreditUserService creditUserService;

    @Override
    public Page<CreditSettlementUserVO> getUserList(CreditSettlementUserListDTO listDTO) {
        Page<CreditUser> creditUserPage = new Page<>(listDTO.getPage(), listDTO.getSize());
        LambdaQueryWrapper<CreditUser> queryWrapper = new LambdaQueryWrapper<>();

        // 关键字搜索（用户名/手机号）
        if (StrUtil.isNotEmpty(listDTO.getKeyword())) {
            LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
            userQueryWrapper.like(User::getUsername, listDTO.getKeyword());
            List<User> userList = userMapper.selectList(userQueryWrapper);
            if (userList == null || userList.isEmpty()) {
                return PageUtil.convertPage(creditUserPage, List.of());
            }
            List<Integer> userIds = userList.stream().map(User::getUserId).toList();
            queryWrapper.in(CreditUser::getUserId, userIds);
        }
        
        queryWrapper.orderByDesc(CreditUser::getCreateTime);
        creditUserPage = creditUserMapper.selectPage(creditUserPage, queryWrapper);

        List<CreditSettlementUserVO> voList = creditUserPage.getRecords().stream().map(creditUser -> {
            CreditSettlementUserVO vo = new CreditSettlementUserVO();
            vo.setCreditUserId(creditUser.getCreditUserId());
            vo.setUserId(creditUser.getUserId());
            vo.setCreditLimit(creditUser.getCreditLimit());
            vo.setUsedAmount(creditUser.getUsedAmount());
            vo.setAvailableAmount(creditUser.getAvailableAmount());
            vo.setStatus(creditUser.getStatus());
            vo.setStatusText(CreditUserStatusEnum.getDescByCode(creditUser.getStatus()));

            // 查询用户信息
            User user = userMapper.selectById(creditUser.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setMobile(user.getMobile());
            }

            // 查询会员认证信息
            LambdaQueryWrapper<UserCompany> companyQuery = new LambdaQueryWrapper<>();
            companyQuery.eq(UserCompany::getUserId, creditUser.getUserId());
            companyQuery.eq(UserCompany::getStatus, 2);
            companyQuery.last("limit 1");
            UserCompany userCompany = userCompanyMapper.selectOne(companyQuery);
            if (userCompany != null) {
                vo.setType(userCompany.getType());
                vo.setTypeText(UserCompanyType.getTypeName(userCompany.getType()));
                vo.setContactName(userCompany.getContactName());
                vo.setCompanyName(userCompany.getCompanyName());
            }

            // 设置结算账期信息
            vo.setSettlementType(creditUser.getSettlementType());
            vo.setSettlementTypeText(SettlementTypeEnum.getDescByCode(creditUser.getSettlementType()));
            PeriodWindow currentWindow = getCurrentPeriodWindow(creditUser, LocalDate.now());
            vo.setSettlementDays(currentWindow.totalDays());

            // 统计待结算订单
            Object[] pendingInfo = creditOrderService.countPendingOrders(creditUser.getUserId());
            vo.setPendingOrderCount((Integer) pendingInfo[0]);
            vo.setPendingAmount((BigDecimal) pendingInfo[1]);

            // 计算剩余账期天数（自然月/季）
            vo.setRemainingDays(calculateRemainingDays(creditUser.getUserId(), currentWindow));

            return vo;
        }).toList();

        return PageUtil.convertPage(creditUserPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submit(CreditSettlementSubmitDTO submitDTO) {
        // 校验回款流水单号是否重复
        if (existsBySettlementSn(submitDTO.getSettlementSn())) {
            throw new GlobalException("回款流水单号已存在");
        }

        // 校验所选订单
        List<Integer> orderIds = submitDTO.getOrderIds();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Integer orderId : orderIds) {
            CreditOrder creditOrder = creditOrderMapper.selectByOrderId(orderId);
            if (creditOrder == null) {
                throw new GlobalException("订单不存在或不是账期订单");
            }
            if (!creditOrder.getUserId().equals(submitDTO.getUserId())) {
                throw new GlobalException("订单不属于该会员");
            }
            // 仅允许“待结算”的账期订单参与结算，避免退款中/已取消等状态被误结算
            if (!creditOrder.getSettlementStatus().equals(CreditOrderSettlementStatusEnum.UNSETTLED.getCode())) {
                throw new GlobalException("订单" + creditOrder.getOrderSn() + "当前状态为"
                        + CreditOrderSettlementStatusEnum.getDescByCode(creditOrder.getSettlementStatus())
                        + "，不可结算");
            }
            // 注意：credit_order.credit_amount 在售后退款审核通过时已同步扣减为“剩余待结算金额”，
            // 这里不再重复减 refundedAmount，否则会出现“部分退款被视为全额退款”的问题。
            BigDecimal netAmount = creditOrder.getCreditAmount() == null ? BigDecimal.ZERO : creditOrder.getCreditAmount();
            // 全额退款的订单也允许结算，避免长期处于未结算状态导致逾期
            totalAmount = totalAmount.add(netAmount);
        }

        // 创建结算记录
        CreditSettlement settlement = new CreditSettlement();
        settlement.setSettlementSn(submitDTO.getSettlementSn());
        settlement.setUserId(submitDTO.getUserId());

        CreditUser creditUser = creditUserMapper.selectByUserId(submitDTO.getUserId());
        if (creditUser != null) {
            settlement.setCreditUserId(creditUser.getCreditUserId());
        }

        settlement.setSettlementAmount(totalAmount);
        settlement.setOrderCount(orderIds.size());
        settlement.setRemark(submitDTO.getRemark());
        settlement.setSettlementTime(TigUtils.toTimestampYmdHms(submitDTO.getSettlementTime()));
        settlement.setSettlementStatus(CreditSettlementStatusEnum.SETTLED.getCode());
        Integer adminId = SecurityUtils.getCurrentAdminId();
        settlement.setOperatorId(adminId);
        AdminUser adminUser = adminUserMapper.selectById(adminId);
        settlement.setOperatorName(adminUser != null ? adminUser.getUsername() : "管理员");
        settlement.setCreateTime(StringUtils.getCurrentTime());
        settlement.setUpdateTime(StringUtils.getCurrentTime());
        this.save(settlement);

        // 更新订单结算状态
        creditOrderService.updateSettlementStatus(orderIds, settlement.getSettlementId(), submitDTO.getSettlementSn());

        // 恢复可用额度
        creditUserService.restoreAmount(submitDTO.getUserId(), totalAmount, "结算恢复额度");

        // 若本次结算后已清空所有待结算订单，则重置滚动结算周期起点
        creditUserService.resetPeriodStartIfNoPending(submitDTO.getUserId());

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancel(CreditSettlementCancelDTO cancelDTO) {
        CreditSettlement settlement = this.getById(cancelDTO.getSettlementId());
        if (settlement == null) {
            throw new GlobalException("结算记录不存在");
        }

        if (!settlement.getSettlementStatus().equals(CreditSettlementStatusEnum.SETTLED.getCode())) {
            throw new GlobalException("该结算记录已撤销，无法重复操作");
        }

        // 校验是否在当前账期内
        CreditUser creditUser = creditUserMapper.selectById(settlement.getCreditUserId());
        if (creditUser == null) {
            throw new GlobalException("授信会员不存在");
        }

        LocalDate settlementDate =  Instant.ofEpochSecond(settlement.getSettlementTime())
                .atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate today = LocalDate.now();

        boolean inCurrentPeriod;
        if (creditUser.getSettlementType() == 1) {
            // 月结：结算时间必须在当月
            inCurrentPeriod = settlementDate.getYear() == today.getYear()
                    && settlementDate.getMonthValue() == today.getMonthValue();
        } else {
            // 季结：结算时间必须在当季
            int settlementQuarter = (settlementDate.getMonthValue() - 1) / 3;
            int currentQuarter = (today.getMonthValue() - 1) / 3;
            inCurrentPeriod = settlementDate.getYear() == today.getYear()
                    && settlementQuarter == currentQuarter;
        }

        if (!inCurrentPeriod) {
            throw new GlobalException("仅支持撤销当前账期内的结算记录");
        }

        // 更新结算记录状态
        settlement.setSettlementStatus(CreditSettlementStatusEnum.CANCELLED.getCode());
        settlement.setCancelTime(StringUtils.getCurrentTime());
        settlement.setCancelReason(cancelDTO.getCancelReason());
        settlement.setUpdateTime(StringUtils.getCurrentTime());
        this.updateById(settlement);

        // 撤销订单结算状态
        creditOrderService.cancelSettlementStatus(settlement.getSettlementId());

        // 扣减可用额度（恢复已使用额度）
        creditUserService.deductAmount(settlement.getUserId(), settlement.getSettlementAmount(), "撤销结算扣减额度");

        return true;
    }

    @Override
    public Page<CreditSettlementVO> history(CreditSettlementListDTO listDTO) {
        Page<CreditSettlement> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        LambdaQueryWrapper<CreditSettlement> queryWrapper = new LambdaQueryWrapper<>();

        // 排序
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 会员ID筛选
        if (listDTO.getUserId() != null) {
            queryWrapper.eq(CreditSettlement::getUserId, listDTO.getUserId());
        }

        // 流水单号搜索
        if (StrUtil.isNotEmpty(listDTO.getSettlementSn())) {
            queryWrapper.like(CreditSettlement::getSettlementSn, listDTO.getSettlementSn());
        }

        // 结算状态筛选
        if (listDTO.getSettlementStatus() != null) {
            queryWrapper.eq(CreditSettlement::getSettlementStatus, listDTO.getSettlementStatus());
        }

        // 结算时间范围（格式：yyyy-MM-dd）
        if (StrUtil.isNotEmpty(listDTO.getSettlementTimeStart())) {
            LocalDateTime startOfDay = LocalDate.parse(listDTO.getSettlementTimeStart()).atStartOfDay();
            Long settlementTimeStart = startOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            queryWrapper.ge(CreditSettlement::getSettlementTime, settlementTimeStart / 1000);
        }
        if (StrUtil.isNotEmpty(listDTO.getSettlementTimeEnd())) {
            LocalDateTime endOfDay = LocalDate.parse(listDTO.getSettlementTimeEnd()).atTime(LocalTime.MAX);
            Long settlementTimeEnd = endOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            queryWrapper.le(CreditSettlement::getSettlementTime, settlementTimeEnd / 1000);
        }

        queryWrapper.orderByDesc(CreditSettlement::getSettlementTime);
        Page<CreditSettlement> settlementPage = this.page(page, queryWrapper);

        List<CreditSettlementVO> voList = settlementPage.getRecords().stream().map(settlement -> {
            CreditSettlementVO vo = new CreditSettlementVO();
            BeanUtils.copyProperties(settlement, vo);

            // 查询用户信息
            User user = userMapper.selectById(settlement.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
            }

            vo.setSettlementStatusText(CreditSettlementStatusEnum.getDescByCode(settlement.getSettlementStatus()));
            vo.setSettlementTime(TigUtils.handelTime(settlement.getSettlementTime()));
            vo.setCreateTime(TigUtils.handelTime(settlement.getCreateTime()));
            if (settlement.getCancelTime() != null) {
                vo.setCancelTime(TigUtils.handelTime(settlement.getCancelTime()));
            }

            return vo;
        }).toList();

        return PageUtil.convertPage(settlementPage, voList);
    }

    @Override
    public boolean existsBySettlementSn(String settlementSn) {
        CreditSettlement settlement = baseMapper.selectBySettlementSn(settlementSn);
        return settlement != null;
    }

    private int calculateRemainingDays(Integer userId, PeriodWindow currentWindow) {
        if (userId == null || currentWindow == null) {
            return 0;
        }
        // 如果有上期未结算的账期订单（逾期），剩余时间返回0
        if (hasPendingBeforePeriod(userId, currentWindow.startDate())) {
            return 0;
        }
        LocalDate today = LocalDate.now();
        long elapsedInPeriod = Math.max(0, ChronoUnit.DAYS.between(currentWindow.startDate(), today));
        long remaining = (long) currentWindow.totalDays() - elapsedInPeriod;
        return (int) Math.max(0, remaining);
    }

    /**
     * 检查是否有上期未结算的账期订单（订单时间在当前周期起点之前）
     */
    private boolean hasPendingBeforePeriod(Integer userId, LocalDate periodStart) {
        if (userId == null || periodStart == null) {
            return false;
        }
        long startEpochSecond = periodStart.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        Long count = creditOrderMapper.selectCount(new LambdaQueryWrapper<CreditOrder>()
                .eq(CreditOrder::getUserId, userId)
                .in(CreditOrder::getSettlementStatus,
                        CreditOrderSettlementStatusEnum.UNSETTLED.getCode(),
                        CreditOrderSettlementStatusEnum.REFUNDING.getCode())
                .lt(CreditOrder::getOrderTime, startEpochSecond));
        return count != null && count > 0;
    }

    private PeriodWindow getCurrentPeriodWindow(CreditUser creditUser, LocalDate today) {
        int settlementDays = SettlementTypeEnum.getDaysByCode(creditUser.getSettlementType());
        LocalDate baseStartDate = getBaseStartDate(creditUser);
        long daysPassed = Math.max(0, ChronoUnit.DAYS.between(baseStartDate, today));
        long periodIndex = daysPassed / settlementDays;
        LocalDate periodStart = baseStartDate.plusDays(periodIndex * settlementDays);
        LocalDate periodEnd = periodStart.plusDays(settlementDays - 1L);
        return new PeriodWindow(periodStart, periodEnd, settlementDays);
    }

    private LocalDate getBaseStartDate(CreditUser creditUser) {
        long startEpochSecond;
        if (creditUser.getPeriodStartTime() != null && creditUser.getPeriodStartTime() > 0) {
            startEpochSecond = creditUser.getPeriodStartTime();
        } else if (creditUser.getCreateTime() != null && creditUser.getCreateTime() > 0) {
            startEpochSecond = creditUser.getCreateTime() > 1000_000_000_000L
                    ? creditUser.getCreateTime() / 1000
                    : creditUser.getCreateTime();
        } else {
            startEpochSecond = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        }
        return Instant.ofEpochSecond(startEpochSecond).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private record PeriodWindow(LocalDate startDate, LocalDate endDate, int totalDays) {
    }
}
