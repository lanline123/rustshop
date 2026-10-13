package com.tigshop.service.finance.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.finance.CreditOrderListDTO;
import com.tigshop.bean.enums.finance.CreditOrderSettlementStatusEnum;
import com.tigshop.bean.enums.order.OrderStatusEnum;
import com.tigshop.bean.enums.order.AftersalesStatusEnum;
import com.tigshop.bean.enums.order.AftersalesTypeEnum;
import com.tigshop.bean.enums.order.ShippingStatusEnum;
import com.tigshop.bean.model.finance.CreditOrder;
import com.tigshop.bean.model.finance.RefundApply;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.model.order.Aftersales;
import com.tigshop.bean.model.order.AftersalesItem;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.user.UserCompany;
import com.tigshop.bean.vo.finance.CreditOrderVO;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.finance.CreditOrderMapper;
import com.tigshop.mapper.finance.RefundApplyMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.order.OrderItemMapper;
import com.tigshop.mapper.order.AftersalesMapper;
import com.tigshop.mapper.order.AftersalesItemMapper;
import com.tigshop.mapper.user.UserCompanyMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.CreditOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 账期订单服务实现类
 *
 * @author Tigshop团队
 * @create 2026-01-14
 */
@RequiredArgsConstructor
@Service
public class CreditOrderServiceImpl extends BaseServiceImpl<CreditOrderMapper, CreditOrder> implements CreditOrderService {

    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserCompanyMapper userCompanyMapper;
    private final AftersalesMapper aftersalesMapper;
    private final AftersalesItemMapper aftersalesItemMapper;
    private final RefundApplyMapper refundApplyMapper;

    @Override
    public Page<CreditOrderVO> list(CreditOrderListDTO listDTO) {
        Page<CreditOrder> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        LambdaQueryWrapper<CreditOrder> queryWrapper = new LambdaQueryWrapper<>();

        // 排序
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 订单号搜索
        if (StrUtil.isNotEmpty(listDTO.getOrderSn())) {
            queryWrapper.like(CreditOrder::getOrderSn, listDTO.getOrderSn());
        }

        // 会员ID筛选
        if (listDTO.getUserId() != null) {
            queryWrapper.eq(CreditOrder::getUserId, listDTO.getUserId());
        }

        // 用户名或手机号搜索
        if (StrUtil.isNotEmpty(listDTO.getUsername()) || StrUtil.isNotEmpty(listDTO.getMobile())) {
            LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
            if (StrUtil.isNotEmpty(listDTO.getUsername())) {
                userQueryWrapper.like(User::getUsername, listDTO.getUsername());
            }
            if (StrUtil.isNotEmpty(listDTO.getMobile())) {
                userQueryWrapper.like(User::getMobile, listDTO.getMobile());
            }
            List<User> userList = userMapper.selectList(userQueryWrapper);
            if (userList == null || userList.isEmpty()) {
                return PageUtil.convertPage(page, List.of());
            }
            List<Integer> userIds = userList.stream().map(User::getUserId).toList();
            queryWrapper.in(CreditOrder::getUserId, userIds);
        }

        // 订单状态筛选（需关联order表查询）
        if (listDTO.getOrderStatus() != null) {
            LambdaQueryWrapper<Order> orderQueryWrapper = new LambdaQueryWrapper<>();
            orderQueryWrapper.eq(Order::getOrderStatus, listDTO.getOrderStatus());
            List<Order> orderList = orderMapper.selectList(orderQueryWrapper);
            if (orderList == null || orderList.isEmpty()) {
                return PageUtil.convertPage(page, List.of());
            }
            List<Integer> orderIds = orderList.stream().map(Order::getOrderId).toList();
            queryWrapper.in(CreditOrder::getOrderId, orderIds);
        }

        // 结算状态筛选
        if (listDTO.getSettlementStatusList() != null && !listDTO.getSettlementStatusList().isEmpty()) {
            queryWrapper.in(CreditOrder::getSettlementStatus, listDTO.getSettlementStatusList());
        } else if (listDTO.getSettlementStatus() != null) {
            queryWrapper.eq(CreditOrder::getSettlementStatus, listDTO.getSettlementStatus());
        }

        // 回款流水单号搜索
        if (StrUtil.isNotEmpty(listDTO.getSettlementSn())) {
            queryWrapper.like(CreditOrder::getSettlementSn, listDTO.getSettlementSn());
        }

        // 结算记录ID筛选
        if (listDTO.getSettlementId() != null) {
            queryWrapper.eq(CreditOrder::getSettlementId, listDTO.getSettlementId());
        }

        // 下单时间范围（格式：yyyy-MM-dd）
        if (StrUtil.isNotEmpty(listDTO.getOrderTimeStart())) {
            LocalDateTime startOfDay = LocalDate.parse(listDTO.getOrderTimeStart()).atStartOfDay();
            Long orderTimeStart = startOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            queryWrapper.ge(CreditOrder::getOrderTime, orderTimeStart / 1000);
        }
        if (StrUtil.isNotEmpty(listDTO.getOrderTimeEnd())) {
            LocalDateTime endOfDay = LocalDate.parse(listDTO.getOrderTimeEnd()).atTime(LocalTime.MAX);
            Long orderTimeEnd = endOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            queryWrapper.le(CreditOrder::getOrderTime, orderTimeEnd / 1000);
        }

        queryWrapper.orderByDesc(CreditOrder::getOrderTime);
        Page<CreditOrder> creditOrderPage = this.page(page, queryWrapper);

        List<Integer> pageOrderIds = creditOrderPage.getRecords().stream()
                .map(CreditOrder::getOrderId)
                .filter(Objects::nonNull)
                .toList();
        Map<Integer, BigDecimal> refundedAmountMap = getRefundedAmountMap(pageOrderIds);

        List<CreditOrderVO> voList = creditOrderPage.getRecords().stream().map(co -> {
            CreditOrderVO vo = convertToVO(co);
            vo.setRefundedAmount(refundedAmountMap.getOrDefault(co.getOrderId(), BigDecimal.ZERO));
            return vo;
        }).toList();

        return PageUtil.convertPage(creditOrderPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCreditOrder(Integer orderId, String orderSn, Integer userId, Integer creditUserId, BigDecimal creditAmount) {
        CreditOrder creditOrder = new CreditOrder();
        creditOrder.setOrderId(orderId);
        creditOrder.setOrderSn(orderSn);
        creditOrder.setUserId(userId);
        creditOrder.setCreditUserId(creditUserId);
        creditOrder.setCreditAmount(creditAmount);
        creditOrder.setSettlementStatus(CreditOrderSettlementStatusEnum.UNSETTLED.getCode());
        creditOrder.setOrderTime(StringUtils.getCurrentTime());
        creditOrder.setCreateTime(StringUtils.getCurrentTime());
        creditOrder.setUpdateTime(StringUtils.getCurrentTime());
        this.save(creditOrder);
    }

    @Override
    public List<CreditOrderVO> getPendingOrders(Integer userId) {
        LambdaQueryWrapper<CreditOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CreditOrder::getUserId, userId);
        queryWrapper.eq(CreditOrder::getSettlementStatus, CreditOrderSettlementStatusEnum.UNSETTLED.getCode());
        queryWrapper.orderByDesc(CreditOrder::getOrderTime);
        List<CreditOrder> creditOrders = this.list(queryWrapper);
        if (creditOrders == null || creditOrders.isEmpty()) {
            return List.of();
        }

        List<Integer> orderIds = creditOrders.stream().map(CreditOrder::getOrderId).filter(Objects::nonNull).toList();
        Map<Integer, BigDecimal> refundedAmountMap = getRefundedAmountMap(orderIds);

        return creditOrders.stream().map(co -> {
            CreditOrderVO vo = convertToVO(co);
            BigDecimal refundedAmount = refundedAmountMap.getOrDefault(co.getOrderId(), BigDecimal.ZERO);
            vo.setRefundedAmount(refundedAmount);
            return vo;
        }).toList();
    }

    @Override
    public CreditOrder getByOrderId(Integer orderId) {
        return baseMapper.selectByOrderId(orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSettlementStatus(List<Integer> orderIds, Integer settlementId, String settlementSn) {
        LambdaUpdateWrapper<CreditOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(CreditOrder::getOrderId, orderIds);
        updateWrapper.set(CreditOrder::getSettlementStatus, CreditOrderSettlementStatusEnum.SETTLED.getCode());
        updateWrapper.set(CreditOrder::getSettlementId, settlementId);
        updateWrapper.set(CreditOrder::getSettlementSn, settlementSn);
        updateWrapper.set(CreditOrder::getSettlementTime, StringUtils.getCurrentTime());
        updateWrapper.set(CreditOrder::getUpdateTime, StringUtils.getCurrentTime());
        this.update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelSettlementStatus(Integer settlementId) {
        LambdaUpdateWrapper<CreditOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CreditOrder::getSettlementId, settlementId);
        updateWrapper.set(CreditOrder::getSettlementStatus, CreditOrderSettlementStatusEnum.UNSETTLED.getCode());
        updateWrapper.set(CreditOrder::getUpdateTime, StringUtils.getCurrentTime());
        this.update(updateWrapper);
    }

    @Override
    public Object[] countPendingOrders(Integer userId) {
        List<CreditOrderVO> pendingOrders = getPendingOrders(userId);
        int count = pendingOrders.size();
        BigDecimal amount = pendingOrders.stream()
                .map(vo -> vo.getCreditAmount() == null ? BigDecimal.ZERO : vo.getCreditAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Object[]{count, amount};
    }

    /**
     * 获取订单已退款金额映射（仅统计财务审核通过的退款申请）
     * 退款金额从关联的售后记录获取，并考虑运费退还逻辑
     * 参考 RefundApplyServiceImpl.calculateCreditRefundAmountWithShipping 方法
     */
    private Map<Integer, BigDecimal> getRefundedAmountMap(List<Integer> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyMap();
        }
        // 查询财务审核通过（refundStatus=2）的退款申请
        List<RefundApply> approvedRefundApplies = refundApplyMapper.selectList(new LambdaQueryWrapper<RefundApply>()
                .in(RefundApply::getOrderId, orderIds)
                .eq(RefundApply::getRefundStatus, 2) // 已处理（财务审核通过）
        );
        if (approvedRefundApplies == null || approvedRefundApplies.isEmpty()) {
            return Collections.emptyMap();
        }
        // 获取关联的售后ID
        List<Integer> aftersaleIds = approvedRefundApplies.stream()
                .map(RefundApply::getAftersaleId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (aftersaleIds.isEmpty()) {
            return Collections.emptyMap();
        }
        // 查询售后记录获取退款金额（同时支持"仅退款"和"退货/退款"）
        List<Aftersales> aftersalesList = aftersalesMapper.selectList(new LambdaQueryWrapper<Aftersales>()
                .in(Aftersales::getAftersaleId, aftersaleIds)
                .in(Aftersales::getAftersaleType,
                        AftersalesTypeEnum.PAYRETURN.getCode(),
                        AftersalesTypeEnum.RETURN.getCode())
        );
        if (aftersalesList == null || aftersalesList.isEmpty()) {
            return Collections.emptyMap();
        }

        // 查询订单信息（用于判断运费退还条件）
        List<Order> orders = orderMapper.selectBatchIds(orderIds);
        Map<Integer, Order> orderMap = orders.stream()
                .collect(Collectors.toMap(Order::getOrderId, o -> o));

        // 查询订单项（用于判断是否全部商品都售后了）
        List<OrderItem> orderItems = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .in(OrderItem::getOrderId, orderIds));
        Map<Integer, List<OrderItem>> orderItemsMap = orderItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));

        // 查询所有已完成售后的售后项（用于判断是否全部商品都售后了）
        List<Aftersales> allCompletedAftersales = aftersalesMapper.selectList(new LambdaQueryWrapper<Aftersales>()
                .in(Aftersales::getOrderId, orderIds)
                .eq(Aftersales::getStatus, AftersalesStatusEnum.COMPLETE.getCode()));
        List<Integer> completedAftersaleIds = allCompletedAftersales.stream()
                .map(Aftersales::getAftersaleId)
                .toList();

        Map<Integer, List<AftersalesItem>> aftersalesItemsByOrderIdMap = new HashMap<>();
        if (!completedAftersaleIds.isEmpty()) {
            List<AftersalesItem> allAftersalesItems = aftersalesItemMapper.selectList(new LambdaQueryWrapper<AftersalesItem>()
                    .in(AftersalesItem::getAftersaleId, completedAftersaleIds));
            // 通过 orderItemId 关联到 orderId
            for (AftersalesItem item : allAftersalesItems) {
                Integer orderId = orderItems.stream()
                        .filter(oi -> oi.getItemId().equals(item.getOrderItemId()))
                        .findFirst()
                        .map(OrderItem::getOrderId)
                        .orElse(null);
                if (orderId != null) {
                    aftersalesItemsByOrderIdMap.computeIfAbsent(orderId, k -> new java.util.ArrayList<>()).add(item);
                }
            }
        }

        // 构建售后ID到退款金额和售后类型的映射
        Map<Integer, Aftersales> aftersalesMap = aftersalesList.stream()
                .collect(Collectors.toMap(Aftersales::getAftersaleId, a -> a, (a, b) -> a));

        // 按订单分组处理
        Map<Integer, List<RefundApply>> refundAppliesByOrder = approvedRefundApplies.stream()
                .filter(r -> r.getOrderId() != null && r.getAftersaleId() != null)
                .filter(r -> aftersalesMap.containsKey(r.getAftersaleId()))
                .collect(Collectors.groupingBy(RefundApply::getOrderId));

        Map<Integer, BigDecimal> result = new HashMap<>();

        for (Map.Entry<Integer, List<RefundApply>> entry : refundAppliesByOrder.entrySet()) {
            Integer orderId = entry.getKey();
            List<RefundApply> orderRefundApplies = entry.getValue();

            Order order = orderMap.get(orderId);
            if (order == null) continue;

            BigDecimal totalRefund = BigDecimal.ZERO;
            boolean shippingFeeRefunded = false; // 运费是否已退还

            for (RefundApply refundApply : orderRefundApplies) {
                Aftersales aftersales = aftersalesMap.get(refundApply.getAftersaleId());
                if (aftersales == null || aftersales.getRefundAmount() == null) continue;

                BigDecimal baseRefund = aftersales.getRefundAmount();
                totalRefund = totalRefund.add(baseRefund);

                // 判断是否需要加运费（每个订单只加一次）
                if (!shippingFeeRefunded) {
                    BigDecimal shippingFee = order.getShippingFee();
                    if (shippingFee != null && shippingFee.compareTo(BigDecimal.ZERO) > 0) {
                        // 计算是否应该退运费
                        if (shouldRefundShippingFee(aftersales, order, orderItemsMap.get(orderId),
                                aftersalesItemsByOrderIdMap.getOrDefault(orderId, Collections.emptyList()))) {
                            totalRefund = totalRefund.add(shippingFee);
                            shippingFeeRefunded = true;
                        }
                    }
                }
            }

            result.put(orderId, totalRefund);
        }

        return result;
    }

    /**
     * 判断是否应该退运费
     * 参考 RefundApplyServiceImpl.checkAudit 和 calculateCreditRefundAmountWithShipping 方法
     */
    private boolean shouldRefundShippingFee(Aftersales aftersales, Order order,
                                            List<OrderItem> orderItems, List<AftersalesItem> completedAftersalesItems) {
        if (orderItems == null || orderItems.isEmpty()) return false;

        boolean isPending = ShippingStatusEnum.PENDING.getCode() == order.getShippingStatus();
        boolean isPayReturn = AftersalesTypeEnum.PAYRETURN.getCode() == aftersales.getAftersaleType();

        // 待发货 + 仅退款：退运费
        if (isPending && isPayReturn) {
            return true;
        }

        // 待发货 + 非仅退款：判断是否全部商品都售后了
        if (isPending) {
            return isAllItemsAftersales(orderItems, completedAftersalesItems);
        }

        // 已完成订单且已收货状态（但未发过货，如虚拟商品）
        if (OrderStatusEnum.COMPLETED.getCode() == order.getOrderStatus()
                && ShippingStatusEnum.SHIPPED.getCode() == order.getShippingStatus()) {
            if (order.getShippingTime() == null || order.getShippingTime() <= 0) {
                return isAllItemsAftersales(orderItems, completedAftersalesItems);
            }
        }

        return false;
    }

    /**
     * 判断是否全部商品都售后了
     */
    private boolean isAllItemsAftersales(List<OrderItem> orderItems, List<AftersalesItem> aftersalesItems) {
        Map<Integer, List<AftersalesItem>> aftersalesItemMap = aftersalesItems.stream()
                .collect(Collectors.groupingBy(AftersalesItem::getOrderItemId));

        return orderItems.stream().allMatch(item -> {
            List<AftersalesItem> curr = aftersalesItemMap.get(item.getItemId());
            if (curr == null || curr.isEmpty()) {
                return false;
            }
            Integer itemNum = curr.stream().map(AftersalesItem::getNumber).reduce(0, Integer::sum);
            return Objects.equals(item.getQuantity(), itemNum);
        });
    }

    /**
     * 转换为VO
     */
    private CreditOrderVO convertToVO(CreditOrder creditOrder) {
        CreditOrderVO vo = new CreditOrderVO();
        BeanUtils.copyProperties(creditOrder, vo);

        // 查询用户信息
        User user = userMapper.selectById(creditOrder.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setMobile(user.getMobile());
        }

        // 查询企业名称
        LambdaQueryWrapper<UserCompany> companyQueryWrapper = new LambdaQueryWrapper<>();
        companyQueryWrapper.eq(UserCompany::getUserId, creditOrder.getUserId());
        companyQueryWrapper.last("LIMIT 1");
        UserCompany userCompany = userCompanyMapper.selectOne(companyQueryWrapper);
        if (userCompany != null) {
            vo.setCompanyName(userCompany.getCompanyName());
        }

        // 查询订单信息
        Order order = orderMapper.selectById(creditOrder.getOrderId());
        if (order != null) {
            vo.setOrderAmount(order.getTotalAmount());
            vo.setOrderStatus(order.getOrderStatus());
            vo.setOrderStatusText(OrderStatusEnum.getStatusName(order.getOrderStatus()));
        }

        vo.setSettlementStatusText(CreditOrderSettlementStatusEnum.getDescByCode(creditOrder.getSettlementStatus()));
        vo.setOrderTime(TigUtils.handelTime(creditOrder.getOrderTime()));
        if (creditOrder.getSettlementTime() != null) {
            vo.setSettlementTime(TigUtils.handelTime(creditOrder.getSettlementTime()));
        }

        return vo;
    }
}
