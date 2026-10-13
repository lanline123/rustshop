package com.tigshop.service.deposit.impl;

import org.springframework.security.access.prepost.PreAuthorize;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.deposit.DepositRefundApplyDTO;
import com.tigshop.bean.dto.deposit.DepositRefundAuditDTO;
import com.tigshop.bean.dto.deposit.DepositRefundListDTO;
import com.tigshop.bean.enums.deposit.DepositLogType;
import com.tigshop.bean.enums.deposit.DepositOwnerType;
import com.tigshop.bean.enums.deposit.DepositRefundStatus;
import com.tigshop.bean.enums.deposit.DepositRefundType;
import com.tigshop.bean.enums.order.AftersalesStatusEnum;
import com.tigshop.bean.enums.order.OrderStatusEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.deposit.ShopDepositRefund;
import com.tigshop.bean.model.merchant.Merchant;
import com.tigshop.bean.model.order.Aftersales;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.vendor.Vendor;
import com.tigshop.bean.vo.deposit.DepositFullRefundCheckVO;
import com.tigshop.bean.vo.deposit.DepositRefundVO;
import com.tigshop.bean.vo.deposit.DepositSettingVO;
import com.tigshop.common.constant.HttpStatus;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.deposit.ShopDepositRefundMapper;
import com.tigshop.mapper.merchant.MerchantMapper;
import com.tigshop.mapper.order.AftersalesMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.mapper.vendor.VendorMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.deposit.DepositAccountService;
import com.tigshop.service.deposit.DepositRefundService;
import com.tigshop.service.deposit.DepositSettingService;
import com.tigshop.service.setting.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 店铺保证金退还/提现服务实现。
 *
 * @author Tigshop团队
 */
@Service
@RequiredArgsConstructor
public class DepositRefundServiceImpl
        extends BaseServiceImpl<ShopDepositRefundMapper, ShopDepositRefund>
        implements DepositRefundService {

    /** 末单收货冷静期天数配置键（复用 ConfigService 键值机制） */
    private static final String CONFIG_REFUND_COOLING_DAYS = "deposit_refund_cooling_days";

    /**
     * 末单收货冷静期默认天数。
     *
     * <p>【假设】CR/计划未明确具体天数，配置缺失或非法时取 7 天作为合理默认；
     * 平台可通过配置键 {@link #CONFIG_REFUND_COOLING_DAYS} 覆盖。</p>
     */
    private static final long DEFAULT_REFUND_COOLING_DAYS = 7L;

    /** 一天的秒数 */
    private static final long SECONDS_PER_DAY = 86400L;

    /**
     * "进行中订单"口径：待支付(0)、待发货(1)、已发货/待收货(2)；
     * 已取消(3)/无效(4)/已完成(5) 不算进行中。
     */
    private static final List<Integer> ONGOING_ORDER_STATUS = List.of(
            OrderStatusEnum.PENDING.getCode(),
            OrderStatusEnum.CONFIRMED.getCode(),
            OrderStatusEnum.PROCESSING.getCode());

    private final DepositAccountService depositAccountService;
    private final ShopMapper shopMapper;
    private final VendorMapper vendorMapper;
    private final MerchantMapper merchantMapper;
    private final OrderMapper orderMapper;
    private final AftersalesMapper aftersalesMapper;
    private final ConfigService configService;
    private final DepositSettingService depositSettingService;

    @Override
    public Integer apply(Integer shopId, DepositRefundApplyDTO applyDTO) {
        return applyInternal(DepositOwnerType.SHOP.getCode(), shopId, applyDTO);
    }

    @Override
    public Integer vendorApply(Integer vendorId, DepositRefundApplyDTO applyDTO) {
        return applyInternal(DepositOwnerType.SUPPLIER.getCode(), vendorId, applyDTO);
    }

    @Override
    public DepositFullRefundCheckVO fullRefundCheck(Integer shopId) {
        return fullRefundCheckInternal(DepositOwnerType.SHOP.getCode(), shopId);
    }

    @Override
    public DepositFullRefundCheckVO vendorFullRefundCheck(Integer vendorId) {
        return fullRefundCheckInternal(DepositOwnerType.SUPPLIER.getCode(), vendorId);
    }

    /**
     * 全额退还预检统一内部入口（只读、不抛异常）。
     *
     * <p>按全额退还 5 条件逐条评估并收集未满足原因，与申请校验
     * {@link #validateFullRefundPreconditions} 同口径，差异点：
     * 条件 4「末单确认收货满 N 天」的 N 取售后限制天数 {@code afterSalesLimitDays}，
     * 而非申请校验使用的 {@code deposit_refund_cooling_days}。</p>
     *
     * @param ownerType 持有者类型 1店铺 2供应商
     * @param ownerId   持有者id（店铺为shopId，供应商为vendorId）
     */
    private DepositFullRefundCheckVO fullRefundCheckInternal(Integer ownerType, Integer ownerId) {
        DepositFullRefundCheckVO vo = new DepositFullRefundCheckVO();
        long limitDays = resolveAfterSalesLimitDays();
        vo.setAfterSalesLimitDays((int) limitDays);

        if (ownerId == null || ownerId <= 0) {
            vo.setCanRefund(false);
            vo.setRefundableAmount(BigDecimal.ZERO);
            vo.setUnmetReasons(List.of("持有者不存在"));
            return vo;
        }

        // 保证金功能关闭：无条件放行全额退还；可退额度仍扣减待审核退还（与部分退还口径一致）
        if (!isDepositEnabled()) {
            vo.setCanRefund(true);
            vo.setRefundableAmount(effectiveRefundable(ownerType, ownerId));
            vo.setUnmetReasons(Collections.emptyList());
            return vo;
        }

        boolean isVendor = DepositOwnerType.SUPPLIER.getCode().equals(ownerType);
        List<String> reasons = new ArrayList<>();

        // 条件 1：处于停业/关店状态
        if (isVendor) {
            Vendor vendor = vendorMapper.selectOnboardedById(ownerId);
            if (vendor == null || !Integer.valueOf(2).equals(vendor.getStatus())) {
                reasons.add("供应商未停业关闭");
            }
        } else {
            Shop shop = shopMapper.selectById(ownerId);
            boolean closed = shop != null
                    && (Integer.valueOf(4).equals(shop.getStatus())
                    || Integer.valueOf(10).equals(shop.getStatus()));
            if (!closed) {
                reasons.add("店铺未停业");
            }
        }

        // 条件 2：无进行中订单
        long ongoingOrderCount = orderMapper.selectCount(
                Wrappers.<Order>lambdaQuery()
                        .eq(isVendor, Order::getVendorId, ownerId)
                        .eq(!isVendor, Order::getShopId, ownerId)
                        .eq(Order::getIsDel, 0)
                        .in(Order::getOrderStatus, ONGOING_ORDER_STATUS));
        if (ongoingOrderCount > 0) {
            reasons.add("存在进行中订单");
        }

        // 条件 3：无未完结售后
        long ongoingAftersalesCount = aftersalesMapper.selectCount(
                Wrappers.<Aftersales>lambdaQuery()
                        .eq(isVendor, Aftersales::getVendorId, ownerId)
                        .eq(!isVendor, Aftersales::getShopId, ownerId)
                        .notIn(Aftersales::getStatus,
                                AftersalesStatusEnum.COMPLETE.getCode(),
                                AftersalesStatusEnum.CANCEL.getCode()));
        if (ongoingAftersalesCount > 0) {
            reasons.add("存在未完结售后");
        }

        // 条件 4：末单确认收货满 N 天（N=售后限制天数；无已收货订单或 N=0 视为已满足）
        if (limitDays > 0) {
            Order lastReceivedOrder = orderMapper.selectOne(
                    Wrappers.<Order>lambdaQuery()
                            .eq(isVendor, Order::getVendorId, ownerId)
                            .eq(!isVendor, Order::getShopId, ownerId)
                            .eq(Order::getIsDel, 0)
                            .gt(Order::getReceivedTime, 0)
                            .orderByDesc(Order::getReceivedTime)
                            .last("limit 1"));
            if (lastReceivedOrder != null
                    && lastReceivedOrder.getReceivedTime() != null
                    && lastReceivedOrder.getReceivedTime() > 0) {
                long deadline = lastReceivedOrder.getReceivedTime() + limitDays * SECONDS_PER_DAY;
                if (nowSeconds() < deadline) {
                    reasons.add("末单确认收货未满 " + limitDays + " 天");
                }
            }
        }

        // 条件 5：无进行中退还单（一步式下仅含待审核）
        long ongoingRefundCount = this.count(
                Wrappers.<ShopDepositRefund>lambdaQuery()
                        .eq(ShopDepositRefund::getOwnerType, ownerType)
                        .eq(ShopDepositRefund::getOwnerId, ownerId)
                        .eq(ShopDepositRefund::getStatus,
                                DepositRefundStatus.PENDING_REVIEW.getCode()));
        if (ongoingRefundCount > 0) {
            reasons.add("存在进行中退还单");
        }

        boolean canRefund = reasons.isEmpty();
        vo.setCanRefund(canRefund);
        // 可退时返回有效可退 = max(0, 可提现 − 待审核退还)；不可退时金额为 0
        vo.setRefundableAmount(canRefund
                ? effectiveRefundable(ownerType, ownerId)
                : BigDecimal.ZERO);
        vo.setUnmetReasons(reasons);
        return vo;
    }

    /**
     * 读取售后限制天数（{@link SettingsEnum#AFTER_SALES_LIMIT_DAYS}）：
     * 配置缺失/非法/为负时按 0 天处理（条件 4 自动满足）。
     */
    private long resolveAfterSalesLimitDays() {
        try {
            String val = configService.getConfigVal(SettingsEnum.AFTER_SALES_LIMIT_DAYS);
            if (val != null && !val.trim().isEmpty()) {
                long days = new BigDecimal(val.trim()).longValue();
                if (days > 0) {
                    return days;
                }
            }
        } catch (Exception ignored) {
            // 配置不存在或解析失败，按 0 天处理
        }
        return 0L;
    }

    /**
     * 退还/提现申请统一内部入口：店铺/供应商两端仅 owner 维度不同，逻辑一致。
     * 可退上限按 owner 维度取有效可退 = max(0, withdrawable − 待审核退还总和)。
     *
     * @param ownerType 持有者类型 1店铺 2供应商
     * @param ownerId   持有者id（店铺为shopId，供应商为vendorId）
     */
    private Integer applyInternal(Integer ownerType, Integer ownerId, DepositRefundApplyDTO applyDTO) {
        if (ownerId == null || ownerId <= 0) {
            throw new GlobalException("持有者不存在");
        }
        if (applyDTO == null || applyDTO.getAmount() == null
                || applyDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new GlobalException("保证金金额非法", HttpStatus.DEPOSIT_AMOUNT_INVALID);
        }
        if (DepositRefundType.getDescByCode(applyDTO.getType()).isEmpty()) {
            throw new GlobalException("退还类型非法", HttpStatus.DEPOSIT_AMOUNT_INVALID);
        }
        // type=2 全额退还：先按顺序校验 5 个前置条件，任一不满足即拦截；type=1 部分提现不走这 5 条件。
        // 保证金功能关闭时无条件放行全额退还（跳过 5 条件），后续可退上限校验仍防止超出实缴。
        if (DepositRefundType.FULL_REFUND.getCode().equals(applyDTO.getType()) && isDepositEnabled()) {
            validateFullRefundPreconditions(ownerType, ownerId);
        }
        // 有效可退 = max(0, 可提现 − 待审核退还)，申请超额拦截（部分/全部退还口径一致）
        BigDecimal effectiveRefundable = effectiveRefundable(ownerType, ownerId);
        if (applyDTO.getAmount().compareTo(effectiveRefundable) > 0) {
            throw new GlobalException("退还金额超过可退上限", HttpStatus.DEPOSIT_WITHDRAW_EXCEED_LIMIT);
        }
        long now = nowSeconds();
        ShopDepositRefund refund = new ShopDepositRefund();
        refund.setOwnerType(ownerType);
        refund.setOwnerId(ownerId);
        refund.setType(applyDTO.getType());
        refund.setAmount(applyDTO.getAmount());
        refund.setPayeeInfo(applyDTO.getPayeeInfo());
        refund.setReason(applyDTO.getReason());
        refund.setStatus(DepositRefundStatus.PENDING_REVIEW.getCode());
        refund.setAuditAdminId(0);
        refund.setAuditTime(0L);
        refund.setAddTime(now);
        refund.setUpdateTime(now);
        this.save(refund);
        return refund.getShopDepositRefundId();
    }

    /**
     * 全额退还（type=2）前置 5 条件校验，按顺序校验，任一不满足抛对应错误码。
     *
     * <p>owner 维度差异：店铺(SHOP)按 shopId 查 shop / order.shop_id / aftersales.shop_id；
     * 供应商(SUPPLIER)按 vendorId 查 vendor / order.vendor_id / aftersales.vendor_id。</p>
     *
     * <ol>
     *   <li>处于停业/关店状态（5407）：店铺要求 status 为 4暂停运营或 10关店；供应商要求 status=2关闭。
     *       即"经营中"（店铺 1开业 / 供应商 1开启）不允许全额退还。</li>
     *   <li>无进行中订单（5408）：owner 下无 orderStatus∈{0,1,2} 且未删除的订单。</li>
     *   <li>无未完成售后（5409）：owner 下无 status∉{6已完成,7已取消} 的售后单。</li>
     *   <li>冷静期已过（5410）：末单收货时间 + 冷静期天数 ≤ 当前时间。</li>
     *   <li>无进行中退还单（5411）：owner 下无 status=0待审核 的退还单。</li>
     * </ol>
     */
    private void validateFullRefundPreconditions(Integer ownerType, Integer ownerId) {
        boolean isVendor = DepositOwnerType.SUPPLIER.getCode().equals(ownerType);

        // 条件 1：处于停业/关店状态（5407）
        if (isVendor) {
            Vendor vendor = vendorMapper.selectOnboardedById(ownerId);
            // 供应商 status：1开启 2关闭；要求已关闭(2)
            if (vendor == null || !Integer.valueOf(2).equals(vendor.getStatus())) {
                throw new GlobalException("全额退还需供应商已停业关闭",
                        HttpStatus.DEPOSIT_REFUND_SHOP_NOT_CLOSED);
            }
        } else {
            Shop shop = shopMapper.selectById(ownerId);
            // 店铺 status：1开业 4暂停运营 10关店；要求暂停运营(4)或关店(10)
            boolean closed = shop != null
                    && (Integer.valueOf(4).equals(shop.getStatus())
                    || Integer.valueOf(10).equals(shop.getStatus()));
            if (!closed) {
                throw new GlobalException("全额退还需店铺停业",
                        HttpStatus.DEPOSIT_REFUND_SHOP_NOT_CLOSED);
            }
        }

        // 条件 2：无进行中订单（5408）
        long ongoingOrderCount = orderMapper.selectCount(
                Wrappers.<Order>lambdaQuery()
                        .eq(isVendor, Order::getVendorId, ownerId)
                        .eq(!isVendor, Order::getShopId, ownerId)
                        .eq(Order::getIsDel, 0)
                        .in(Order::getOrderStatus, ONGOING_ORDER_STATUS));
        if (ongoingOrderCount > 0) {
            throw new GlobalException("存在进行中订单",
                    HttpStatus.DEPOSIT_REFUND_HAS_ONGOING_ORDER);
        }

        // 条件 3：无未完成售后（5409）——进行中口径 = 非"已完成/已取消"
        long ongoingAftersalesCount = aftersalesMapper.selectCount(
                Wrappers.<Aftersales>lambdaQuery()
                        .eq(isVendor, Aftersales::getVendorId, ownerId)
                        .eq(!isVendor, Aftersales::getShopId, ownerId)
                        .notIn(Aftersales::getStatus,
                                AftersalesStatusEnum.COMPLETE.getCode(),
                                AftersalesStatusEnum.CANCEL.getCode()));
        if (ongoingAftersalesCount > 0) {
            throw new GlobalException("存在进行中售后",
                    HttpStatus.DEPOSIT_REFUND_HAS_ONGOING_AFTERSALE);
        }

        // 条件 4：冷静期已过（5410）——起算点 = 末单收货时间（无已收货订单则视为已过）
        Order lastReceivedOrder = orderMapper.selectOne(
                Wrappers.<Order>lambdaQuery()
                        .eq(isVendor, Order::getVendorId, ownerId)
                        .eq(!isVendor, Order::getShopId, ownerId)
                        .eq(Order::getIsDel, 0)
                        .gt(Order::getReceivedTime, 0)
                        .orderByDesc(Order::getReceivedTime)
                        .last("limit 1"));
        if (lastReceivedOrder != null
                && lastReceivedOrder.getReceivedTime() != null
                && lastReceivedOrder.getReceivedTime() > 0) {
            long deadline = lastReceivedOrder.getReceivedTime()
                    + resolveCoolingDays() * SECONDS_PER_DAY;
            if (nowSeconds() < deadline) {
                throw new GlobalException("未过末单收货冷静期",
                        HttpStatus.DEPOSIT_REFUND_COOLDOWN_NOT_REACHED);
            }
        }

        // 条件 5：无进行中退还单（5411）——一步式下进行中仅含待审核
        long ongoingRefundCount = this.count(
                Wrappers.<ShopDepositRefund>lambdaQuery()
                        .eq(ShopDepositRefund::getOwnerType, ownerType)
                        .eq(ShopDepositRefund::getOwnerId, ownerId)
                        .eq(ShopDepositRefund::getStatus,
                                DepositRefundStatus.PENDING_REVIEW.getCode()));
        if (ongoingRefundCount > 0) {
            throw new GlobalException("存在进行中退还单",
                    HttpStatus.DEPOSIT_REFUND_HAS_ONGOING_REFUND);
        }
    }

    /**
     * 读取末单收货冷静期天数：配置缺失/非法时回退默认 {@link #DEFAULT_REFUND_COOLING_DAYS}。
     */
    private long resolveCoolingDays() {
        try {
            ConfigPO configPO = configService.getConfigByCode(CONFIG_REFUND_COOLING_DAYS);
            if (configPO != null && configPO.getBizVal() != null) {
                long days = Long.parseLong(configPO.getBizVal().trim());
                if (days >= 0) {
                    return days;
                }
            }
        } catch (Exception ignored) {
            // 配置不存在或解析失败，使用默认值
        }
        return DEFAULT_REFUND_COOLING_DAYS;
    }

    @Override
    public Page<DepositRefundVO> list(DepositRefundListDTO listDTO) {
        Page<ShopDepositRefund> page = new Page<>(listDTO.getPage(), listDTO.getSize());

        // 按 owner_type 分段解析命中的 ownerId 约束集合（组织名称/主体类型跨表解析，口径同账户/明细列表）
        // 兼容原 ownerId/shopId 精确入参：与各段约束取交。
        Integer explicitOwnerId = listDTO.getOwnerId() != null ? listDTO.getOwnerId() : listDTO.getShopId();
        List<OwnerSegment> segments = new ArrayList<>();
        for (Integer ownerType : resolveOwnerTypes(listDTO.getOwnerType())) {
            Set<Integer> idSet = resolveOwnerIdConstraint(ownerType, listDTO.getOwnerName(), listDTO.getSubjectType());
            if (idSet != null && idSet.isEmpty()) {
                continue;
            }
            if (explicitOwnerId != null && explicitOwnerId > 0) {
                if (idSet == null) {
                    idSet = new HashSet<>(Collections.singletonList(explicitOwnerId));
                } else {
                    idSet = new HashSet<>(idSet);
                    idSet.retainAll(Collections.singletonList(explicitOwnerId));
                    if (idSet.isEmpty()) {
                        continue;
                    }
                }
            }
            segments.add(new OwnerSegment(ownerType, idSet));
        }
        if (segments.isEmpty()) {
            return new Page<>(listDTO.getPage(), listDTO.getSize(), 0);
        }

        LambdaQueryWrapper<ShopDepositRefund> queryWrapper = new LambdaQueryWrapper<>();
        applyOwnerSegments(queryWrapper, segments);
        queryWrapper.eq(listDTO.getType() != null,
                ShopDepositRefund::getType, listDTO.getType());
        queryWrapper.eq(listDTO.getStatus() != null,
                ShopDepositRefund::getStatus, listDTO.getStatus());
        queryWrapper.ge(listDTO.getStartTime() != null,
                ShopDepositRefund::getAddTime, listDTO.getStartTime());
        queryWrapper.le(listDTO.getEndTime() != null,
                ShopDepositRefund::getAddTime, listDTO.getEndTime());
        queryWrapper.orderByDesc(ShopDepositRefund::getShopDepositRefundId);
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        Page<ShopDepositRefund> refundPage = this.page(page, queryWrapper);
        List<DepositRefundVO> voList = refundPage.getRecords().stream()
                .map(this::toVO)
                .toList();
        return PageUtil.convertPage(refundPage, voList);
    }

    /** owner 类型范围：传了=单类型，未传=店铺+供应商。 */
    private List<Integer> resolveOwnerTypes(Integer ownerType) {
        if (ownerType == null) {
            return List.of(DepositOwnerType.SHOP.getCode(), DepositOwnerType.SUPPLIER.getCode());
        }
        return List.of(ownerType);
    }

    /**
     * 解析某一 owner 类型下，组织名称/主体类型筛选命中的 ownerId 集合。
     *
     * @return null=该类型无任何筛选约束（全量）；非空集=约束命中的 ownerId；空集=该类型被排除。
     */
    private Set<Integer> resolveOwnerIdConstraint(Integer ownerType, String ownerName, Integer subjectType) {
        boolean hasName = StrUtil.isNotBlank(ownerName);
        boolean hasSubjectType = subjectType != null;
        if (!hasName && !hasSubjectType) {
            return null;
        }
        List<Set<Integer>> constraints = new ArrayList<>();
        if (DepositOwnerType.SUPPLIER.getCode().equals(ownerType)) {
            if (hasName) {
                constraints.add(vendorIdsByName(ownerName));
            }
            if (hasSubjectType) {
                constraints.add(vendorIdsBySubjectType(subjectType));
            }
        } else {
            if (hasName) {
                constraints.add(shopIdsByName(ownerName));
            }
            if (hasSubjectType) {
                constraints.add(shopIdsByMerchantType(subjectType));
            }
        }
        return intersect(constraints);
    }

    /** 将分段 owner 约束组装为查询条件，保持单表 shop_deposit_refund 分页/排序。 */
    private void applyOwnerSegments(LambdaQueryWrapper<ShopDepositRefund> wrapper, List<OwnerSegment> segments) {
        boolean allUnbounded = segments.stream().allMatch(s -> s.idSet() == null);
        if (allUnbounded) {
            List<Integer> types = segments.stream().map(OwnerSegment::ownerType).toList();
            if (types.size() == 1) {
                wrapper.eq(ShopDepositRefund::getOwnerType, types.get(0));
            } else {
                wrapper.in(ShopDepositRefund::getOwnerType, types);
            }
            return;
        }
        if (segments.size() == 1) {
            OwnerSegment seg = segments.get(0);
            wrapper.eq(ShopDepositRefund::getOwnerType, seg.ownerType());
            if (seg.idSet() != null) {
                wrapper.in(ShopDepositRefund::getOwnerId, seg.idSet());
            }
            return;
        }
        // 多段且至少一段带 ownerId 约束：(type1 AND id IN s1) OR (type2 AND id IN s2)
        wrapper.and(outer -> {
            boolean first = true;
            for (OwnerSegment seg : segments) {
                if (first) {
                    outer.and(o -> {
                        o.eq(ShopDepositRefund::getOwnerType, seg.ownerType());
                        if (seg.idSet() != null) {
                            o.in(ShopDepositRefund::getOwnerId, seg.idSet());
                        }
                    });
                    first = false;
                } else {
                    outer.or(o -> {
                        o.eq(ShopDepositRefund::getOwnerType, seg.ownerType());
                        if (seg.idSet() != null) {
                            o.in(ShopDepositRefund::getOwnerId, seg.idSet());
                        }
                    });
                }
            }
        });
    }

    private Set<Integer> intersect(List<Set<Integer>> sets) {
        Set<Integer> result = null;
        for (Set<Integer> set : sets) {
            if (result == null) {
                result = new HashSet<>(set);
            } else {
                result.retainAll(set);
            }
            if (result.isEmpty()) {
                return Collections.emptySet();
            }
        }
        return result == null ? Collections.emptySet() : result;
    }

    /** 店铺：按店铺名模糊取 shopId 集合。 */
    private Set<Integer> shopIdsByName(String name) {
        List<Shop> shops = shopMapper.selectList(Wrappers.<Shop>lambdaQuery()
                .select(Shop::getShopId)
                .like(Shop::getShopTitle, name));
        return shops.stream().map(Shop::getShopId).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    /** 店铺：按主体类型（商户性质 1个人 2企业）取 shopId 集合。 */
    private Set<Integer> shopIdsByMerchantType(Integer subjectType) {
        List<Merchant> merchants = merchantMapper.selectList(Wrappers.<Merchant>lambdaQuery()
                .select(Merchant::getMerchantId)
                .eq(Merchant::getType, subjectType));
        Set<Integer> merchantIds = merchants.stream()
                .map(Merchant::getMerchantId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (merchantIds.isEmpty()) {
            return Collections.emptySet();
        }
        List<Shop> shops = shopMapper.selectList(Wrappers.<Shop>lambdaQuery()
                .select(Shop::getShopId)
                .in(Shop::getMerchantId, merchantIds));
        return shops.stream().map(Shop::getShopId).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    /** 供应商：按供应商名模糊取 vendorId 集合。 */
    private Set<Integer> vendorIdsByName(String name) {
        List<Vendor> vendors = vendorMapper.selectOnboardedList(Wrappers.<Vendor>lambdaQuery()
                .select(Vendor::getVendorId)
                .like(Vendor::getVendorName, name));
        return vendors.stream().map(Vendor::getVendorId).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    /** 供应商：按主体类型（供应商性质 1个人 2企业）取 vendorId 集合。 */
    private Set<Integer> vendorIdsBySubjectType(Integer subjectType) {
        List<Vendor> vendors = vendorMapper.selectOnboardedList(Wrappers.<Vendor>lambdaQuery()
                .select(Vendor::getVendorId)
                .eq(Vendor::getType, subjectType));
        return vendors.stream().map(Vendor::getVendorId).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    /** owner 分段约束：idSet=null 表示该类型无约束(全量)。 */
    private record OwnerSegment(Integer ownerType, Set<Integer> idSet) {
    }

    @Override
    public DepositRefundVO detail(Integer id) {
        if (id == null) {
            return null;
        }
        ShopDepositRefund refund = this.getById(id);
        if (refund == null) {
            throw new GlobalException("退还单不存在");
        }
        return toVO(refund);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("@pms.hasPermission('depositRefundAudit')")
    public void audit(Integer adminId, DepositRefundAuditDTO auditDTO) {
        if (auditDTO == null || auditDTO.getId() == null) {
            throw new GlobalException("退还单不存在");
        }
        ShopDepositRefund refund = this.getById(auditDTO.getId());
        if (refund == null) {
            throw new GlobalException("退还单不存在");
        }
        if (!DepositRefundStatus.PENDING_REVIEW.getCode().equals(refund.getStatus())) {
            throw new GlobalException("退还单状态非法", HttpStatus.DEPOSIT_REFUND_STATUS_INVALID);
        }
        boolean pass = Boolean.TRUE.equals(auditDTO.getPass());
        long now = nowSeconds();
        if (pass) {
            // 一步式：通过即代表已打款，打款凭证必填
            if (auditDTO.getPayVoucher() == null || auditDTO.getPayVoucher().trim().isEmpty()) {
                throw new GlobalException("打款凭证不能为空");
            }
            // 退还金额沿用申请金额，统一按提现出账(WITHDRAW)记录，不再区分全额退还(REFUND)（余额不足由 changeDeposit 拦截）
            depositAccountService.changeDeposit(refund.getOwnerType(), refund.getOwnerId(), refund.getAmount(),
                    DepositLogType.WITHDRAW, refund.getShopDepositRefundId(), 0, adminId, auditDTO.getAuditRemark());
            refund.setStatus(DepositRefundStatus.COMPLETED.getCode());
            refund.setPayVoucher(auditDTO.getPayVoucher());
        } else {
            // 驳回
            refund.setStatus(DepositRefundStatus.REJECTED.getCode());
        }
        refund.setAuditAdminId(adminId == null ? 0 : adminId);
        refund.setAuditTime(now);
        refund.setAuditRemark(auditDTO.getAuditRemark());
        refund.setUpdateTime(now);
        this.updateById(refund);
    }

    private DepositRefundVO toVO(ShopDepositRefund refund) {
        DepositRefundVO vo = new DepositRefundVO();
        BeanUtils.copyProperties(refund, vo);
        vo.setOwnerType(refund.getOwnerType());
        vo.setOwnerId(refund.getOwnerId());
        vo.setOwnerTypeText(DepositOwnerType.getDescByCode(refund.getOwnerType()));
        // 兼容字段：shopId 同 ownerId 值；shopName 仅店铺维度回填
        vo.setShopId(refund.getOwnerId());
        vo.setStatusText(DepositRefundStatus.getDescByCode(refund.getStatus()));
        vo.setTypeText(DepositRefundType.getDescByCode(refund.getType()));
        vo.setAddTimeText(TigUtils.handelTime(refund.getAddTime()));
        vo.setAuditTimeText(TigUtils.handelTime(refund.getAuditTime()));
        fillOwnerName(vo, refund.getOwnerType(), refund.getOwnerId());
        return vo;
    }

    /**
     * 按 owner_type 回填持有者名称与主体类型：
     * 店铺取 shopTitle + merchant.type，供应商取 vendorName + vendor.type。
     * 同时为店铺维度回填兼容字段 shopName。主体类型口径与账户/明细列表一致。
     */
    private void fillOwnerName(DepositRefundVO vo, Integer ownerType, Integer ownerId) {
        if (ownerId == null || ownerId <= 0) {
            return;
        }
        if (DepositOwnerType.SUPPLIER.getCode().equals(ownerType)) {
            Vendor vendor = vendorMapper.selectOnboardedById(ownerId);
            if (vendor != null) {
                vo.setOwnerName(vendor.getVendorName());
                vo.setSubjectType(vendor.getType());
                vo.setSubjectTypeText(subjectTypeText(vendor.getType()));
            }
        } else {
            Shop shop = shopMapper.selectById(ownerId);
            if (shop != null) {
                vo.setOwnerName(shop.getShopTitle());
                vo.setShopName(shop.getShopTitle());
                if (shop.getMerchantId() != null && shop.getMerchantId() > 0) {
                    Merchant merchant = merchantMapper.selectById(shop.getMerchantId());
                    if (merchant != null) {
                        vo.setSubjectType(merchant.getType());
                        vo.setSubjectTypeText(subjectTypeText(merchant.getType()));
                    }
                }
            }
        }
    }

    /** 主体类型文案：1个人 2企业。 */
    private String subjectTypeText(Integer subjectType) {
        if (subjectType == null) {
            return "";
        }
        return subjectType == 2 ? "企业" : subjectType == 1 ? "个人" : "";
    }

    @Override
    public BigDecimal pendingRefundSum(Integer ownerType, Integer ownerId) {
        if (ownerType == null || ownerId == null || ownerId <= 0) {
            return BigDecimal.ZERO;
        }
        List<ShopDepositRefund> list = this.list(
                Wrappers.<ShopDepositRefund>lambdaQuery()
                        .eq(ShopDepositRefund::getOwnerType, ownerType)
                        .eq(ShopDepositRefund::getOwnerId, ownerId)
                        .eq(ShopDepositRefund::getStatus, DepositRefundStatus.PENDING_REVIEW.getCode()));
        return list.stream()
                .map(ShopDepositRefund::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 有效可退额度 = max(0, withdrawable − 待审核退还总和)。
     * 与部分退还前端口径（overview.withdrawable − pendingWithdraw）及全额退还 refundableAmount 统一。
     */
    private BigDecimal effectiveRefundable(Integer ownerType, Integer ownerId) {
        BigDecimal withdrawable = depositAccountService.withdrawable(ownerType, ownerId);
        if (withdrawable == null) {
            withdrawable = BigDecimal.ZERO;
        }
        BigDecimal pending = pendingRefundSum(ownerType, ownerId);
        BigDecimal effective = withdrawable.subtract(pending);
        return effective.compareTo(BigDecimal.ZERO) > 0 ? effective : BigDecimal.ZERO;
    }

    /**
     * 保证金功能是否开启（deposit_enable=1）。配置缺失/为 0 视为关闭。
     */
    private boolean isDepositEnabled() {
        DepositSettingVO setting = depositSettingService.detail();
        return setting != null && setting.getDepositEnable() != null && setting.getDepositEnable() == 1;
    }

    private static long nowSeconds() {
        return System.currentTimeMillis() / 1000L;
    }
}
