package com.tigshop.service.finance.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.finance.CreditUserCreateDTO;
import com.tigshop.bean.dto.finance.CreditUserListDTO;
import com.tigshop.bean.dto.finance.CreditUserUpdateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.enums.finance.CreditOrderSettlementStatusEnum;
import com.tigshop.bean.enums.finance.CreditPaymentStatusEnum;
import com.tigshop.bean.enums.finance.CreditUserStatusEnum;
import com.tigshop.bean.enums.finance.SettlementTypeEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.enums.user.UserCompanyType;
import com.tigshop.bean.model.finance.CreditOrder;
import com.tigshop.bean.model.finance.CreditUser;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.user.UserCompany;
import com.tigshop.bean.vo.finance.CreditUserListVO;
import com.tigshop.bean.vo.finance.CreditUserVO;
import com.tigshop.bean.vo.finance.UserCreditDetailVO;
import com.tigshop.bean.vo.finance.UserCreditInfoVO;
import com.tigshop.bean.vo.order.OrderItemVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.finance.CreditOrderMapper;
import com.tigshop.mapper.finance.CreditUserMapper;
import com.tigshop.mapper.user.UserCompanyMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.CreditUserService;
import com.tigshop.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.time.Instant;
import java.util.stream.Collectors;

/**
 * 授信会员服务实现类
 *
 * @author Tigshop团队
 * @create 2026-01-14
 */
@RequiredArgsConstructor
@Service
public class CreditUserServiceImpl extends BaseServiceImpl<CreditUserMapper, CreditUser> implements CreditUserService {

    private final UserMapper userMapper;
    private final UserCompanyMapper userCompanyMapper;
    private final CreditOrderMapper creditOrderMapper;
    private final ApplicationContext applicationContext;
    private final com.tigshop.service.setting.ConfigService configService;

    /**
     * 覆盖通用 updateField：当更新字段是 status（启用/禁用）时，走专门的 setStatus 逻辑，
     * 避免绕过后续可能新增的业务校验/联动逻辑。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateField(UpdateFieldDTO updateFieldDTO, String[] allowFields) {
        String column = this.getColumnByPropertyName(updateFieldDTO.getField());
        if ("status".equals(column)) {
            Integer status;
            try {
                status = Integer.valueOf(updateFieldDTO.getVal());
            } catch (Exception e) {
                throw new GlobalException("无效的状态值");
            }
            this.setStatus(updateFieldDTO.getId(), status);
            return true;
        }
        return super.updateField(updateFieldDTO, allowFields);
    }

    @Override
    public Page<CreditUserListVO> list(CreditUserListDTO listDTO) {
        Page<CreditUser> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        LambdaQueryWrapper<CreditUser> queryWrapper = new LambdaQueryWrapper<>();

        // 排序
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 状态筛选
        if (listDTO.getStatus() != null) {
            queryWrapper.eq(CreditUser::getStatus, listDTO.getStatus());
        }

        // 用户名搜索
        if (StrUtil.isNotEmpty(listDTO.getUsername())) {
            LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
            userQueryWrapper.like(User::getUsername, listDTO.getUsername());
            List<User> userList = userMapper.selectList(userQueryWrapper);
            if (userList == null || userList.isEmpty()) {
                return PageUtil.convertPage(page, List.of());
            }
            List<Integer> userIds = userList.stream().map(User::getUserId).toList();
            queryWrapper.in(CreditUser::getUserId, userIds);
        }

        // 会员类型筛选
        if (listDTO.getType() != null) {
            LambdaQueryWrapper<UserCompany> companyQueryWrapper = new LambdaQueryWrapper<>();
            companyQueryWrapper.eq(UserCompany::getType, listDTO.getType());
            companyQueryWrapper.eq(UserCompany::getStatus, 2);
            List<UserCompany> companyList = userCompanyMapper.selectList(companyQueryWrapper);
            if (companyList == null || companyList.isEmpty()) {
                return PageUtil.convertPage(page, List.of());
            }
            List<Integer> userIds = companyList.stream().map(UserCompany::getUserId).toList();
            queryWrapper.in(CreditUser::getUserId, userIds);
        }

        queryWrapper.orderByDesc(CreditUser::getCreateTime);
        Page<CreditUser> creditUserPage = this.page(page, queryWrapper);

        List<CreditUserListVO> voList = creditUserPage.getRecords().stream().map(creditUser -> {
            CreditUserListVO vo = new CreditUserListVO();
            BeanUtils.copyProperties(creditUser, vo);

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

            vo.setSettlementTypeText(SettlementTypeEnum.getDescByCode(creditUser.getSettlementType()));
            vo.setStatusText(CreditUserStatusEnum.getDescByCode(creditUser.getStatus()));
            vo.setCreateTime(TigUtils.handelTime(creditUser.getCreateTime()));

            return vo;
        }).toList();

        return PageUtil.convertPage(creditUserPage, voList);
    }

    @Override
    public CreditUserVO detail(Integer creditUserId) {
        CreditUser creditUser = this.getById(creditUserId);
        if (creditUser == null) {
            throw new GlobalException("授信会员不存在");
        }

        CreditUserVO vo = new CreditUserVO();
        BeanUtils.copyProperties(creditUser, vo);

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

        vo.setSettlementTypeText(SettlementTypeEnum.getDescByCode(creditUser.getSettlementType()));
        vo.setSettlementDays(SettlementTypeEnum.getDaysByCode(creditUser.getSettlementType()));
        vo.setStatusText(CreditUserStatusEnum.getDescByCode(creditUser.getStatus()));
        vo.setCreateTime(TigUtils.handelTime(creditUser.getCreateTime()));
        vo.setUpdateTime(TigUtils.handelTime(creditUser.getUpdateTime()));

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean create(CreditUserCreateDTO createDTO) {
        // 校验会员是否存在
        User user = userMapper.selectById(createDTO.getUserId());
        if (user == null) {
            throw new GlobalException("会员不存在");
        }

        // 校验会员是否已通过认证
        LambdaQueryWrapper<UserCompany> companyQuery = new LambdaQueryWrapper<>();
        companyQuery.eq(UserCompany::getUserId, createDTO.getUserId());
        companyQuery.eq(UserCompany::getStatus, 2);
        companyQuery.last("limit 1");
        UserCompany userCompany = userCompanyMapper.selectOne(companyQuery);
        if (userCompany == null) {
            throw new GlobalException("该会员未完成认证，无法添加为授信会员");
        }

        // 校验是否已是授信会员
        CreditUser existCreditUser = baseMapper.selectByUserId(createDTO.getUserId());
        if (existCreditUser != null) {
            throw new GlobalException("该会员已是授信会员");
        }

        // 校验结算账期类型
        if (createDTO.getSettlementType() != 1 && createDTO.getSettlementType() != 2) {
            throw new GlobalException("请选择有效的结算账期类型");
        }

        // 创建授信会员
        CreditUser creditUser = new CreditUser();
        creditUser.setUserId(createDTO.getUserId());
        creditUser.setCreditLimit(createDTO.getCreditLimit());
        creditUser.setUsedAmount(BigDecimal.ZERO);
        creditUser.setAvailableAmount(createDTO.getCreditLimit());
        creditUser.setSettlementType(createDTO.getSettlementType());
        creditUser.setStatus(createDTO.getStatus());
        // 初始化结算周期起点（滚动周期，从当天0点开始）
        long periodStartEpochSecond = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        creditUser.setPeriodStartTime(periodStartEpochSecond);
        creditUser.setCreateTime(StringUtils.getCurrentTime());
        creditUser.setUpdateTime(StringUtils.getCurrentTime());

        return this.save(creditUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(CreditUserUpdateDTO updateDTO) {
        CreditUser creditUser = this.getById(updateDTO.getCreditUserId());
        if (creditUser == null) {
            throw new GlobalException("授信会员不存在");
        }

        // 校验信用额度调整
        if (updateDTO.getCreditLimit() != null) {
            BigDecimal newAvailable = updateDTO.getCreditLimit().subtract(creditUser.getUsedAmount());
            if (newAvailable.compareTo(BigDecimal.ZERO) < 0) {
                throw new GlobalException("信用额度不能小于已使用额度（" + creditUser.getUsedAmount() + configService.getCurrencyName() + "）");
            }
            creditUser.setCreditLimit(updateDTO.getCreditLimit());
            creditUser.setAvailableAmount(newAvailable);
        }

        if (updateDTO.getSettlementType() != null) {
            if (updateDTO.getSettlementType() != 1 && updateDTO.getSettlementType() != 2) {
                throw new GlobalException("请选择有效的结算账期类型");
            }
            creditUser.setSettlementType(updateDTO.getSettlementType());
        }

        if (updateDTO.getStatus() != null) {
            creditUser.setStatus(updateDTO.getStatus());
        }

        creditUser.setUpdateTime(StringUtils.getCurrentTime());
        return this.updateById(creditUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Integer creditUserId) {
        CreditUser creditUser = this.getById(creditUserId);
        if (creditUser == null) {
            throw new GlobalException("授信会员不存在");
        }

        // 检查是否有待结算的账期订单
        LambdaQueryWrapper<CreditOrder> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(CreditOrder::getCreditUserId, creditUserId);
        orderQuery.eq(CreditOrder::getSettlementStatus, CreditOrderSettlementStatusEnum.UNSETTLED.getCode());
        long unsettledCount = creditOrderMapper.selectCount(orderQuery);
        if (unsettledCount > 0) {
            throw new GlobalException("存在待结算的账期订单，无法删除");
        }

        return this.removeById(creditUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setStatus(Integer creditUserId, Integer status) {
        CreditUser creditUser = this.getById(creditUserId);
        if (creditUser == null) {
            throw new GlobalException("授信会员不存在");
        }
        if (!Objects.equals(status, CreditUserStatusEnum.ENABLED.getCode())
                && !Objects.equals(status, CreditUserStatusEnum.DISABLED.getCode())) {
            throw new GlobalException("无效的状态值");
        }
        creditUser.setStatus(status);
        creditUser.setUpdateTime(StringUtils.getCurrentTime());
        this.updateById(creditUser);
    }

    @Override
    public CreditUser getByUserId(Integer userId) {
        return baseMapper.selectByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductAmount(Integer userId, BigDecimal amount, String remark) {
        // 先校验授信会员是否存在
        CreditUser creditUser = baseMapper.selectByUserId(userId);
        if (creditUser == null) {
            throw new GlobalException("授信会员不存在");
        }

        // 使用数据库原子操作扣减额度，同时校验可用额度是否充足
        // available_amount >= amount 保证不会超扣
        boolean updated = this.lambdaUpdate()
                .setIncrBy(CreditUser::getUsedAmount, amount)
                .setDecrBy(CreditUser::getAvailableAmount, amount)
                .set(CreditUser::getUpdateTime, StringUtils.getCurrentTime())
                .eq(CreditUser::getUserId, userId)
                .ge(CreditUser::getAvailableAmount, amount)
                .update();

        if (!updated) {
            throw new GlobalException("账期可用额度不足");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreAmount(Integer userId, BigDecimal amount, String remark) {
        // 先校验授信会员是否存在
        CreditUser creditUser = baseMapper.selectByUserId(userId);
        if (creditUser == null) {
            throw new GlobalException("授信会员不存在");
        }

        // 使用数据库原子操作恢复额度
        // used_amount >= amount 保证不会出现负数
        boolean updated = this.lambdaUpdate()
                .setDecrBy(CreditUser::getUsedAmount, amount)
                .setIncrBy(CreditUser::getAvailableAmount, amount)
                .set(CreditUser::getUpdateTime, StringUtils.getCurrentTime())
                .eq(CreditUser::getUserId, userId)
                .ge(CreditUser::getUsedAmount, amount)
                .update();

        if (!updated) {
            throw new GlobalException("恢复额度失败，已用额度不足");
        }
    }

    @Override
    public UserCreditInfoVO getUserCreditInfo(Integer userId, BigDecimal orderAmount) {
        CreditUser creditUser = baseMapper.selectByUserId(userId);
        if (creditUser == null) {
            return null;
        }

        PeriodWindow currentWindow = getCurrentPeriodWindow(creditUser, LocalDate.now());
        UserCreditInfoVO info = new UserCreditInfoVO();
        info.setCreditLimit(creditUser.getCreditLimit());
        info.setUsedAmount(creditUser.getUsedAmount());
        info.setAvailableAmount(creditUser.getAvailableAmount());
        info.setSettlementType(creditUser.getSettlementType());
        info.setSettlementTypeText(SettlementTypeEnum.getDescByCode(creditUser.getSettlementType()));
        info.setSettlementDays(currentWindow.totalDays());
        info.setRemainingDays(calculateRemainingDays(creditUser, currentWindow));

        // 判断账期支付状态：冻结 > 逾期 > 正常
        boolean isFrozen = !Objects.equals(creditUser.getStatus(), CreditUserStatusEnum.ENABLED.getCode());
        boolean isOverdue = hasPendingBeforePeriod(creditUser.getUserId(), currentWindow.startDate());

        if (isFrozen) {
            // 冻结状态：status为禁用
            info.setCreditPaymentStatus(CreditPaymentStatusEnum.FROZEN.getCode());
            info.setCreditPaymentStatusText(CreditPaymentStatusEnum.FROZEN.getDescription());
        } else if (isOverdue) {
            // 逾期状态：有逾期未结算的订单
            info.setCreditPaymentStatus(CreditPaymentStatusEnum.OVERDUE.getCode());
            info.setCreditPaymentStatusText(CreditPaymentStatusEnum.OVERDUE.getDescription());
        } else {
            // 正常状态
            info.setCreditPaymentStatus(CreditPaymentStatusEnum.NORMAL.getCode());
            info.setCreditPaymentStatusText(CreditPaymentStatusEnum.NORMAL.getDescription());
        }

        // 仅当“非授信会员”时不可用，其余情况均允许使用账期支付
        info.setCanUseCredit(true);
        return info;
    }

    @Override
    public UserCreditDetailVO getUserCreditDetail(Integer userId) {
        // 后台未开启账期支付时，不返回账期信息，避免前台展示已关闭的功能。
        String useCredit = configService.getConfigVal(SettingsEnum.USE_CREDIT);
        if (!"1".equals(useCredit)) {
            return null;
        }

        CreditUser creditUser = baseMapper.selectByUserId(userId);
        if (creditUser == null) {
            throw new GlobalException("您不是授信会员，无法查看账期信息");
        }

        PeriodWindow currentWindow = getCurrentPeriodWindow(creditUser, LocalDate.now());
        int settlementDays = currentWindow.totalDays();
        int remainingDays = calculateRemainingDays(creditUser, currentWindow);
        long nowEpochSecond = Instant.now().getEpochSecond();

        UserCreditDetailVO vo = new UserCreditDetailVO();
        vo.setCreditLimit(creditUser.getCreditLimit());
        vo.setUsedAmount(creditUser.getUsedAmount());
        vo.setAvailableAmount(creditUser.getAvailableAmount());
        vo.setSettlementType(creditUser.getSettlementType());
        vo.setSettlementDays(settlementDays);
        vo.setRemainingDays(remainingDays);

        // 未结算订单：包含“未结算/退款中”，已取消不展示
        List<CreditOrder> pendingOrders = creditOrderMapper.selectList(new LambdaQueryWrapper<CreditOrder>()
                .eq(CreditOrder::getUserId, userId)
                .in(CreditOrder::getSettlementStatus,
                        CreditOrderSettlementStatusEnum.UNSETTLED.getCode(),
                        CreditOrderSettlementStatusEnum.REFUNDING.getCode())
                .orderByDesc(CreditOrder::getOrderTime));

        // 已结算订单
        List<CreditOrder> settledOrders = creditOrderMapper.selectList(new LambdaQueryWrapper<CreditOrder>()
                .eq(CreditOrder::getUserId, userId)
                .eq(CreditOrder::getSettlementStatus, CreditOrderSettlementStatusEnum.SETTLED.getCode())
                .orderByDesc(CreditOrder::getSettlementTime));

        vo.setPendingOrders(buildUserCreditOrderVOList(pendingOrders, creditUser, nowEpochSecond));
        vo.setSettledOrders(buildUserCreditOrderVOList(settledOrders, creditUser, nowEpochSecond));

        return vo;
    }

    @Override
    public boolean canUseCreditPayment(Integer userId, CreditUser creditUser) {
        if (creditUser == null || !Objects.equals(creditUser.getStatus(), CreditUserStatusEnum.ENABLED.getCode())) {
            return false;
        }
        if (creditUser.getAvailableAmount() == null || creditUser.getAvailableAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        PeriodWindow currentWindow = getCurrentPeriodWindow(creditUser, LocalDate.now());
        return !hasPendingBeforePeriod(userId, currentWindow.startDate());
    }

    private int calculateRemainingDays(CreditUser creditUser, PeriodWindow currentWindow) {
        if (creditUser == null || currentWindow == null) {
            return 0;
        }
        // 如果有上期未结算的账期订单（逾期），剩余时间返回0
        if (hasPendingBeforePeriod(creditUser.getUserId(), currentWindow.startDate())) {
            return 0;
        }
        LocalDate today = LocalDate.now();
        long elapsedInPeriod = Math.max(0, ChronoUnit.DAYS.between(currentWindow.startDate(), today));
        long remaining = (long) currentWindow.totalDays() - elapsedInPeriod;
        return (int) Math.max(0, remaining);
    }

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

    private List<UserCreditDetailVO.UserCreditOrderVO> buildUserCreditOrderVOList(List<CreditOrder> creditOrders,
                                                                                CreditUser creditUser,
                                                                                long nowEpochSecond) {
        if (creditOrders == null || creditOrders.isEmpty()) {
            return List.of();
        }
        int settlementDays = SettlementTypeEnum.getDaysByCode(creditUser.getSettlementType());
        LocalDate baseStartDate = getBaseStartDate(creditUser);

        List<Integer> orderIds = creditOrders.stream().map(CreditOrder::getOrderId).filter(Objects::nonNull).toList();
        OrderService orderService = applicationContext.getBean(OrderService.class);

        // 复用 /api/user/order/list 相同的订单商品组装逻辑（OrderServiceImpl#getOrderItemList）
        List<OrderItemVO> orderItemVOList = orderIds.isEmpty() ? List.of() : orderService.getOrderItemList(orderIds);
        Map<Integer, List<OrderItemVO>> orderItemsMap = orderItemVOList.stream()
                .collect(Collectors.groupingBy(OrderItemVO::getOrderId));

        List<UserCreditDetailVO.UserCreditOrderVO> result = new ArrayList<>();
        for (CreditOrder co : creditOrders) {
            UserCreditDetailVO.UserCreditOrderVO item = new UserCreditDetailVO.UserCreditOrderVO();
            item.setOrderId(co.getOrderId());
            item.setOrderSn(co.getOrderSn());
            item.setCreditAmount(co.getCreditAmount());
            item.setOrderTime(co.getOrderTime() == null ? "" : TigUtils.handelTime(co.getOrderTime()));

            item.setSettlementStatus(co.getSettlementStatus());
            item.setSettlementStatusText(CreditOrderSettlementStatusEnum.getDescByCode(co.getSettlementStatus()));

            long orderTime = co.getOrderTime() == null ? 0L : co.getOrderTime();
            boolean overdue = false;
            if (orderTime > 0 && !CreditOrderSettlementStatusEnum.SETTLED.getCode().equals(co.getSettlementStatus())) {
                LocalDate orderDate = Instant.ofEpochSecond(orderTime)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                LocalDate nowDate = Instant.ofEpochSecond(nowEpochSecond)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                if (orderDate.isBefore(baseStartDate)) {
                    overdue = true;
                } else {
                    long daysFromBase = Math.max(0, ChronoUnit.DAYS.between(baseStartDate, orderDate));
                    long periodIndex = daysFromBase / settlementDays;
                    LocalDate orderPeriodEnd = baseStartDate.plusDays((periodIndex + 1) * settlementDays - 1L);
                    overdue = nowDate.isAfter(orderPeriodEnd);
                }
            }
            item.setRiskStatus(overdue ? 1 : 0);
            item.setRiskStatusText(overdue ? "逾期" : "正常");

            List<OrderItemVO> orderItems = orderItemsMap.getOrDefault(co.getOrderId(), List.of());
            int totalQuantity = orderItems.stream()
                    .map(oi -> oi.getQuantity() == null ? 0 : oi.getQuantity())
                    .reduce(0, Integer::sum);
            item.setTotalQuantity(totalQuantity);

            // items 返回与 /api/user/order/list 的 OrderVO.items 完全一致的结构（OrderItemVO 全字段）
            item.setItems(orderItems);

            result.add(item);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPeriodStartIfNoPending(Integer userId) {
        // 若没有任何待结算/退款中的账期订单：
        // - 已有周期起点：保持不变（滚动周期持续）
        // - 无周期起点：初始化为当天0点
        Long cnt = creditOrderMapper.selectCount(new LambdaQueryWrapper<CreditOrder>()
                .eq(CreditOrder::getUserId, userId)
                .in(CreditOrder::getSettlementStatus,
                        CreditOrderSettlementStatusEnum.UNSETTLED.getCode(),
                        CreditOrderSettlementStatusEnum.REFUNDING.getCode()));
        if (cnt != null && cnt == 0L) {
            CreditUser creditUser = baseMapper.selectByUserId(userId);
            if (creditUser == null) {
                return;
            }
            if (creditUser.getPeriodStartTime() != null && creditUser.getPeriodStartTime() > 0) {
                return;
            }
            long startOfDayEpochSecond = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            this.lambdaUpdate()
                    .set(CreditUser::getPeriodStartTime, startOfDayEpochSecond)
                    .set(CreditUser::getUpdateTime, StringUtils.getCurrentTime())
                    .eq(CreditUser::getUserId, userId)
                    .update();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAllPeriodStartTime() {
        // 查询所有启用的授信会员
        List<CreditUser> creditUsers = this.lambdaQuery()
                .eq(CreditUser::getStatus, CreditUserStatusEnum.ENABLED.getCode())
                .isNotNull(CreditUser::getPeriodStartTime)
                .list();

        if (creditUsers == null || creditUsers.isEmpty()) {
            return;
        }

        long nowEpochSecond = System.currentTimeMillis() / 1000;
        LocalDate today = LocalDate.now();
        long todayStartEpochSecond = today.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

        for (CreditUser creditUser : creditUsers) {
            try {
                // 获取结算周期天数
                int settlementDays = SettlementTypeEnum.getDaysByCode(creditUser.getSettlementType());
                if (settlementDays <= 0) {
                    continue;
                }

                Long periodStartTime = creditUser.getPeriodStartTime();
                if (periodStartTime == null || periodStartTime <= 0) {
                    continue;
                }

                // 计算周期起点日期
                LocalDate periodStartDate = Instant.ofEpochSecond(periodStartTime)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                // 计算从周期起点到今天经过了多少天
                long daysPassed = ChronoUnit.DAYS.between(periodStartDate, today);

                // 如果还在第一个周期内，不需要更新
                if (daysPassed < settlementDays) {
                    continue;
                }

                // 计算当前应该在第几个周期（从0开始）
                long currentPeriodIndex = daysPassed / settlementDays;

                // 计算新的周期起点：原起点 + (周期索引 * 周期天数)
                LocalDate newPeriodStartDate = periodStartDate.plusDays(currentPeriodIndex * settlementDays);
                long newPeriodStartEpochSecond = newPeriodStartDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

                // 如果新周期起点与当前不同，则更新
                if (newPeriodStartEpochSecond != periodStartTime) {
                    this.lambdaUpdate()
                            .set(CreditUser::getPeriodStartTime, newPeriodStartEpochSecond)
                            .set(CreditUser::getUpdateTime, StringUtils.getCurrentTime())
                            .eq(CreditUser::getCreditUserId, creditUser.getCreditUserId())
                            .update();
                }
            } catch (Exception e) {
                // 单个会员更新失败不影响其他会员
            }
        }
    }
}
