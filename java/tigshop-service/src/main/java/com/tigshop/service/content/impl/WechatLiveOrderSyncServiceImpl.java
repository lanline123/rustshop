package com.tigshop.service.content.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tigshop.bean.dto.content.WechatLiveContextDTO;
import com.tigshop.bean.dto.content.WechatLiveSyncCallbackDTO;
import com.tigshop.bean.enums.order.OrderStatusEnum;
import com.tigshop.bean.enums.order.PayStatusEnum;
import com.tigshop.bean.enums.order.PaymentStatus;
import com.tigshop.bean.enums.order.ShippingStatusEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.feign.wechat.GetTokenResult;
import com.tigshop.bean.feign.wechat.UploadShippingInfoParam;
import com.tigshop.bean.feign.wechat.UploadShippingInfoResult;
import com.tigshop.bean.model.finance.Paylog;
import com.tigshop.bean.model.finance.RefundApply;
import com.tigshop.bean.model.order.Aftersales;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.model.content.WechatLiveGoods;
import com.tigshop.bean.model.content.WechatLiveRoomGoods;
import com.tigshop.bean.model.content.WechatLiveOrderRel;
import com.tigshop.bean.model.content.WechatLiveSyncLog;
import com.tigshop.bean.model.setting.LogisticsCompany;
import com.tigshop.bean.model.user.UserAuthorize;
import com.tigshop.bean.utils.WechatLiveOrderContextUtils;
import com.tigshop.bean.vo.order.OrderItemVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.feign.WechatApiClient;
import com.tigshop.service.oauth.WechatOAuthService;
import com.tigshop.mapper.finance.PaylogMapper;
import com.tigshop.mapper.order.AftersalesMapper;
import com.tigshop.mapper.order.OrderItemMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.mapper.product.ProductSkuMapper;
import com.tigshop.mapper.user.UserAuthorizeMapper;
import com.tigshop.service.content.WechatLiveGoodsService;
import com.tigshop.service.content.WechatLiveOrderRelService;
import com.tigshop.service.content.WechatLiveRoomGoodsService;
import com.tigshop.service.content.WechatLiveOrderSyncService;
import com.tigshop.service.content.WechatLiveSyncLogService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.LogisticsCompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.content.WechatLiveOrderSyncConstants.*;

/**
 * 微信直播订单同步服务实现
 *
 * @author Tigshop团队
 * @create 2026年03月08日 16:56
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatLiveOrderSyncServiceImpl implements WechatLiveOrderSyncService {

    private final WechatLiveOrderRelService wechatLiveOrderRelService;
    private final WechatLiveSyncLogService wechatLiveSyncLogService;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final AftersalesMapper aftersalesMapper;
    private final PaylogMapper paylogMapper;
    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;
    private final UserAuthorizeMapper userAuthorizeMapper;
    private final LogisticsCompanyService logisticsCompanyService;
    private final ConfigService configService;
    private final WechatApiClient wechatApiClient;
    private final WechatOAuthService wechatOAuthService;
    private final WechatLiveGoodsService wechatLiveGoodsService;
    private final WechatLiveRoomGoodsService wechatLiveRoomGoodsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindOrderRelations(Order order, List<OrderItem> orderItems) {
        if (order == null || order.getOrderId() == null || CollUtil.isEmpty(orderItems)) {
            return;
        }
        wechatLiveOrderRelService.removeByOrderId(order.getOrderId());
        List<WechatLiveOrderRel> relList = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            WechatLiveContextDTO context = WechatLiveOrderContextUtils.extractFromPromotionData(orderItem.getPromotionData());
            if (WechatLiveOrderContextUtils.isEmpty(context)) {
                continue;
            }
            WechatLiveOrderRel rel = new WechatLiveOrderRel();
            rel.setOrderId(order.getOrderId());
            rel.setOrderItemId(orderItem.getItemId());
            rel.setWechatLiveId(context.getWechatLiveId());
            rel.setRoomId(context.getRoomId());
            rel.setWechatGoodsId(context.getWechatGoodsId());
            rel.setProductId(context.getProductId());
            rel.setShareOpenId(context.getShareOpenId());
            rel.setViewerOpenId(context.getViewerOpenId());
            rel.setScene(context.getScene());
            rel.setCustomParams(context.getCustomParams());
            rel.setSubSource(ORDER_SUB_SOURCE_WECHAT_LIVE);
            rel.setSyncStatus(SYNC_STATUS_PENDING);
            rel.setShopId(order.getShopId());
            rel.setAddTime(StringUtils.getCurrentTime());
            rel.setUpdateTime(StringUtils.getCurrentTime());
            relList.add(rel);
        }
        if (!relList.isEmpty()) {
            wechatLiveOrderRelService.saveBatch(relList);
        }
    }

    @Override
    public void onOrderCreated(Order order) {
        createOutboundLog(order, EVENT_ORDER_CREATED, null, SYNC_STATUS_PENDING);
    }

    @Override
    public void onOrderPaid(Order order) {
        createOutboundLog(order, EVENT_ORDER_PAID, null, SYNC_STATUS_PENDING);
    }

    @Override
    public void onShippingSyncStart(Order order, List<OrderItemVO> deliverItems) {
        if (!hasLiveRelation(order.getOrderId())) {
            return;
        }
        String requestData = JSONUtil.toJsonStr(Map.of(
                "orderId", order.getOrderId(),
                "trackingNo", order.getTrackingNo(),
                "deliverItems", deliverItems
        ));
        createOutboundLog(order, EVENT_ORDER_SHIPPED, requestData, SYNC_STATUS_PENDING);
        updateRelationStatus(order.getOrderId(), SYNC_STATUS_PENDING);
    }

    @Override
    public void onShippingSyncSuccess(Order order, List<OrderItemVO> deliverItems, String requestData, String responseData) {
        if (!hasLiveRelation(order.getOrderId())) {
            return;
        }
        WechatLiveSyncLog logRecord = getLatestOutboundLog(order.getOrderId(), EVENT_ORDER_SHIPPED);
        if (logRecord == null) {
            logRecord = createOutboundLog(order, EVENT_ORDER_SHIPPED, requestData, SYNC_STATUS_PENDING);
            if (logRecord == null) {
                return;
            }
        }
        logRecord.setSyncStatus(SYNC_STATUS_SUCCESS);
        logRecord.setRequestData(requestData);
        logRecord.setResponseData(responseData);
        logRecord.setLastSyncTime(StringUtils.getCurrentTime());
        logRecord.setUpdateTime(StringUtils.getCurrentTime());
        wechatLiveSyncLogService.updateById(logRecord);
        updateRelationStatus(order.getOrderId(), SYNC_STATUS_SUCCESS);
    }

    @Override
    public void onShippingSyncFailed(Order order, String errorMessage, String requestData) {
        if (!hasLiveRelation(order.getOrderId())) {
            return;
        }
        WechatLiveSyncLog logRecord = getLatestOutboundLog(order.getOrderId(), EVENT_ORDER_SHIPPED);
        if (logRecord == null) {
            logRecord = createOutboundLog(order, EVENT_ORDER_SHIPPED, requestData, SYNC_STATUS_FAILED);
            if (logRecord == null) {
                return;
            }
        }
        logRecord.setSyncStatus(SYNC_STATUS_FAILED);
        logRecord.setRequestData(requestData);
        logRecord.setErrorMessage(errorMessage);
        logRecord.setRetryCount(Optional.ofNullable(logRecord.getRetryCount()).orElse(0) + 1);
        logRecord.setLastSyncTime(StringUtils.getCurrentTime());
        logRecord.setNextRetryTime(StringUtils.getCurrentTime() + 300);
        logRecord.setUpdateTime(StringUtils.getCurrentTime());
        wechatLiveSyncLogService.updateById(logRecord);
        updateRelationStatus(order.getOrderId(), SYNC_STATUS_FAILED);
    }

    @Override
    public void onAftersaleChanged(Aftersales aftersales, String eventType) {
        if (aftersales == null || aftersales.getOrderId() == null || !hasLiveRelation(aftersales.getOrderId())) {
            return;
        }
        Order order = orderMapper.selectById(aftersales.getOrderId());
        WechatLiveSyncLog logRecord = createOutboundLog(order, eventType, JSONUtil.toJsonStr(aftersales), SYNC_STATUS_PENDING);
        if (logRecord != null) {
            logRecord.setAftersaleId(aftersales.getAftersaleId());
            wechatLiveSyncLogService.updateById(logRecord);
        }
    }

    @Override
    public void onRefundChanged(RefundApply refundApply, String eventType) {
        if (refundApply == null || refundApply.getOrderId() == null || !hasLiveRelation(refundApply.getOrderId())) {
            return;
        }
        Order order = orderMapper.selectById(refundApply.getOrderId());
        WechatLiveSyncLog logRecord = createOutboundLog(order, eventType, JSONUtil.toJsonStr(refundApply), SYNC_STATUS_PENDING);
        if (logRecord != null) {
            logRecord.setAftersaleId(refundApply.getAftersaleId());
            logRecord.setRefundId(refundApply.getRefundId());
            wechatLiveSyncLogService.updateById(logRecord);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleInboundCallback(WechatLiveSyncCallbackDTO callbackDTO) {
        Integer orderId = resolveOrderId(callbackDTO);
        if (orderId == null && shouldAutoCreateOrder(callbackDTO)) {
            orderId = createOrderFromWechatCallback(callbackDTO);
        }
        if (orderId == null && callbackDTO.getOrderSn() != null) {
            Order order = orderMapper.selectOne(Wrappers.lambdaQuery(Order.class)
                    .eq(Order::getOrderSn, callbackDTO.getOrderSn())
                    .last("limit 1"));
            orderId = order != null ? order.getOrderId() : null;
        }

        Integer roomId = callbackDTO.getRoomId();
        if (roomId == null && orderId != null) {
            roomId = wechatLiveOrderRelService.listByOrderId(orderId).stream()
                    .map(WechatLiveOrderRel::getRoomId)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }

        WechatLiveSyncLog logRecord = new WechatLiveSyncLog();
        logRecord.setOrderId(orderId);
        logRecord.setAftersaleId(callbackDTO.getAftersaleId());
        logRecord.setRefundId(callbackDTO.getRefundId());
        logRecord.setRoomId(roomId);
        logRecord.setEventType(callbackDTO.getEventType());
        logRecord.setDirection(DIRECTION_INBOUND);
        logRecord.setSyncStatus(SYNC_STATUS_SUCCESS);
        logRecord.setIdempotentKey(callbackDTO.getIdempotentKey());
        logRecord.setRequestData(JSONUtil.toJsonStr(callbackDTO));
        logRecord.setResponseData(JSONUtil.toJsonStr(Map.of("message", "callback accepted")));
        logRecord.setAddTime(StringUtils.getCurrentTime());
        logRecord.setUpdateTime(StringUtils.getCurrentTime());
        logRecord.setLastSyncTime(StringUtils.getCurrentTime());
        wechatLiveSyncLogService.save(logRecord);

        if (orderId != null) {
            updateRelationStatus(orderId, SYNC_STATUS_PARTIAL);
            Order order = orderMapper.selectById(orderId);
            if (order != null) {
                JSONObject callbackSnapshot = new JSONObject();
                callbackSnapshot.put("eventType", callbackDTO.getEventType());
                callbackSnapshot.put("status", callbackDTO.getStatus());
                callbackSnapshot.put("message", callbackDTO.getMessage());
                callbackSnapshot.put("payload", callbackDTO.getPayload());
                Order updateOrder = new Order();
                updateOrder.setOrderId(orderId);
                updateOrder.setOrderExtension(WechatLiveOrderContextUtils.applyLastCallback(order.getOrderExtension(), callbackSnapshot));
                orderMapper.updateById(updateOrder);
            }
        }
    }

    private Integer resolveOrderId(WechatLiveSyncCallbackDTO callbackDTO) {
        Integer orderId = callbackDTO.getOrderId();
        if (orderId != null) {
            return orderId;
        }
        if (StringUtils.isBlank(callbackDTO.getOrderSn())) {
            return null;
        }
        Order order = orderMapper.selectOne(Wrappers.lambdaQuery(Order.class)
                .eq(Order::getOrderSn, callbackDTO.getOrderSn())
                .last("limit 1"));
        return order != null ? order.getOrderId() : null;
    }

    private boolean shouldAutoCreateOrder(WechatLiveSyncCallbackDTO callbackDTO) {
        Map<String, Object> payload = Optional.ofNullable(callbackDTO.getPayload()).orElse(Collections.emptyMap());
        if (payload.isEmpty()) {
            return false;
        }
        if (!extractOrderItemPayload(payload).isEmpty()) {
            return true;
        }
        return toInt(payload.get("productId")) != null;
    }

    private Integer createOrderFromWechatCallback(WechatLiveSyncCallbackDTO callbackDTO) {
        if (StringUtils.isNotBlank(callbackDTO.getOrderSn())) {
            Order exists = orderMapper.selectOne(Wrappers.lambdaQuery(Order.class)
                    .eq(Order::getOrderSn, callbackDTO.getOrderSn())
                    .last("limit 1"));
            if (exists != null) {
                return exists.getOrderId();
            }
        }
        Map<String, Object> payload = Optional.ofNullable(callbackDTO.getPayload()).orElse(Collections.emptyMap());
        String viewerOpenId = firstNonBlank(
                asString(payload.get("viewerOpenId")),
                asString(payload.get("openId")),
                asString(payload.get("userOpenId"))
        );
        if (StringUtils.isBlank(viewerOpenId)) {
            throw new GlobalException("微信回调缺少用户openId，无法自动建单");
        }
        UserAuthorize userAuthorize = userAuthorizeMapper.selectOne(Wrappers.lambdaQuery(UserAuthorize.class)
                .eq(UserAuthorize::getOpenId, viewerOpenId)
                .last("limit 1"));
        if (userAuthorize == null || userAuthorize.getUserId() == null) {
            throw new GlobalException("未找到openId对应用户，无法自动建单");
        }

        Integer userId = userAuthorize.getUserId();
        Integer roomId = firstNonNull(callbackDTO.getRoomId(), toInt(payload.get("roomId")));
        Long now = StringUtils.getCurrentTime();
        List<Map<String, Object>> itemPayloads = extractOrderItemPayload(payload);
        if (itemPayloads.isEmpty()) {
            throw new GlobalException("微信回调缺少商品明细，无法自动建单");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        List<WechatLiveContextDTO> contexts = new ArrayList<>();
        Integer shopId = 0;
        BigDecimal productAmount = BigDecimal.ZERO;
        for (Map<String, Object> itemPayload : itemPayloads) {
            OrderItem orderItem = buildOrderItemFromPayload(itemPayload, payload, callbackDTO, userId, roomId);
            orderItems.add(orderItem);
            shopId = orderItem.getShopId() != null ? orderItem.getShopId() : shopId;
            productAmount = productAmount.add(orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
            contexts.add(WechatLiveOrderContextUtils.extractFromPromotionData(orderItem.getPromotionData()));
        }

        BigDecimal shippingFee = parseAmount(payload.get("shippingFee"), BigDecimal.ZERO);
        BigDecimal totalAmount = productAmount.add(shippingFee);
        boolean paid = isPaidCallback(callbackDTO, payload);
        String localOrderSn = getOrCreateLocalOrderSn(callbackDTO.getOrderSn());

        Order order = new Order();
        order.setOrderSn(localOrderSn);
        order.setUserId(userId);
        order.setShopId(shopId);
        order.setOrderType(1);
        order.setOrderStatus(paid ? OrderStatusEnum.CONFIRMED.getCode() : OrderStatusEnum.PENDING.getCode());
        order.setShippingStatus(ShippingStatusEnum.PENDING.getCode());
        order.setPayStatus(paid ? PaymentStatus.PAID.getCode() : PaymentStatus.UNPAID.getCode());
        order.setAddTime(now);
        order.setPayTime(paid ? now : null);
        order.setPayTypeId(1);
        order.setShippingMethod(1);
        order.setShippingTypeId(1);
        order.setShippingTypeName("物流配送");
        order.setLogisticsId(0);
        order.setLogisticsName("待发货");
        order.setConsignee(firstNonBlank(asString(payload.get("consignee")), asString(payload.get("receiverName")), ""));
        order.setMobile(firstNonBlank(asString(payload.get("mobile")), asString(payload.get("receiverMobile")), ""));
        order.setAddress(firstNonBlank(asString(payload.get("address")), asString(payload.get("receiverAddress")), ""));
        order.setRegionIds(asString(payload.get("regionIds")));
        order.setRegionNames(asString(payload.get("regionNames")));
        order.setAddressData(JSONUtil.toJsonStr(Map.of(
                "consignee", order.getConsignee(),
                "mobile", order.getMobile(),
                "address", order.getAddress()
        )));
        order.setBuyerNote(firstNonBlank(asString(payload.get("buyerNote")), "微信直播自动建单"));
        order.setOrderSource("miniProgram");
        order.setIsStoreSplited(0);
        order.setIsDel(0);
        order.setCommentStatus(0);
        order.setIsNeedCommisson(0);
        order.setDistributionStatus(0);
        order.setProductAmount(productAmount);
        order.setShippingFee(shippingFee);
        order.setTotalAmount(totalAmount);
        order.setCouponAmount(BigDecimal.ZERO);
        order.setPointsAmount(BigDecimal.ZERO);
        order.setGiftCardAmount(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setServiceFee(BigDecimal.ZERO);
        order.setInvoiceFee(BigDecimal.ZERO);
        order.setBalance(BigDecimal.ZERO);
        order.setOfflinePaidAmount(BigDecimal.ZERO);
        order.setOnlinePaidAmount(paid ? totalAmount : BigDecimal.ZERO);
        order.setPaidAmount(paid ? totalAmount : BigDecimal.ZERO);
        order.setUnpaidAmount(paid ? BigDecimal.ZERO : totalAmount);
        order.setUnrefundAmount(totalAmount);
        order.setUsePoints(0);
        order.setInvoiceData("");

        String extension = WechatLiveOrderContextUtils.enrichOrderExtension("{}", contexts);
        JSONObject extensionJson = JSONObject.parseObject(extension);
        extensionJson.put("wechatLiveInbound", true);
        extensionJson.put("wechatLiveInboundEventType", callbackDTO.getEventType());
        extensionJson.put("wechatOrderSn", callbackDTO.getOrderSn());
        extensionJson.put("wechatLiveInboundPayload", payload);
        order.setOrderExtension(extensionJson.toJSONString());

        orderMapper.insert(order);

        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(order.getOrderId());
            orderItem.setOrderSn(order.getOrderSn());
            orderItemMapper.insert(orderItem);
        }
        bindOrderRelations(order, orderItems);
        onOrderCreated(order);

        if (paid) {
            createPaidPaylog(order, callbackDTO, payload, totalAmount, now);
            onOrderPaid(order);
        }
        return order.getOrderId();
    }

    private OrderItem buildOrderItemFromPayload(Map<String, Object> itemPayload,
                                                Map<String, Object> payload,
                                                WechatLiveSyncCallbackDTO callbackDTO,
                                                Integer userId,
                                                Integer roomId) {
        Integer productId = toInt(itemPayload.get("productId"));
        Integer wechatGoodsId = toInt(itemPayload.get("wechatGoodsId"));
        Integer resolvedRoomId = firstNonNull(roomId, toInt(itemPayload.get("roomId")));
        WechatLiveGoods wechatLiveGoods = null;
        if (resolvedRoomId != null && wechatGoodsId != null) {
            WechatLiveRoomGoods roomGoods = wechatLiveRoomGoodsService.getGoodsByRoomIdAndWechatGoodsId(resolvedRoomId, wechatGoodsId);
            if (roomGoods != null && roomGoods.getWechatLiveGoodsId() != null) {
                wechatLiveGoods = wechatLiveGoodsService.getById(roomGoods.getWechatLiveGoodsId());
            }
            if (productId == null && wechatLiveGoods != null && wechatLiveGoods.getProductId() != null) {
                productId = wechatLiveGoods.getProductId().intValue();
            }
        }
        if (productId == null) {
            throw new GlobalException("微信回调商品未匹配到系统商品");
        }

        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new GlobalException("商品不存在，productId=" + productId);
        }
        Integer skuId = toInt(itemPayload.get("skuId"));
        if (skuId == null && wechatLiveGoods != null && wechatLiveGoods.getProductSkuId() != null && wechatLiveGoods.getProductSkuId() > 0) {
            skuId = wechatLiveGoods.getProductSkuId().intValue();
        }
        ProductSku productSku = skuId != null ? productSkuMapper.selectById(skuId) : null;

        Integer quantity = Optional.ofNullable(toInt(itemPayload.get("quantity"))).orElse(1);
        BigDecimal unitPrice = parseAmount(itemPayload.get("price"),
                productSku != null ? productSku.getSkuPrice() : product.getProductPrice());

        WechatLiveContextDTO contextDTO = new WechatLiveContextDTO();
        contextDTO.setRoomId(resolvedRoomId);
        contextDTO.setWechatGoodsId(wechatGoodsId);
        contextDTO.setProductId(productId);
        contextDTO.setSkuId(skuId);
        contextDTO.setShareOpenId(firstNonBlank(asString(itemPayload.get("shareOpenId")), asString(payload.get("shareOpenId"))));
        contextDTO.setViewerOpenId(firstNonBlank(asString(itemPayload.get("viewerOpenId")), asString(payload.get("viewerOpenId")), asString(payload.get("openId"))));
        contextDTO.setScene(firstNonBlank(asString(itemPayload.get("scene")), asString(payload.get("scene"))));
        contextDTO.setCustomParams(firstNonBlank(asString(itemPayload.get("customParams")), asString(payload.get("customParams"))));

        OrderItem orderItem = new OrderItem();
        orderItem.setUserId(userId);
        orderItem.setProductId(productId);
        orderItem.setProductName(product.getProductName());
        orderItem.setProductSn(product.getProductSn());
        orderItem.setPicThumb(product.getPicThumb());
        orderItem.setShopId(wechatLiveGoods != null && wechatLiveGoods.getShopId() != null ? wechatLiveGoods.getShopId() : product.getShopId());
        orderItem.setSkuId(skuId);
        orderItem.setSkuData(productSku != null ? productSku.getSkuData() : "");
        orderItem.setPrice(unitPrice);
        orderItem.setOriginPrice(unitPrice);
        orderItem.setQuantity(quantity);
        orderItem.setDeliveryQuantity(0);
        orderItem.setProductType(Optional.ofNullable(product.getProductType()).orElse(1));
        orderItem.setIsGift(0);
        orderItem.setIsPin(0);
        orderItem.setIsSeckill(0);
        orderItem.setPromotionData(WechatLiveOrderContextUtils.mergeIntoPromotionData("", contextDTO));
        return orderItem;
    }

    private void createPaidPaylog(Order order,
                                  WechatLiveSyncCallbackDTO callbackDTO,
                                  Map<String, Object> payload,
                                  BigDecimal amount,
                                  Long now) {
        String paySn = firstNonBlank(
                asString(payload.get("paySn")),
                asString(payload.get("outTradeNo")),
                asString(payload.get("transactionId")),
                callbackDTO.getOrderSn(),
                order.getOrderSn()
        );
        Paylog paylog = new Paylog();
        paylog.setPaySn(paySn);
        paylog.setPayName("微信支付");
        paylog.setPayCode("wechat");
        paylog.setOrderId(order.getOrderId());
        paylog.setOrderSn(order.getOrderSn());
        paylog.setOrderType(0);
        paylog.setOrderAmount(amount);
        paylog.setPayAmount(amount);
        paylog.setPayStatus(PayStatusEnum.PAID.getCode());
        paylog.setTransactionId(firstNonBlank(asString(payload.get("transactionId")), paySn));
        paylog.setNotifyData(JSONUtil.toJsonStr(payload));
        paylog.setRefundAmount(BigDecimal.ZERO);
        paylog.setAddTime(now);
        paylogMapper.insert(paylog);
    }

    private boolean isPaidCallback(WechatLiveSyncCallbackDTO callbackDTO, Map<String, Object> payload) {
        String status = Optional.ofNullable(callbackDTO.getStatus()).orElse("").toUpperCase(Locale.ROOT);
        if (status.contains("UNPAID") || status.contains("PENDING")) {
            return false;
        }
        if (status.contains("PAID") || status.contains("SUCCESS")) {
            return true;
        }
        Integer payStatus = toInt(payload.get("payStatus"));
        if (payStatus != null) {
            return payStatus > 0;
        }
        return true;
    }

    private List<Map<String, Object>> extractOrderItemPayload(Map<String, Object> payload) {
        for (String key : List.of("items", "orderItems", "goodsList", "products")) {
            Object value = payload.get(key);
            if (!(value instanceof Collection<?> collection)) {
                continue;
            }
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object item : collection) {
                if (item instanceof Map<?, ?> mapObj) {
                    Map<String, Object> mapped = new HashMap<>();
                    mapObj.forEach((k, v) -> mapped.put(String.valueOf(k), v));
                    result.add(mapped);
                }
            }
            if (!result.isEmpty()) {
                return result;
            }
        }
        if (payload.containsKey("productId")) {
            return List.of(new HashMap<>(payload));
        }
        return Collections.emptyList();
    }

    private String getOrCreateLocalOrderSn(String preferredOrderSn) {
        if (StringUtils.isNotBlank(preferredOrderSn)) {
            Order exists = orderMapper.selectOne(Wrappers.lambdaQuery(Order.class)
                    .eq(Order::getOrderSn, preferredOrderSn)
                    .last("limit 1"));
            if (exists == null) {
                return preferredOrderSn;
            }
        }
        String orderSn = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")) + (10000000 + new Random().nextInt(90000000));
        while (orderMapper.selectCount(Wrappers.lambdaQuery(Order.class).eq(Order::getOrderSn, orderSn)) > 0) {
            orderSn = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")) + (10000000 + new Random().nextInt(90000000));
        }
        return orderSn;
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Integer toInt(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ignored) {
            return null;
        }
    }

    private BigDecimal parseAmount(Object value, BigDecimal defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    @SafeVarargs
    private <T> T firstNonNull(T... values) {
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retrySyncLog(Long logId) {
        WechatLiveSyncLog logRecord = wechatLiveSyncLogService.getById(logId);
        if (logRecord == null) {
            return;
        }
        if (DIRECTION_INBOUND.equals(logRecord.getDirection())) {
            logRecord.setSyncStatus(SYNC_STATUS_SKIPPED);
            logRecord.setResponseData(JSONUtil.toJsonStr(Map.of("message", "inbound callback log skipped")));
            logRecord.setUpdateTime(StringUtils.getCurrentTime());
            wechatLiveSyncLogService.updateById(logRecord);
            return;
        }
        if (EVENT_ORDER_SHIPPED.equals(logRecord.getEventType())) {
            dispatchShippingSync(logRecord);
            return;
        }
        markSkipped(logRecord, "当前微信官方未开放该事件的直连接口，保留日志用于对账与补偿");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processPendingLogs() {
        long now = StringUtils.getCurrentTime();
        List<WechatLiveSyncLog> logs = wechatLiveSyncLogService.list(Wrappers.lambdaQuery(WechatLiveSyncLog.class)
                .in(WechatLiveSyncLog::getSyncStatus, List.of(SYNC_STATUS_PENDING, SYNC_STATUS_FAILED))
                .and(wrapper -> wrapper.isNull(WechatLiveSyncLog::getNextRetryTime)
                        .or()
                        .le(WechatLiveSyncLog::getNextRetryTime, now))
                .last("limit 100"));
        for (WechatLiveSyncLog logRecord : logs) {
            try {
                retrySyncLog(logRecord.getId());
            } catch (Exception e) {
                log.error("处理微信直播同步日志失败, logId={}", logRecord.getId(), e);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reconcileLiveOrders() {
        List<WechatLiveOrderRel> relList = wechatLiveOrderRelService.list(Wrappers.lambdaQuery(WechatLiveOrderRel.class)
                .orderByDesc(WechatLiveOrderRel::getId));
        if (CollUtil.isEmpty(relList)) {
            return;
        }
        List<Integer> orderIds = relList.stream().map(WechatLiveOrderRel::getOrderId).distinct().toList();
        List<Order> orders = orderMapper.selectBatchIds(orderIds);
        for (Order order : orders) {
            if (order == null) {
                continue;
            }
            ensureEventExists(order, EVENT_ORDER_CREATED);
            if (order.getPayStatus() != null && order.getPayStatus() > 0) {
                ensureEventExists(order, EVENT_ORDER_PAID);
            }
            if (Objects.equals(order.getShippingStatus(), ShippingStatusEnum.SENT.getCode())
                    || Objects.equals(order.getShippingStatus(), ShippingStatusEnum.SHIPPED.getCode())) {
                ensureEventExists(order, EVENT_ORDER_SHIPPED);
            }
        }
    }

    private void dispatchShippingSync(WechatLiveSyncLog logRecord) {
        Order order = orderMapper.selectById(logRecord.getOrderId());
        if (order == null || !hasLiveRelation(order.getOrderId())) {
            markSkipped(logRecord, "直播订单关系不存在，跳过同步");
            return;
        }
        List<OrderItemVO> deliverItems = orderItemMapper.selectList(Wrappers.lambdaQuery(OrderItem.class)
                        .eq(OrderItem::getOrderId, order.getOrderId()))
                .stream()
                .map(item -> {
                    OrderItemVO itemVO = new OrderItemVO();
                    org.springframework.beans.BeanUtils.copyProperties(item, itemVO);
                    return itemVO;
                })
                .collect(Collectors.toList());
        try {
            UploadShippingInfoParam param = buildShippingParam(order, deliverItems);
            String requestData = JSONUtil.toJsonStr(param);
            String accessToken = getAccessToken();
            UploadShippingInfoResult result = wechatApiClient.uploadShippingInfo(accessToken, param);
            if (result == null || ObjectUtil.notEqual(result.getErrcode(), 0)) {
                throw new GlobalException("发货信息同步失败：" + (result != null ? result.getErrmsg() : ""));
            }
            logRecord.setSyncStatus(SYNC_STATUS_SUCCESS);
            logRecord.setRequestData(requestData);
            logRecord.setResponseData(JSONUtil.toJsonStr(result));
            logRecord.setErrorMessage(null);
            logRecord.setLastSyncTime(StringUtils.getCurrentTime());
            logRecord.setUpdateTime(StringUtils.getCurrentTime());
            wechatLiveSyncLogService.updateById(logRecord);
            updateRelationStatus(order.getOrderId(), SYNC_STATUS_SUCCESS);
        } catch (Exception e) {
            logRecord.setSyncStatus(SYNC_STATUS_FAILED);
            logRecord.setErrorMessage(e.getMessage());
            logRecord.setRetryCount(Optional.ofNullable(logRecord.getRetryCount()).orElse(0) + 1);
            logRecord.setLastSyncTime(StringUtils.getCurrentTime());
            logRecord.setNextRetryTime(StringUtils.getCurrentTime() + 300);
            logRecord.setUpdateTime(StringUtils.getCurrentTime());
            wechatLiveSyncLogService.updateById(logRecord);
            updateRelationStatus(order.getOrderId(), SYNC_STATUS_FAILED);
            throw e;
        }
    }

    private UploadShippingInfoParam buildShippingParam(Order order, List<OrderItemVO> deliverItems) {
        Paylog paylog = paylogMapper.selectOne(
                Wrappers.lambdaQuery(Paylog.class)
                        .eq(Paylog::getOrderId, order.getOrderId())
                        // 商品订单：orderType=0，避免撞充值单号
                        .eq(Paylog::getOrderType, 0)
                        .eq(Paylog::getPayStatus, PayStatusEnum.PAID.getCode())
                        .last("limit 1")
        );
        if (paylog == null) {
            throw new GlobalException("未找到已支付的支付流水");
        }

        String wechatPayMchId = configService.getConfigVal(SettingsEnum.WECHAT_PAY_MCHID);
        UploadShippingInfoParam.OrderKey orderKey = UploadShippingInfoParam.OrderKey.builder()
                .orderNumberType(1)
                .mchid(wechatPayMchId)
                .outTradeNo(paylog.getPaySn())
                .build();


        int logisticsType;
        if (order.getLogisticsId() != null && order.getLogisticsId() != 0) {
            logisticsType = 1;
        } else {
            logisticsType = 3;
        }

        UploadShippingInfoParam.Shipping shipping;
            LogisticsCompany logisticsCompany = logisticsCompanyService.lambdaQuery()
                    .eq(LogisticsCompany::getLogisticsId, order.getLogisticsId())
                    .one();
            String expressCompany = logisticsCompany != null ? logisticsCompany.getLogisticsCode() : "";
            shipping = UploadShippingInfoParam.Shipping.builder()
                    .trackingNo(order.getTrackingNo())
                    .expressCompany(expressCompany)
                    .itemDesc(!deliverItems.isEmpty() ? deliverItems.getFirst().getProductName() : "")
                    .build();
        shipping.assembleContact(null, order.getMobile());

        boolean isAllDelivered = order.getParentOrderId() == null;
        if (!isAllDelivered) {
            Long pendingCount = orderMapper.selectCount(Wrappers.lambdaQuery(Order.class)
                    .eq(Order::getParentOrderId, order.getParentOrderId())
                    .eq(Order::getShippingStatus, ShippingStatusEnum.PENDING.getCode())
                    .ne(Order::getOrderId, order.getOrderId()));
            isAllDelivered = pendingCount == 0;
        }

        if (order.getShippingTime() == null || order.getShippingTime() == 0) {
            throw new GlobalException("订单发货时间为空");
        }
        ZonedDateTime zdt = Instant.ofEpochMilli(order.getShippingTime()).atZone(ZoneId.of("Asia/Shanghai"));
        String uploadTime = zdt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        UserAuthorize userAuthorize = userAuthorizeMapper.selectOne(Wrappers.lambdaQuery(UserAuthorize.class)
                .isNotNull(UserAuthorize::getOpenId)
                .eq(UserAuthorize::getUserId, order.getUserId())
                .last("limit 1"));
        if (userAuthorize == null || userAuthorize.getOpenId() == null) {
            throw new GlobalException("用户openid不存在");
        }

        return UploadShippingInfoParam.builder()
                .orderKey(orderKey)
                .logisticsType(logisticsType)
                .deliveryMode(1)
                .isAllDelivered(isAllDelivered)
                .shippingList(Collections.singletonList(shipping))
                .uploadTime(uploadTime)
                .payer(new UploadShippingInfoParam.Payer(userAuthorize.getOpenId()))
                .build();
    }

    private String getAccessToken() {
        String accessToken = wechatOAuthService.getMpAccessToken();
        if (StrUtil.isBlank(accessToken)) {
            throw new GlobalException("获取微信accessToken失败");
        }
        return accessToken;
    }

    private void ensureEventExists(Order order, String eventType) {
        long count = wechatLiveSyncLogService.count(Wrappers.lambdaQuery(WechatLiveSyncLog.class)
                .eq(WechatLiveSyncLog::getOrderId, order.getOrderId())
                .eq(WechatLiveSyncLog::getEventType, eventType)
                .eq(WechatLiveSyncLog::getDirection, DIRECTION_OUTBOUND));
        if (count == 0) {
            createOutboundLog(order, eventType, null, SYNC_STATUS_PENDING);
        }
    }

    private void markSkipped(WechatLiveSyncLog logRecord, String message) {
        logRecord.setSyncStatus(SYNC_STATUS_SKIPPED);
        logRecord.setResponseData(JSONUtil.toJsonStr(Map.of("message", message)));
        logRecord.setUpdateTime(StringUtils.getCurrentTime());
        logRecord.setLastSyncTime(StringUtils.getCurrentTime());
        wechatLiveSyncLogService.updateById(logRecord);
    }

    private boolean hasLiveRelation(Integer orderId) {
        if (orderId == null || orderId <= 0) {
            return false;
        }
        return wechatLiveOrderRelService.count(Wrappers.lambdaQuery(WechatLiveOrderRel.class)
                .eq(WechatLiveOrderRel::getOrderId, orderId)) > 0;
    }

    private WechatLiveSyncLog createOutboundLog(Order order, String eventType, String requestData, Integer status) {
        if (order == null || order.getOrderId() == null || !hasLiveRelation(order.getOrderId())) {
            return null;
        }
        WechatLiveSyncLog logRecord = new WechatLiveSyncLog();
        logRecord.setOrderId(order.getOrderId());
        logRecord.setRoomId(wechatLiveOrderRelService.listByOrderId(order.getOrderId()).stream()
                .map(WechatLiveOrderRel::getRoomId)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null));
        logRecord.setEventType(eventType);
        logRecord.setDirection(DIRECTION_OUTBOUND);
        logRecord.setSyncStatus(status);
        logRecord.setIdempotentKey(eventType + ":" + order.getOrderId() + ":" + System.nanoTime());
        logRecord.setRequestData(requestData);
        logRecord.setRetryCount(0);
        logRecord.setShopId(order.getShopId());
        logRecord.setAddTime(StringUtils.getCurrentTime());
        logRecord.setUpdateTime(StringUtils.getCurrentTime());
        wechatLiveSyncLogService.save(logRecord);
        return logRecord;
    }

    private WechatLiveSyncLog getLatestOutboundLog(Integer orderId, String eventType) {
        if (orderId == null) {
            return null;
        }
        return wechatLiveSyncLogService.getOne(Wrappers.lambdaQuery(WechatLiveSyncLog.class)
                .eq(WechatLiveSyncLog::getOrderId, orderId)
                .eq(WechatLiveSyncLog::getEventType, eventType)
                .eq(WechatLiveSyncLog::getDirection, DIRECTION_OUTBOUND)
                .orderByDesc(WechatLiveSyncLog::getId)
                .last("limit 1"));
    }

    private void updateRelationStatus(Integer orderId, Integer status) {
        if (orderId == null) {
            return;
        }
        List<WechatLiveOrderRel> relList = wechatLiveOrderRelService.listByOrderId(orderId);
        if (relList.isEmpty()) {
            return;
        }
        long now = StringUtils.getCurrentTime();
        relList.forEach(rel -> {
            rel.setSyncStatus(status);
            rel.setLastSyncTime(now);
            rel.setUpdateTime(now);
        });
        wechatLiveOrderRelService.updateBatchById(relList);
    }
}
