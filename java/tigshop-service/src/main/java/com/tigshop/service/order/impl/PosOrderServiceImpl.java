// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.order.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.tigshop.bean.dto.order.OrderConfigDTO;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.enums.order.*;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.enums.shop.ShopType;
import com.tigshop.bean.model.finance.Paylog;
import com.tigshop.bean.model.o2o.UserPickupInfo;
import com.tigshop.bean.model.order.*;
import com.tigshop.bean.model.product.Comment;
import com.tigshop.bean.model.shop.OrderConfig;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.vendor.Vendor;
import com.tigshop.bean.query.order.OrderListPageQuery;
import com.tigshop.bean.vo.finance.OrderInvoiceVO;
import com.tigshop.bean.vo.order.OrderActionVO;
import com.tigshop.bean.vo.order.OrderItemVO;
import com.tigshop.bean.vo.order.OrderReservationVO;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.bean.vo.setting.config.OrderSettingsVO;
import com.tigshop.bean.vo.user.UserAddressVO;
import com.tigshop.bean.vo.user.UserBaseVO;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.*;
import com.tigshop.mapper.finance.PaylogMapper;
import com.tigshop.mapper.o2o.UserPickupInfoMapper;
import com.tigshop.mapper.order.AftersalesMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.product.CommentMapper;
import com.tigshop.mapper.shop.OrderConfigMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.mapper.vendor.VendorMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.order.*;
import com.tigshop.service.setting.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tigshop.bean.enums.o2o.ShipmentStatusEnum.NOT_SHIPPED;
import static com.tigshop.bean.enums.o2o.ShipmentStatusEnum.SHIPPED;
import static com.tigshop.bean.enums.order.OrderStatusEnum.PENDING;
import static com.tigshop.common.constant.Constants.DATE_FORMAT;

/**
 * 订单管理服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class PosOrderServiceImpl extends BaseServiceImpl<OrderMapper, Order> implements PosOrderService {

    private final UserMapper userMapper;
    private final ShopMapper shopMapper;
    private final ConfigService configService;
    private final CommentMapper commentMapper;
    private final AftersalesMapper aftersalesMapper;
    private final OrderConfigMapper orderConfigMapper;
    private final PaylogMapper paylogMapper;
    private final AftersalesService aftersalesService;
    private final TigshopProperties tigshopProperties;
    private final VendorMapper vendorMapper;
    private final OrderReservationService orderReservationService;
    private final OrderItemService orderItemService;
    private final AftersalesItemService aftersalesItemService;
    private final OrderAftersalesService orderAfterSalesService;
    private final UserPickupInfoMapper userPickupInfoMapper;

    @Override
    public Page<OrderVO> list(OrderListPageQuery pageQuery) {
        Page<Order> page = buildSortOrder(pageQuery);

        // 构造查询构造器
        // LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        MPJLambdaWrapper<Order> queryWrapper = new MPJLambdaWrapper<>();
        buildQueryWrapper(queryWrapper, pageQuery);

        // 待晒单
        if (pageQuery.getIsShowed() != null && pageQuery.getIsShowed() > -1) {
            List<Comment> comments = commentMapper.selectList(
                    Wrappers.lambdaQuery(Comment.class).eq(Comment::getUserId, pageQuery.getUserId()).eq(Comment::getIsShowed, pageQuery.getIsShowed())
            );
            // 取出评论的orderId
            List<Integer> orderIds = comments.stream().map(Comment::getOrderId).toList();
            if (orderIds.isEmpty()) {
                return new Page<>();
            }
            queryWrapper.in(Order::getOrderId, orderIds);
        }
        // 如果查询参数包含了支付方式，那么联表筛选出已支付的相关支付方式的订单
        if (StrUtil.isNotBlank(pageQuery.getPayCode())) {
            queryWrapper.leftJoin(Paylog.class, Paylog::getOrderId, Order::getOrderId)
                    // 商品订单：orderType=0，避免关联到撞号的充值单支付记录
                    .eq(Paylog::getOrderType, 0)
                    .eq(Paylog::getPayCode, pageQuery.getPayCode())
                    .eq(Paylog::getPayStatus, PayStatusEnum.PAID.getCode())
                    .groupBy(Order::getOrderId);
        }
        // 执行查询
        Page<Order> orderPage = this.page(page, queryWrapper);

        // 获取查询结果
        List<Order> orderPageRecords = orderPage.getRecords();
        if (orderPageRecords.isEmpty()) {
            return new Page<>();
        }

        List<Integer> userIds = orderPageRecords.stream().map(Order::getUserId).distinct().toList();
        Map<Integer, User> userMap = new HashMap<>();
        if (CollUtil.isNotEmpty(userIds)) {
            List<User> userList = userMapper.selectBatchIds(userIds);
            userMap = userList.stream().collect(Collectors.toMap(User::getUserId, Function.identity()));
        }

        // 调用方法获取订单信息
        List<Integer> orderIds = orderPageRecords.stream().map(Order::getOrderId).toList();
        List<OrderItemVO> itemListNew = getOrderItemList(orderIds);

        // 插入售后数据
        Map<Integer, List<OrderItemVO>> itemMap = itemListNew.stream().collect(Collectors.groupingBy(OrderItemVO::getOrderId));
        // 取出店铺Id
        List<Integer> shopIds = orderPageRecords.stream().map(Order::getShopId).distinct().toList();
        List<Shop> shopList = shopMapper.selectBatchIds(shopIds);
        Map<Integer, Shop> shopMap = shopList.stream().collect(Collectors.toMap(Shop::getShopId, Function.identity()));

        // 查询店铺结算配置
        List<OrderConfig> orderConfigs = orderConfigMapper.selectList(
                new LambdaQueryWrapper<OrderConfig>()
                        .in(OrderConfig::getShopId, shopIds)
        );
        Map<Integer, OrderConfig> orderConfigMap = orderConfigs.stream().collect(Collectors.toMap(OrderConfig::getShopId, Function.identity()));

        // 获取订单配置
        OrderSettingsVO orderSettings = configService.getSettings(OrderSettingsVO.class);

        // 转换为VO
        Map<Integer, User> finalUserMap = userMap;
        String afterSalesLimitDays = configService.getConfigVal(SettingsEnum.AFTER_SALES_LIMIT_DAYS);
        String autoDeliveryDays = configService.getConfigVal(SettingsEnum.AUTO_DELIVERY_DAYS);
        List<OrderVO> orderVOList = orderPageRecords.stream()
                .map(order -> {
                    OrderVO orderVO = new OrderVO();
                    BeanUtils.copyProperties(order, orderVO);
                    orderVO.setRegionIds(JsonUtil.jsonToList(order.getRegionIds(), Integer.class));
                    orderVO.setRegionNames(JsonUtil.jsonToList(order.getRegionNames(), String.class));

                    Integer orderStatus = order.getOrderStatus();
                    boolean isCycleMainOrder = false;
                    if (OrderType.isReservationOrder(order.getOrderType()) && !isCycleMainOrder) {
                        orderVO.setOrderStatusName(OrderReservationStatusEnum.getStatusName(order.getOrderStatus()));
                    } else if (Objects.equals(order.getShippingTypeId(), OrderDeliveryTypeEnum.SAME_CITY.getCode())) {
                        orderVO.setOrderStatusName(OrderSameCityEnum.getStatusName(order.getOrderStatus()));
                    } else {
                        orderVO.setOrderStatusName(OrderStatusEnum.getStatusName(order.getOrderStatus()));
                    }

                    orderVO.setPayStatusName(PaymentStatus.getStatusName(order.getPayStatus()));
                    orderVO.setShippingStatusName(ShippingStatusEnum.getStatusName(order.getShippingStatus()));
                    orderVO.setInvoiceData(JSON.parseObject(order.getInvoiceData(), OrderInvoiceVO.class));
                    if (orderVO.getRegionNames() != null) {
                        orderVO.setUserAddress(String.join(" ", orderVO.getRegionNames()) + " " + order.getAddress());
                    }
                    UserAddressVO addressData = JsonUtil.fromJson(order.getAddressData(), UserAddressVO.class);
                    orderVO.setAddressData(addressData);

                    DateTime addDate = DateUtil.date(order.getAddTime() * 1000);
                    orderVO.setAddTime(DateUtil.format(addDate, DATE_FORMAT));

                    if (order.getPayTime() > 0) {
                        DateTime payDate = DateUtil.date(order.getPayTime() * 1000);
                        orderVO.setPayTime(DateUtil.format(payDate, DATE_FORMAT));
                    }

                    if (order.getShippingTime() > 0) {
                        DateTime shippingDate = DateUtil.date(order.getShippingTime() * 1000);
                        orderVO.setShippingTime(DateUtil.format(shippingDate, DATE_FORMAT));
                    }

                    if (order.getReceivedTime() > 0) {
                        DateTime receivedDate = DateUtil.date(order.getReceivedTime() * 1000);
                        orderVO.setReceivedTime(DateUtil.format(receivedDate, DATE_FORMAT));
                    }

                    orderVO.setItems(itemMap.get(order.getOrderId()));
                    UserBaseVO userBaseVO = new UserBaseVO();
                    User user = finalUserMap.get(order.getUserId());
                    if (user != null) {
                        BeanUtils.copyProperties(user, userBaseVO);
                    }
                    orderVO.setUser(userBaseVO);
                    orderVO.setAvailableActions(availableActions(orderVO));

                    if (order.getShopId() != null && order.getShopId() > 0) {
                        OrderVO.ShopVO shopVO = new OrderVO.ShopVO();
                        Shop shop = shopMap.get(order.getShopId());
                        if (shop != null) {
                            BeanUtils.copyProperties(shop, shopVO);
                            shopVO.setStatusText(ShopType.getTypeName(shop.getStatus()));
                            orderVO.setShop(shopVO);
                            orderVO.setShopName(shopVO.getShopTitle());
                            orderVO.setShopId(shopVO.getShopId());
                        }
                    }

                    // 判断是否可以申请售后；1. 判断店铺结算配置（确认收货后立即不可售后）（自营店不用判断）2. 全局设置（买家申请售后限制）
                    boolean toAftersales = orderVO.getAvailableActions().getToAftersales();
                    orderVO.setToAftersalses(toAftersales);
                    if (toAftersales) {
                        // 1. 判断店铺结算配置（确认收货后立即不可售后）（自营店不用判断）
                        if (order.getShopId() != null && order.getShopId() > 0) {
                            OrderConfig orderConfig = orderConfigMap.get(order.getShopId());
                            if (orderConfig != null) {
                                OrderConfigDTO orderConfigData = JsonUtil.fromJson(orderConfig.getData(), OrderConfigDTO.class);
                                if (OrderStatusEnum.COMPLETED.getCode() == order.getOrderStatus() && 0 == orderConfigData.getDateType()) {
                                    orderVO.setToAftersalses(false);
                                }
                            }
                        }
                        // 2. 全局设置（买家申请售后限制）
                        if (OrderStatusEnum.COMPLETED.getCode() == order.getOrderStatus()) {
                            if (StrUtil.isNotBlank(afterSalesLimitDays)) {
                                long receivedTime = order.getReceivedTime() * 1000;
                                BigDecimal afterSalesLimitDaysTime = new BigDecimal(afterSalesLimitDays).multiply(new BigDecimal(24 * 60 * 60 * 1000));
                                LocalDateTime limitDateTime = Instant.ofEpochMilli(receivedTime + afterSalesLimitDaysTime.longValue()).atZone(ZoneId.systemDefault()).toLocalDateTime();
                                boolean toAftersalses = limitDateTime.isAfter(LocalDateTime.now());
                                orderVO.setToAftersalses(toAftersalses);
                            }
                        }
                    }
                    orderVO.getAvailableActions().setToAftersales(orderVO.getToAftersalses());

                    // 自动发货天数
                    Long aftersalesCount = aftersalesMapper.selectCount(
                            Wrappers.lambdaQuery(Aftersales.class)
                                    .eq(Aftersales::getOrderId, order.getOrderId())
                                    .eq(Aftersales::getStatus, AftersalesStatusEnum.IN_REVIEW.getCode())
                    );
                    if (aftersalesCount == 0 && OrderStatusEnum.PROCESSING.getCode() == order.getOrderStatus() && StrUtil.isNotBlank(autoDeliveryDays)) {
                        long shippingTimeMillis = order.getShippingTime() * 1000;
                        long elapsedMillis = Instant.now().toEpochMilli() - shippingTimeMillis;

                        long elapsedDays = TimeUnit.MILLISECONDS.toDays(elapsedMillis);

                        BigDecimal result = new BigDecimal(autoDeliveryDays).subtract(new BigDecimal(elapsedDays)).setScale(0, RoundingMode.UP);

                        orderVO.setAutoDeliveryDays(result);
                    }

                    orderVO.assembleOrderSettings(orderSettings);

                    // 支付信息
                    Paylog paylog = paylogMapper.selectOne(Wrappers.lambdaQuery(Paylog.class)
                            .eq(Paylog::getPayStatus, PayStatusEnum.PAID.getCode())
                            .eq(Paylog::getOrderId, order.getOrderId())
                            // 商品订单：orderType=0，避免撞充值单号
                            .eq(Paylog::getOrderType, 0)
                            .select(Paylog::getPaySn, Paylog::getPayCode, Paylog::getTransactionId)
                    );
                    orderVO.setPayLog(paylog);

                    orderVO.setVendorName(Optional.ofNullable(vendorMapper.selectOnboardedById(order.getVendorId())).map(Vendor::getVendorName).orElse(""));
                    // 补充预约相关信息
                    if (OrderType.isReservationOrder(order.getOrderType())) {
                        OrderReservation one = orderReservationService.getOne(new LambdaQueryWrapper<OrderReservation>().eq(OrderReservation::getOrderId, order.getOrderId()).last("LIMIT 1"));
                        if (one != null) {
                            orderVO.setOrderReservation(OrderReservationVO.toOrderReservationVO(one));
                        }
                    }
                    // 订单是否在售后中：已完成但平台财务退款未到账的售后仍算售后中
                    List<Aftersales> posAftersalesList = aftersalesService.list(
                            new LambdaQueryWrapper<Aftersales>()
                                    .eq(Aftersales::getOrderId, order.getOrderId())
                                    .select(Aftersales::getAftersaleId, Aftersales::getStatus));
                    List<Integer> posTerminalStatus = AftersalesStatusEnum.terminalStatus();
                    boolean posAfterSalesIng = posAftersalesList.stream()
                            .anyMatch(a -> a.getStatus() != null && !posTerminalStatus.contains(a.getStatus()))
                            || !aftersalesService.findPendingFinanceRefundIds(posAftersalesList).isEmpty();
                    orderVO.setAfterSalesIng(posAfterSalesIng);
                    return orderVO;
                }).toList();
        return PageUtil.convertPage(orderPage, orderVOList);
    }

    // 构建查询构造器
    public void buildQueryWrapper(MPJLambdaWrapper<Order> queryWrapper, OrderListPageQuery pageQuery) {
        // 关键词检索 收货人 + 订单号 + 订单id
        String keyword = pageQuery.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            queryWrapper.leftJoin(User.class, User::getUserId, Order::getUserId);
            queryWrapper.and(wrapper -> wrapper
                    .like(User::getUsername, keyword)
                    .or()
                    .like(User::getMobile, keyword)
            );
        }

        queryWrapper.and(wrapper -> wrapper
                .eq(Order::getOrderType, OrderType.RESERVATION_HOME.getCode())
                .or()
                .eq(Order::getOrderType, OrderType.RESERVATION_STORE.getCode())
        );

        //订单状态
        if (pageQuery.getOrderStatus() != null && ObjectUtil.isNotEmpty(pageQuery.getOrderStatus())) {
            String[] split = pageQuery.getOrderStatus().split(",");
            if (split.length == 1 && Integer.parseInt(split[0]) >= 0) {
                queryWrapper.eq(Order::getOrderStatus, split[0]);
            }
            if (split.length > 1) {
                queryWrapper.in(Order::getOrderStatus, List.of(split));
            }
        }
        if (pageQuery.getOrderStatus() != null && ObjectUtil.equals(pageQuery.getOrderStatus(), "-2")) {
            // 查询删除的订单
            queryWrapper.eq(Order::getIsDel, DeleteType.DELETE.getCode());
        } else {
            queryWrapper.eq(Order::getIsDel, DeleteType.NOT_DELETE.getCode());
        }
        if (pageQuery.getOrderIds() != null && !pageQuery.getOrderIds().isEmpty()) {
            queryWrapper.in(Order::getOrderId, pageQuery.getOrderIds());
        }
        // 店铺检索
        if (getShopId() != null && getShopId() > 0) {
            {
                queryWrapper.eq(Order::getShopId, getShopId());
            }
        } else {
            if (pageQuery.getShopId() != null && pageQuery.getShopId() > 0) {
                queryWrapper.eq(Order::getShopId, pageQuery.getShopId());
            }
        }
        try {
            if (AdminTypeEnum.fromCode(getAdminType()).equals(AdminTypeEnum.POS)) {
                queryWrapper.eq(Order::getShopId, getShopId());
            }
        } catch (Exception ignored) {

        }
        //供应商筛选
        if (getVendorId() != null && getVendorId() > 0) {
            queryWrapper.eq(Order::getVendorId, getVendorId());
        } else {
            if (pageQuery.getVendorId() != null && pageQuery.getVendorId() > 0) {
                queryWrapper.eq(Order::getVendorId, pageQuery.getVendorId());
            }
            if (pageQuery.getVendorId() != null && pageQuery.getVendorId() == -2) {
                //查询供应商ID大于0的
                queryWrapper.gt(Order::getVendorId, 0);
            }
        }

        // 是否结算检索
        if (pageQuery.getIsSettlement() != null && pageQuery.getIsSettlement() > -1) {
            queryWrapper.eq(Order::getIsSettlement, pageQuery.getIsSettlement());
        }
        // 是否积分订单
        if (pageQuery.getIsExchangeOrder() != null && pageQuery.getIsExchangeOrder() > -1) {
            queryWrapper.eq(Order::getIsExchangeOrder, pageQuery.getIsExchangeOrder());
        }
        // 支付状态
        if (pageQuery.getPayStatus() != null && pageQuery.getPayStatus() != -1) {
            queryWrapper.eq(Order::getPayStatus, pageQuery.getPayStatus());
        }
        // 发货状态
        if (pageQuery.getShippingStatus() != null && pageQuery.getShippingStatus() != -1) {
            queryWrapper.eq(Order::getShippingStatus, pageQuery.getShippingStatus());
        }
        // 评价状态
        if (pageQuery.getCommentStatus() != null && pageQuery.getCommentStatus() != -1) {
            if (pageQuery.getCommentStatus() > 0) {
                queryWrapper.eq(Order::getCommentStatus, pageQuery.getCommentStatus());
            } else {
                queryWrapper.eq(Order::getCommentStatus, 0).eq(Order::getOrderStatus, OrderStatusEnum.COMPLETED.getCode());
            }
        }
        // 详情地址
        if (StrUtil.isNotEmpty(pageQuery.getAddress())) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Order::getAddress, pageQuery.getAddress())
                    .or()
                    .like(Order::getRegionNames, pageQuery.getAddress()));
        }
        // 收货人的email
        if (StrUtil.isNotEmpty(pageQuery.getEmail())) {
            queryWrapper.like(Order::getEmail, pageQuery.getEmail());
        }
        //手机号
        if (StrUtil.isNotEmpty(pageQuery.getMobile())) {
            queryWrapper.like(Order::getMobile, pageQuery.getMobile());
        }
        //配送物流
        if (pageQuery.getLogisticsId() != null && pageQuery.getLogisticsId() > 0) {
            queryWrapper.eq(Order::getLogisticsId, pageQuery.getLogisticsId());
        }
        if (StrUtil.isNotEmpty(pageQuery.getReservationStartTime()) || StrUtil.isNotEmpty(pageQuery.getReservationEndTime())) {
            queryWrapper.leftJoin(OrderReservation.class, OrderReservation::getOrderId, Order::getOrderId);
        }
        if (StrUtil.isNotEmpty(pageQuery.getReservationStartTime())) {
            queryWrapper.ge(OrderReservation::getReservationDate, pageQuery.getReservationStartTime());
        }
        if (StrUtil.isNotEmpty(pageQuery.getReservationEndTime())) {
            queryWrapper.le(OrderReservation::getReservationDate, pageQuery.getReservationEndTime());
        }
        //支付时间
        if (StrUtil.isNotBlank(pageQuery.getPayTime())) {
            //逗号隔开分割时间
            String[] payTime = pageQuery.getPayTime().split(",");
            Long payTimeStart = DateUtil.parse(payTime[0]).getTime();
            Long payTimeEnd = DateUtil.parse(payTime[1]).getTime() + 86400;
            queryWrapper.between(Order::getPayTime, payTimeStart, payTimeEnd);
        }

        //用户ID
        if (pageQuery.getUserId() != null) {
            queryWrapper.eq(Order::getUserId, pageQuery.getUserId());
        }
        // PC 端 日期检索
        if (pageQuery.getDataType() != null) {
            Long[] startAndEndTime = TigUtils.getStartAndEndTime(pageQuery.getDataType());
            queryWrapper.between(Order::getAddTime, startAndEndTime[0], startAndEndTime[1]);
        }

        if (pageQuery.getMark() != null && pageQuery.getMark() != -1) {
            queryWrapper.eq(Order::getMark, pageQuery.getMark());
        }
        //订单来源in筛选，先解析string按逗号隔开为数组
        if (StrUtil.isNotBlank(pageQuery.getOrderSource())) {
            List<String> split = StrUtil.split(pageQuery.getOrderSource(), ",");
            queryWrapper.in(Order::getOrderSource, split);
        }
    }


    public List<OrderItemVO> getOrderItemList(List<Integer> orderIds) {
        List<OrderItemVO> itemList = orderItemService.getItemByOrderIds(orderIds);
        return itemList.stream().map(item -> {
            OrderItemVO orderItemVO = new OrderItemVO();
            BeanUtils.copyProperties(item, orderItemVO);
            LambdaQueryWrapper<AftersalesItem> queryWrapperSales = new LambdaQueryWrapper<>();
            queryWrapperSales.eq(AftersalesItem::getOrderItemId, item.getItemId());
            queryWrapperSales.orderByDesc(AftersalesItem::getAftersalesItemId);
            queryWrapperSales.last("limit 1");
            AftersalesItem aftersalesItem = aftersalesItemService.getOne(queryWrapperSales);
            if (aftersalesItem != null) {
                orderItemVO.setAftersalesItem(aftersalesItem);
            }
            return orderItemVO;
        }).toList();
    }

    public OrderActionVO availableActions(OrderVO orderVO) {
        OrderActionVO orderActionVO = new OrderActionVO();
        Integer orderId = orderVO.getOrderId();
        OrderStatusEnum orderStatusEnum = OrderStatusEnum.getStatus(orderVO.getOrderStatus());
        orderVO.setToAftersalses(false);
        Boolean isPickup = orderVO.getIsPickup();
        switch (orderStatusEnum) {
            case PENDING:
                // 待确认，待付款
                orderActionVO.setSetConfirm(true);
                orderActionVO.setCancelOrder(true);
                orderActionVO.setModifyOrder(true);
                orderActionVO.setModifyShippingInfo(true);
                orderActionVO.setModifyOrderMoney(true);
                orderActionVO.setSetPaid(true);
                orderActionVO.setModifyOrderConsignee(true);
                // 假设 adminUid 是一个可以通过某种方式获取的变量
                if (SecurityUtils.isAdminUser() && orderVO.getPayTypeId() != PayType.OFFLINE.getCode()) {
                    // 后台用户不允许操作非线下支付订单
                    orderActionVO.setSetPaid(false);
                }
                orderActionVO.setToPay(true);
                break;
            case CONFIRMED:
                // 已确认，待发货
                orderActionVO.setSplitOrder(true);
                orderActionVO.setDeliver(true);
                orderActionVO.setModifyOrderConsignee(true);
                if (orderVO.getPayStatus() != PaymentStatus.PAID.getCode()) {
                    orderActionVO.setSetPaid(true);
                    orderActionVO.setCancelOrder(true);
                }
                orderActionVO.setToAftersales(orderAfterSalesService.getOrderAfterSalesToAction(orderId));
                break;
            case PROCESSING:
                // 已发货，处理中
                if(!Boolean.TRUE.equals(isPickup)){
                    orderActionVO.setConfirmReceipt(true);
                }
                orderActionVO.setModifyShippingInfo(true);
                orderActionVO.setToAftersales(orderAfterSalesService.getOrderAfterSalesToAction(orderId));
                break;
            case COMPLETED:
                orderActionVO.setRebuy(true);
                orderActionVO.setToAftersales(orderAfterSalesService.getOrderAfterSalesToAction(orderId));
                // D1146：评价功能关闭时不下发待评价入口，避免订单列表 / 详情残留按钮。
                // 这里直读配置而非注入 CommentService，避免订单与评价服务间的构造器循环依赖。
                if (orderVO.getCommentStatus() == PENDING.getCode()
                        && !"0".equals(configService.getConfigVal(SettingsEnum.PRODUCT_COMMENT_ENABLED))) {
                    orderActionVO.setToComment(true);
                }
                break;
            case CANCELLED:
                orderActionVO.setRebuy(true);
                orderActionVO.setDelOrder(true);
                break;
            case INVALID:
                orderActionVO.setDelOrder(true);
                break;
            // 其他状态...
            default:
                break;
        }
        PaymentStatus paymentStatus = PaymentStatus.getStatus(orderVO.getPayStatus());
        switch (paymentStatus) {
            case UNPAID:
                break;
            case PAID:
                orderActionVO.setSetUnpaid(false);
                break;
            default:
                break;
        }
        if (orderVO.getIsStoreSplited() != null && orderVO.getIsStoreSplited() == 1) {
            orderActionVO.setSplitOrder(false);
        } else {
            orderActionVO.setDeliver(false);
        }
        if (orderVO.getIsDel() != null && orderVO.getIsDel() == 1) {
            orderActionVO.setDelOrder(false);
        }
        if (orderVO.getOrderType() != null && orderVO.getOrderType() > 1) {
            orderActionVO.setRebuy(false);
            if (OrderType.getNoAfterSalesType(orderVO.getOrderType())) {
                orderActionVO.setToAftersales(false);
            }
        }
        return orderActionVO;
    }

}
