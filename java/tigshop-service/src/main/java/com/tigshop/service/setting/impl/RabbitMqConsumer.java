// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.setting.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tigshop.bean.enums.setting.SmsProviderEnum;
import com.tigshop.service.sms.SmsProvider;
import com.tigshop.service.sms.SmsProviderFactory;
import com.tigshop.service.sms.SmsResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tigshop.bean.dto.common.TranslateDTO;
import com.tigshop.bean.dto.im.AutoSendDTO;
import com.tigshop.bean.dto.print.PrintOrderDTO;
import com.tigshop.bean.dto.setting.SmsSendDTO;
import com.tigshop.bean.dto.vendor.VendorDiscontinueDTO;
import com.tigshop.bean.enums.finance.StatementType;
import com.tigshop.bean.enums.order.*;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.enums.supplychain.VendorSourceTypeEnum;
import com.tigshop.bean.feign.feieyun.FeieyunApiResult;
import com.tigshop.bean.model.finance.Paylog;
import com.tigshop.bean.model.finance.RecordRate;
import com.tigshop.bean.model.finance.StatementOutput;
import com.tigshop.bean.model.lang.Locales;
import com.tigshop.bean.model.lang.Translations;
import com.tigshop.bean.model.order.Aftersales;
import com.tigshop.bean.model.order.AftersalesItem;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.model.print.Print;
import com.tigshop.bean.model.salesman.Salesman;
import com.tigshop.bean.model.salesman.SalesmanConfig;
import com.tigshop.bean.model.salesman.SalesmanCustomer;
import com.tigshop.bean.model.salesman.SalesmanOrder;
import com.tigshop.bean.model.shop.Account;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.user.UserRank;
import com.tigshop.bean.model.user.UserRankConfig;
import com.tigshop.bean.model.vendor.Vendor;
import com.tigshop.bean.model.vendor.VendorAccountLog;
import com.tigshop.bean.model.vendor.VendorSettlementOrder;
import com.tigshop.bean.param.finance.statement.StatementSaveParam;
import com.tigshop.bean.param.print.PrintTemplate;
import com.tigshop.bean.param.print.PrintTemplateItem;
import com.tigshop.bean.param.print.PrintTemplateOption;
import com.tigshop.bean.vo.order.OrderItemVO;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.bean.vo.print.PrintConfigVO;
import com.tigshop.common.annotation.RetryLimit;
import com.tigshop.common.config.RabbitMQConfig;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.enums.DataType;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.FeieyunApiUtils;
import com.tigshop.common.utils.RedisCache;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.feign.FeieyunApiClient;
import com.tigshop.mapper.finance.PaylogMapper;
import com.tigshop.mapper.finance.StatementOutputMapper;
import com.tigshop.mapper.order.AftersalesItemMapper;
import com.tigshop.mapper.salesman.SalesmanConfigMapper;
import com.tigshop.mapper.salesman.SalesmanCustomerMapper;
import com.tigshop.mapper.salesman.SalesmanMapper;
import com.tigshop.mapper.salesman.SalesmanOrderMapper;
import com.tigshop.mapper.user.UserRankConfigMapper;
import com.tigshop.mapper.user.UserRankMapper;
import com.tigshop.mapper.vendor.VendorAccountLogMapper;
import com.tigshop.mapper.vendor.VendorSettlementOrderMapper;
import com.tigshop.mapper.vendor.product.VendorProductSkuMapper;
import com.tigshop.service.finance.StatementService;
import com.tigshop.service.im.ImMessageService;
import com.tigshop.service.lang.TranslationsService;
import com.tigshop.service.order.AftersalesService;
import com.tigshop.service.order.OrderItemService;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.print.PrintConfigService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.promotion.GrouponPromotionRecordService;
import com.tigshop.service.finance.SubMerchantService;
import com.tigshop.service.finance.profitsharing.ProfitSharingChannelResolver;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.OnlineSplitSettlementMath;
import com.tigshop.service.shop.AccountService;
import com.tigshop.service.shop.ShopService;
import com.tigshop.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.tigshop.bean.enums.order.AftersalesStatusEnum.IN_REVIEW;

/**
 * @author Tigshop
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class RabbitMqConsumer {

    private final OrderService orderService;

    private final OrderItemService orderItemService;

    private final ImMessageService imMessageService;

    private final RedisCache redisCache;

    private final AftersalesService aftersalesService;

    private final AccountService accountService;

    private final ShopService shopService;


    private final TranslationsService translationsService;

    private final ConfigService configService;


    private final ProductService productService;

    private final StatementService statementService;

    private static final Integer MAX_RETRY_COUNT = 5;
    private static final Integer MAX_RETRY_COUNT_TTL_MINUTES = 5;
    private final SalesmanConfigMapper salesmanConfigMapper;
    private final SalesmanMapper salesmanMapper;
    private final SalesmanCustomerMapper salemanCustomerMapper;
    private final SalesmanOrderMapper salesmanOrderMapper;
    private final UserRankMapper userRankMapper;
    private final UserService userService;
    private final UserRankConfigMapper userRankConfigMapper;
    private final VendorProductSkuMapper vendorProductSkuMapper;
    private final VendorAccountLogMapper vendorAccountLogMapper;
    private final PrintConfigService printConfigService;
    private final PaylogMapper paylogMapper;
    // 打印链路按平台分发：渲染器 + 适配器 + 日志服务
    private final com.tigshop.service.print.renderer.ReceiptRendererFactory rendererFactory;
    private final com.tigshop.service.print.adapter.PrintPlatformAdapterFactory printAdapterFactory;
    private final com.tigshop.service.print.PrintLogService printLogService;
    private final VendorSettlementOrderMapper vendorSettlementOrderMapper;
    private final AftersalesItemMapper aftersalesItemMapper;
    private final TigshopProperties tigshopProperties;
    private final StatementOutputMapper statementOutputMapper;
    private final GrouponPromotionRecordService grouponPromotionRecordService;
    private final SmsProviderFactory smsProviderFactory;
    private final com.tigshop.service.salesman.SalesmanOrderService salesmanOrderService;
    private final SubMerchantService subMerchantService;

    @RabbitListener(queues = RabbitMQConfig.GROUPON_RECORD_QUEUE)
    @RetryLimit
    public void receiveGrouponRecord(Long grouponRecordId) {
        log.info("receiveGrouponRecord：{}", grouponRecordId);
        grouponPromotionRecordService.expirationTime(grouponRecordId);
    }

    @RabbitListener(queues = RabbitMQConfig.SEND_SMS_QUEUE)
    @RetryLimit
    public void receiveSms(SmsSendDTO smsSend) {
        log.info("收到短信任务：{}", JSONUtil.toJsonStr(smsSend));
        sendSms(smsSend);
    }

    private void sendSms(SmsSendDTO smsSend) {
        SmsProviderEnum providerEnum = SmsProviderEnum.fromCode(smsSend.getSmsProvider());
        log.info("正在发送短信，服务商：{}，号码：{}，模板：{}", providerEnum.getName(), smsSend.getPhoneNumbers(), smsSend.getTemplateCode());

        SmsProvider provider = smsProviderFactory.getProvider(providerEnum);
        SmsResult result = provider.sendSms(smsSend);

        if (!result.isSuccess()) {
            log.error("短信发送失败！服务商：{}，号码：{}，错误码：{}，错误信息：{}",
                    providerEnum.getName(), smsSend.getPhoneNumbers(), result.getCode(), result.getMessage());
        } else {
            log.info("短信发送成功！服务商：{}，号码：{}", providerEnum.getName(), smsSend.getPhoneNumbers());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_CANCEL_QUEUE)
    @RetryLimit
    public void receiveOrderCancelMessage(Integer orderId) {
        log.info("收到订单取消消息：{}", orderId);
        orderService.cancelOrder(orderId, null,"");
    }

    @RabbitListener(queues = RabbitMQConfig.AUTO_MESSAGE_QUEUE)
    @RetryLimit
    public void receiveAutoReplyMessage(AutoSendDTO autoSend) {
        // 在此执行自动回复逻辑
        if ("autoWelcome".equals(autoSend.getType())) {
            imMessageService.autoWelcome(Integer.parseInt(autoSend.getConversationId()));
        } else if ("autoWaiting".equals(autoSend.getType())) {
            imMessageService.autoWaiting(Integer.parseInt(autoSend.getConversationId()));
        } else if ("autoOffWork".equals(autoSend.getType())) {
            imMessageService.autoOffWork(Integer.parseInt(autoSend.getConversationId()));
        } else if ("autoTransfer".equals(autoSend.getType())) {
            imMessageService.autoTransfer(Integer.parseInt(autoSend.getConversationId()),
                    Integer.parseInt(autoSend.getOldServantId()),
                    Integer.parseInt(autoSend.getOldServantId()));
        }
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_CONFIRM_RECEIPT_QUEUE)
    @RetryLimit
    public void receiveOrderConfirmReceiptMessage(Integer orderId) {
        log.info("收到订单自动确认收货消息：{}", orderId);

        // 判断是否已经收货
        Long receivedCount = orderService.lambdaQuery()
                .eq(Order::getOrderId, orderId)
                .eq(Order::getShippingStatus, ShippingStatusEnum.SHIPPED.getCode())
                .count();
        if (receivedCount == 1) {
            return;
        }

        // 判断订单是否售后中
        Long aftersalesCount = aftersalesService.lambdaQuery()
                .eq(Aftersales::getOrderId, orderId)
                .eq(Aftersales::getStatus, AftersalesStatusEnum.IN_REVIEW.getCode())
                .count();
        if (aftersalesCount == 1) {
            return;
        }

        // 整单售后进行中：已发货订单在审核通过后仍为"已发货"，此时自动确认收货会让订单错误完成并触发结算
        if (aftersalesService.hasInProgressFullOrderAftersales(orderId)) {
            return;
        }


        orderService.confirmReceipt(orderId);
    }


    /**
     * 订单完成后延迟赠送积分（会员订单已完成 N 天后赠送积分）
     */
    @RabbitListener(queues = RabbitMQConfig.ORDER_COMPLETE_SEND_POINT_QUEUE)
    @RetryLimit
    public void receiveOrderCompleteSendPointMessage(Integer orderId) {
        log.info("收到订单完成延迟赠送积分消息：orderId={}", orderId);
        try {
            orderService.doOrderCompleteSendPoint(orderId);
        } catch (Exception e) {
            log.error("订单完成延迟赠送积分异常，orderId={}", orderId, e);
            throw e;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.AUTO_RETURN_GOODS_QUEUE)
    @RetryLimit
    public void receiveAutoReturnGoodsMessage(Integer aftersalesId) {
        log.info("收到订单退货申请自动确认消息：{}", aftersalesId);

        // 售后审核开关已关闭时，延迟消息无需处理（申请时已自动通过）
        String afterSalesReview = configService.getConfigVal(SettingsEnum.AFTER_SALES_REVIEW);
        if (!"1".equals(afterSalesReview)) {
            log.info("售后审核已关闭，跳过自动确认消息：{}", aftersalesId);
            return;
        }

        Aftersales aftersales = aftersalesService.lambdaQuery()
                .eq(Aftersales::getAftersaleId, aftersalesId)
                .eq(Aftersales::getStatus, AftersalesStatusEnum.IN_REVIEW.getCode())
                .eq(Aftersales::getAftersaleType, AftersalesTypeEnum.RETURN.getCode())
                .one();
        if (aftersales == null) {
            return;
        }
        aftersalesService.autoApproveReturnAftersales(aftersalesId);
    }

    @RabbitListener(queues = RabbitMQConfig.AFTERSALES_AUTO_RECEIPT_QUEUE)
    @RetryLimit
    public void receiveAftersalesAutoReceiptMessage(Integer aftersalesId) {
        log.info("收到售后退货自动确认收货消息：{}", aftersalesId);
        boolean result = aftersalesService.autoReceive(aftersalesId);
        if (!result) {
            log.info("售后无需自动确认收货：{}", aftersalesId);
        }
    }


    @RabbitListener(queues = RabbitMQConfig.STORE_SETTLEMENT_QUEUE)
    @RetryLimit
    @Transactional(rollbackFor = Exception.class)
    public void storeSettlementMessage(Integer orderId) {
        log.info("【进入队列】收到订单确认收货后自动结算店铺资金消息：{}", orderId);

        Order order = orderService.getById(orderId);

        if (order == null) {
            log.error("【错误】未找到订单信息：{}", orderId);
            return;
        }
        // 判断是否已经结算
        if (!Objects.equals(order.getIsSettlement(), Constants.NO)) {
            return;
        }

        Shop shop = shopService.getById(order.getShopId());
        if (shop == null) {
            log.error("【错误】未找到店铺信息：{}", order.getShopId());
            return;
        }
        // 先原子抢占结算权；MQ 重投或多实例并发时只有一个消费者能修改资金。
        boolean claimed = orderService.lambdaUpdate()
                .eq(Order::getOrderId, orderId)
                .eq(Order::getIsSettlement, Constants.NO)
                .set(Order::getIsSettlement, Constants.YES)
                .update();
        if (!claimed) {
            return;
        }
        BigDecimal paidAmount = order.getPaidAmount();
        BigDecimal refundAmount = BigDecimal.ZERO;
        List<Aftersales> aftersalesList = aftersalesService.lambdaQuery()
                .eq(Aftersales::getOrderId, orderId)
                .eq(Aftersales::getStatus, AftersalesStatusEnum.COMPLETE.getCode())
                .list();
        if (CollUtil.isNotEmpty(aftersalesList)) {
            for (Aftersales aftersales : aftersalesList) {
                refundAmount = refundAmount.add(aftersales.getRefundAmount());
                if (order.getVendorId() != null && order.getVendorId() > 0) {
                    refundAmount = refundAmount.add(getVendorRefundAmount(orderId, null));
                }
            }
        }
        paidAmount = paidAmount.subtract(refundAmount);
        BigDecimal vendorAmount = BigDecimal.ZERO;
        if (order.getVendorId() != null && order.getVendorId() > 0) {
            // 供应商订单
            vendorAmount = getVendorOrderAmount(orderId);
        }

        paidAmount = paidAmount.subtract(vendorAmount);

        // 分销佣金由店铺承担：从店铺结算金额中扣减佣金
        BigDecimal commissionAmount = salesmanOrderService.getTotalCommissionByOrderId(orderId);
        paidAmount = paidAmount.subtract(commissionAmount);

        OnlineSplitCtx splitCtx = resolveOnlineSplit(order);
        if (splitCtx.onlineSplit()) {
            // 线上分账：二级户 portion 已在微信二级户，shopMoney 只结算余额 portion，不扣服务费/佣金
            BigDecimal shopNet = OnlineSplitSettlementMath.shopMoneyNetOnSettle(
                    order.getPaidAmount(), splitCtx.onlineAmount(), refundAmount);
            if (shopNet.compareTo(BigDecimal.ZERO) > 0) {
                shopService.lambdaUpdate()
                        .eq(Shop::getShopId, order.getShopId())
                        .setIncrBy(Shop::getShopMoney, shopNet)
                        .update();
                this.saveAccount(shop, shopNet, order);
            } else {
                log.info("订单[{}]线上分账结算：余额 portion 净额为0，跳过 shopMoney", orderId);
            }
        } else {
            BigDecimal fee = BigDecimal.ZERO;
            if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                RecordRate recordRate = statementService.getRecordRate(orderId, StatementType.ORDER.getCode());
                BigDecimal shopServiceFee = recordRate.getShopServiceFee();
                fee = paidAmount.multiply(shopServiceFee)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
            shopService.lambdaUpdate()
                    .eq(Shop::getShopId, order.getShopId())
                    .setIncrBy(Shop::getShopMoney, paidAmount.subtract(fee))
                    .update();

            //保存资金账户日志信息
            this.saveAccount(shop, paidAmount.subtract(fee), order);
        }

        // 供应商结算独立于店铺结算，店铺事务提交后再投递 MQ，避免店铺结算回滚时供应商先入账。
        // 订单结算
        updateStatement2Settlement(orderId, StatementType.ORDER.getCode());
        // 服务费结算
        updateStatement2Settlement(orderId, StatementType.SERVICE_FEE.getCode());
        // 分销佣金结算
        updateStatement2Settlement(orderId, StatementType.DISTRIBUTION_COMMISSION.getCode());
        // 售后结算
        List<Integer> aftersalesIds = aftersalesList.stream().map(Aftersales::getAftersaleId).toList();
        aftersalesIds.forEach(aftersalesId -> updateStatement2Settlement(aftersalesId, StatementType.ORDER_REFUND.getCode()));
        // 服务费结算
        aftersalesIds.forEach(aftersalesId -> updateStatement2Settlement(aftersalesId, StatementType.SERVICE_FEE.getCode()));
    }

    @RabbitListener(queues = RabbitMQConfig.REFUND_SETTLEMENT_QUEUE)
    @RetryLimit
    public void refundSettlementMessage(Integer aftersalesId) {
        log.info("【进入队列】收到退款结算：{}", aftersalesId);

        Aftersales byId = aftersalesService.getById(aftersalesId);
        if (byId == null) {
            log.error("【错误】未找到售后信息：{}", aftersalesId);
            return;
        }
        Integer orderId = byId.getOrderId();
        Order order = orderService.getById(orderId);

        if (order == null) {
            log.error("【错误】未找到订单信息：{}", orderId);
            return;
        }
        // 判断是否已经结算
        if (Objects.equals(order.getIsSettlement(), Constants.NO)) {
            return;
        }

        try {
            Shop shop = shopService.getById(order.getShopId());

            // 获取供应商退款金额（根据订单商品中的供应商商品计算）
            BigDecimal vendorRefundAmount = getVendorRefundAmount(orderId, aftersalesId);

            // 确定供应商ID：优先使用售后记录的vendorId，其次使用订单的vendorId
            Integer vendorId = byId.getVendorId();
            if (vendorId == null || vendorId <= 0) {
                vendorId = order.getVendorId();
            }

            BigDecimal shopRefundAmount = BigDecimal.ZERO;
            List<Aftersales> aftersalesList = aftersalesService.lambdaQuery()
                    .eq(Aftersales::getOrderId, orderId)
                    .eq(Aftersales::getAftersaleId, aftersalesId)
                    .eq(Aftersales::getStatus, AftersalesStatusEnum.COMPLETE.getCode())
                    .list();
            if (CollUtil.isNotEmpty(aftersalesList)) {
                for (Aftersales aftersales : aftersalesList) {
                    shopRefundAmount = shopRefundAmount.add(aftersales.getRefundAmount());
                    // 如果有供应商退款金额，从店铺退款金额中扣除
                    if (vendorRefundAmount.compareTo(BigDecimal.ZERO) > 0) {
                        shopRefundAmount = shopRefundAmount.subtract(vendorRefundAmount);
                    }
                }
            }

            // 分销佣金由店铺承担：退款时待结算佣金返还店铺（减少店铺扣减额）
            BigDecimal pendingCommission = getPendingDistributionCommission(orderId);
            if (pendingCommission.compareTo(BigDecimal.ZERO) > 0) {
                shopRefundAmount = shopRefundAmount.subtract(pendingCommission);
            }

            RecordRate recordRate = statementService.getRecordRate(orderId, StatementType.ORDER.getCode());
            BigDecimal shopServiceFee = recordRate.getShopServiceFee();
            OnlineSplitCtx splitCtx = resolveOnlineSplit(order);
            if (shopRefundAmount.compareTo(BigDecimal.ZERO) != 0) {
                if (splitCtx.onlineSplit()) {
                    // 线上分账：二级户退款走分账回退，shopMoney 只扣余额份额，不扣服务费
                    BigDecimal balanceDecr = OnlineSplitSettlementMath.shopMoneyDecrOnRefund(
                            shopRefundAmount, order.getPaidAmount(), splitCtx.onlineAmount());
                    if (balanceDecr.compareTo(BigDecimal.ZERO) != 0) {
                        this.saveDescAccount(shop, balanceDecr, order);
                        shopService.lambdaUpdate()
                                .eq(Shop::getShopId, order.getShopId())
                                .setDecrBy(Shop::getShopMoney, balanceDecr)
                                .update();
                    } else {
                        log.info("售后[{}]线上分账退款：余额份额为0，跳过 shopMoney 扣减", aftersalesId);
                    }
                } else {
                    BigDecimal fee = BigDecimal.ZERO;
                    if (shopRefundAmount.compareTo(BigDecimal.ZERO) > 0) {
                        fee = shopRefundAmount.multiply(shopServiceFee)
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    }
                    //保存资金账户日志信息
                    this.saveDescAccount(shop, shopRefundAmount.subtract(fee), order);
                    shopService.lambdaUpdate()
                            .eq(Shop::getShopId, order.getShopId())
                            .setDecrBy(Shop::getShopMoney, shopRefundAmount.subtract(fee))
                            .update();
                }
            }
            BigDecimal supplierServiceFee = recordRate.getSupplierServiceFee();


        } catch (GlobalException e) {
            log.error("【异常】收到退款结算 RabbitMQ：{}", e.getMessage());
        }

    }

    public void updateStatement2Settlement(Integer record, Integer recordType) {
        int update = statementOutputMapper.update(new LambdaUpdateWrapper<StatementOutput>()
                .eq(StatementOutput::getRecordId, record)
                .eq(StatementOutput::getRecordType, recordType)
                .set(StatementOutput::getRealSettlement, Constants.YES)
                .set(StatementOutput::getRealSettlementTime, StringUtils.getCurrentTime()));
        log.info("【出账单】更新 {} 条为已结算", update);
    }

    private BigDecimal getPendingDistributionCommission(Integer orderId) {
        List<StatementOutput> outputs = statementOutputMapper.selectList(
                new LambdaQueryWrapper<StatementOutput>()
                        .eq(StatementOutput::getRecordId, orderId)
                        .eq(StatementOutput::getRecordType, StatementType.DISTRIBUTION_COMMISSION.getCode())
                        .eq(StatementOutput::getRealSettlement, Constants.NO));
        if (CollUtil.isEmpty(outputs)) {
            return BigDecimal.ZERO;
        }
        return outputs.stream()
                .map(StatementOutput::getExpenditure)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 供应商分的钱款
     *
     * @param orderId
     * @return
     */
    private BigDecimal getVendorOrderAmount(Integer orderId) {
        BigDecimal vendorAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = orderItemService.lambdaQuery()
                .eq(OrderItem::getOrderId, orderId)
                .isNotNull(OrderItem::getVendorId)
                .list();

        if (CollUtil.isEmpty(orderItems)) {
            return BigDecimal.ZERO;
        }

        // 计算供应商应得金额（扣除退款）
        for (OrderItem item : orderItems) {
            BigDecimal itemAmount = item.getVendorProductSupplyPrice().multiply(new BigDecimal(item.getQuantity()));
            vendorAmount = vendorAmount.add(itemAmount);
        }
        return vendorAmount;
    }

    /**
     * 供应商退的钱款
     *
     * @param orderId
     * @return
     */
    private BigDecimal getVendorRefundAmount(Integer orderId, Integer aftersalesId) {
        List<OrderItem> orderItems = orderItemService.lambdaQuery()
                .eq(OrderItem::getOrderId, orderId)
                .isNotNull(OrderItem::getVendorId)
                .list();

        if (CollUtil.isEmpty(orderItems)) {
            return BigDecimal.ZERO;
        }
        List<Integer> aftersalesIds = null;
        if (aftersalesId != null) {
            aftersalesIds = List.of(aftersalesId);
        } else {
            List<Aftersales> vendorAftersalesList = aftersalesService.lambdaQuery()
                    .eq(Aftersales::getOrderId, orderId)
                    .eq(Aftersales::getStatus, AftersalesStatusEnum.COMPLETE.getCode())
                    .list();
            aftersalesIds = vendorAftersalesList.stream().map(Aftersales::getAftersaleId).toList();
        }
        // 计算供应商应得金额（扣除退款）
        BigDecimal vendorRefundAmountAll = BigDecimal.ZERO;
        for (OrderItem item : orderItems) {
            // 计算如果按件退多少
            if (ObjectUtil.isNotEmpty(aftersalesIds)) {
                List<AftersalesItem> aftersalesItems = aftersalesItemMapper.selectList(new LambdaQueryWrapper<AftersalesItem>()
                        .in(AftersalesItem::getAftersaleId, aftersalesIds)
                        .eq(AftersalesItem::getOrderItemId, item.getItemId())
                );
                if (ObjectUtil.isNotEmpty(aftersalesItems)) {
                    int number = aftersalesItems.stream().mapToInt(AftersalesItem::getNumber).sum();
                    vendorRefundAmountAll = vendorRefundAmountAll.add(item.getVendorProductSupplyPrice().multiply(new BigDecimal(number)));
                }
            }
        }
        return vendorRefundAmountAll;
    }

    private void saveAccount(Shop shop, BigDecimal paidAmount, Order order) {
        BigDecimal newShopMoney = shop.getShopMoney().add(paidAmount).setScale(2, RoundingMode.HALF_UP);
        Account account = new Account();
        account.setShopMoney(shop.getShopMoney());
        account.setShopId(order.getShopId());
        account.setFrozenMoney(shop.getFrozenMoney());
        account.setNewShopMoney(newShopMoney);
        account.setNewFrozenMoney(shop.getFrozenMoney());
        account.setType(4);
        account.setAddTime(StringUtils.getCurrentTime());
        account.setIsNew(1);
        accountService.save(account);
    }

    /**
     * 判定订单是否走线上分账结算（渠道无关：微信/汇付/支付宝），并取二级户实收（成功网关 paylog.payAmount）。
     * <p>口径见 DESIGN-online-split-settlement §九（CR-20260731-150400）：按该单自身支付渠道 +
     * 该渠道二级户是否激活判定，不强绑当前全局单选渠道，规避平台切换渠道后历史单误判。
     * <p>拉卡拉一期平台统一收款、不分账，其 payCode 解析渠道为 null → 天然走平台代收逻辑。
     * <p>异常时降级为非线上分账（走原平台代收逻辑）并 WARN，不阻断结算主流程。
     */
    private OnlineSplitCtx resolveOnlineSplit(Order order) {
        try {
            if (order == null || order.getOrderId() == null) {
                return OnlineSplitCtx.direct();
            }
            Integer shopId = order.getShopId();
            if (shopId == null || shopId <= 0) {
                return OnlineSplitCtx.direct();
            }
            // 粗粒度前置门：线上分账模式（总开关 + 分账方式=线上）
            if (!configService.isProfitSharingEnabled()) {
                return OnlineSplitCtx.direct();
            }
            if (!"2".equals(StrUtil.trimToEmpty(configService.getConfigVal(SettingsEnum.SPLIT_PAYMENT_METHOD)))) {
                return OnlineSplitCtx.direct();
            }
            // 取该单成功网关 paylog（payStatus=1 且 payAmount>0），按其 payCode 解析线上分账渠道
            List<Paylog> paidLogs = paylogMapper.selectList(new LambdaQueryWrapper<Paylog>()
                    .eq(Paylog::getOrderId, order.getOrderId())
                    // 商品订单：orderType=0，避免撞充值单号
                    .eq(Paylog::getOrderType, 0)
                    .eq(Paylog::getPayStatus, 1)
                    .gt(Paylog::getPayAmount, BigDecimal.ZERO)
                    .orderByDesc(Paylog::getPaylogId));
            for (Paylog paylog : paidLogs) {
                String channel = ProfitSharingChannelResolver.resolvePayChannel(paylog.getPayCode());
                if (channel == null) {
                    continue;
                }
                // 该单支付渠道的二级户已激活 → 资金进店铺二级户，判为线上分账单
                if (subMerchantService.getActiveByShop(shopId.longValue(), channel) != null) {
                    return new OnlineSplitCtx(true, paylog.getPayAmount());
                }
            }
            return OnlineSplitCtx.direct();
        } catch (Exception e) {
            log.warn("订单[{}]线上分账结算判定异常，降级走平台代收逻辑",
                    order != null ? order.getOrderId() : null, e);
            return OnlineSplitCtx.direct();
        }
    }

    /**
     * @param onlineSplit  是否线上分账单（资金进店铺二级户）
     * @param onlineAmount 二级户实收金额（成功网关 paylog.payAmount，渠道无关）
     */
    private record OnlineSplitCtx(boolean onlineSplit, BigDecimal onlineAmount) {
        static OnlineSplitCtx direct() {
            return new OnlineSplitCtx(false, BigDecimal.ZERO);
        }
    }

    private void saveDescAccount(Shop shop, BigDecimal refundAmount, Order order) {
        if(Objects.isNull(shop)){
            return;
        }
        BigDecimal newShopMoney = shop.getShopMoney().subtract(refundAmount).setScale(2, RoundingMode.HALF_UP);
        Account account = new Account();
        account.setShopMoney(shop.getShopMoney());
        account.setShopId(order.getShopId());
        account.setFrozenMoney(shop.getFrozenMoney());
        account.setNewShopMoney(newShopMoney);
        account.setNewFrozenMoney(shop.getFrozenMoney());
        account.setType(4);
        account.setAddTime(StringUtils.getCurrentTime());
        account.setIsNew(1);
        accountService.save(account);
    }

    @RabbitListener(queues = RabbitMQConfig.TRANSLATION_NAME_QUEUE)
    @RetryLimit
    public void translationName(TranslateDTO dto) {
        log.info("【进入队列】收到翻译信息：{}", dto.getTranslationName());
        String uuid = UUID.randomUUID().toString();
        boolean lock = redisCache.tryLock("translationName::" + dto.getTranslationName(), uuid, 60, TimeUnit.SECONDS);
        if (lock) {
            try {
                Boolean cacheObject = redisCache.getCacheObject("noConfigSecret");
                if (cacheObject != null && cacheObject) {
                    log.info("未配置翻译 cache");
                    return;
                }
                // 获取语言配置
                String langVolcengineSecret = configService.getConfigByCode(SettingsEnum.LANG_VOLCENGINE_SECRET.getBizCode()).getBizVal();
                if (ObjectUtil.isEmpty(langVolcengineSecret) || ObjectUtil.equals(langVolcengineSecret, "youLangVolcengineSecret")) {
                    log.info("未配置翻译 mysql");
                    redisCache.setCacheObject("noConfigSecret", true, 60, TimeUnit.SECONDS);
                    return;
                }
                String translationKey = SecureUtil.md5(dto.getTranslationName());
                long count = translationsService.count(new LambdaQueryWrapper<Translations>()
                        .eq(Translations::getTranslationKey, translationKey)
                        .eq(Translations::getDataType, DataType.PAGE.getCode())
                );
                if (count > 0) {
                    return;
                }
                List<Translations> translationsList = new ArrayList<>();
                Translations translations = Translations.builder()
                        .translationName(dto.getTranslationName())
                        .translationKey(translationKey)
                        .dataType(DataType.PAGE.getCode())
                        .build();
                translationsList.add(translations);
                translationsService.save(translations);
                translationsService.updateTranslation();
            } catch (Exception e) {
                log.error("【异常】收到翻译信息 RabbitMQ：{}", e.getMessage());
            } finally {
                redisCache.unlock("translationName::" + dto.getTranslationName(), uuid);
            }
        }
    }

    @RabbitListener(queues = RabbitMQConfig.SALESMAN_SETTLEMENT_QUEUE)
    @RetryLimit
    public void salesmanSettlementMessage(Integer orderId) {
        log.info(" 订单确认收货后第15天结算 ：{}", orderId);
        Long count = orderService.lambdaQuery().eq(Order::getOrderId, orderId).count();
        if(count<=0){
            //说明拆单了
            Order byId = orderService.getById(orderId);
            if (byId.getParentOrderId() != null) {
                orderService.settlement(byId.getParentOrderId(), 1);
            }
        }else{
            orderService.settlement(orderId, 1);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.SALESMAN_UPDATE_QUEUE)
    @RetryLimit
    public void salesmanUpdateMessage(Integer salesmanId) {
        log.info("判断分销员是否可以升级 ：{}", salesmanId);
        // 获取升级配置
        SalesmanConfig salesmanConfig = salesmanConfigMapper.selectOne(new LambdaQueryWrapper<SalesmanConfig>().eq(SalesmanConfig::getCode, "salesmanConfig"));
        if (salesmanConfig == null) {
            return;
        }
        String data = salesmanConfig.getData();
        JSONObject config = JSONUtil.parseObj(data);
        JSONArray level = config.getJSONArray("level");
        // 防止死循环，加个安全保护：最多升级20次
        int maxUpgradeTimes = 20;
        int upgradeCount = 0;
        while (true) {
            // 获取当前分销等级
            Salesman salesman = salesmanMapper.selectById(salesmanId);
            Integer levelId = salesman.getLevel();
            Integer nextLevelId = levelId + 1;
            JSONObject levelJson = null;
            for (int i = 0; i < level.size(); i++) {
                if (nextLevelId.equals(level.getJSONObject(i).getInt("id"))) {
                    levelJson = level.getJSONObject(i);
                }
            }
            // 下一级为空，直接跳过
            if (levelJson == null) {
                return;
            }
            BigDecimal salesAmount = null;
            Integer salesInviteUsers = null;
            BigDecimal selfBuyAmount = null;
            // 获取分销下一级等级
            JSONObject jsonObject = levelJson.getJSONObject("condition");
            if (jsonObject.getJSONObject("salesAmount").getBool("checked")) {
                //推广金额
                salesAmount = jsonObject.getJSONObject("salesAmount").getBigDecimal("value");
            }
            if (jsonObject.getJSONObject("salesInviteUsers").getBool("checked")) {
                //发展客户数
                salesInviteUsers = jsonObject.getJSONObject("salesInviteUsers").getInt("value");
            }
            if (jsonObject.getJSONObject("selfBuyAmount").getBool("checked")) {
                //自购金额
                selfBuyAmount = jsonObject.getJSONObject("selfBuyAmount").getBigDecimal("value");
            }
            // 获取拉新的客户数，判断是否满足下一级
            Long inviteUsers = salemanCustomerMapper.selectCount(new LambdaQueryWrapper<SalesmanCustomer>().eq(SalesmanCustomer::getSalesmanId, salesmanId));
            log.info("发展客户数：{}；升级发展客户数：{}", inviteUsers, salesInviteUsers);
            if (salesInviteUsers != null && inviteUsers < salesInviteUsers) {
                return;
            }
            // 获取自购金额，判断是否满足下一级
            BigDecimal selfBuy = orderService.list(new LambdaQueryWrapper<Order>().eq(Order::getUserId, salesman.getUserId()).eq(Order::getOrderStatus, OrderStatusEnum.COMPLETED.getCode()))
                    .stream()
                    .map(Order::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            log.info("自购金额：{}；升级自购金额：{}", selfBuy, selfBuyAmount);
            if (selfBuyAmount != null && selfBuy.compareTo(selfBuyAmount) < 0) {
                return;
            }
            // 获取分销金额数，判断是否满足下一级（推广金额口径与 sale_amount 一致，仅统计已结算订单）
            BigDecimal sales = salesmanOrderMapper.selectList(new LambdaQueryWrapper<SalesmanOrder>()
                            .eq(SalesmanOrder::getSalesmanId, salesmanId)
                            .eq(SalesmanOrder::getStatus, 1))
                    .stream()
                    .map(SalesmanOrder::getOrderAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            log.info("分销金额：{}；分销升级金额：{}", sales, salesAmount);
            if (salesAmount != null && sales.compareTo(salesAmount) < 0) {
                return;
            }
            salesman.setLevel(nextLevelId);
            salesmanMapper.updateById(salesman);
            upgradeCount++;
            if (upgradeCount > maxUpgradeTimes) {
                log.error("分销员升级超过 {} 次，可能存在死循环，salesmanId={}", maxUpgradeTimes, salesmanId);
                break;
            }
        }
    }

    @RabbitListener(queues = RabbitMQConfig.VIP_UPDATE_QUEUE)
    @RetryLimit
    public void vipUpdateMessage(Integer userId) {
        log.info("判断会员是否可以升级 ：{}", userId);
        UserRankConfig userRankConfig = userRankConfigMapper.selectOne(new LambdaQueryWrapper<UserRankConfig>().eq(UserRankConfig::getCode, "rank_config"));
        if (userRankConfig == null) {
            return;
        }
        List<UserRank> userRankList = userRankMapper.selectList(new LambdaQueryWrapper<>());
        // 获取当前等级
        User currentUser = userService.getById(userId);
        Integer rankId = currentUser.getRankId();
        Integer nextRankId = rankId + 1;
        UserRank nextRank = null;
        for (UserRank userRank : userRankList) {
            if (nextRankId.equals(userRank.getRankId())) {
                nextRank = userRank;
            }
        }
        // 下一级为空，直接跳过
        if (nextRank == null) {
            return;
        }
        if (userRankConfig.getRankType() == 1) {
            // 成长值升级逻辑
            if (BigDecimal.valueOf(currentUser.getGrowthPoints()).compareTo(nextRank.getMinGrowthPoints()) < 0) {
                return;
            }
        } else {
            // 消费金额升级逻辑
            if (currentUser.getOrderAmount().compareTo(nextRank.getMinGrowthPoints()) < 0) {
                return;
            }
        }

        currentUser.setRankId(nextRank.getRankId());
        userService.updateById(currentUser);
    }



    private void saveVendorAccountLog(Vendor vendor, BigDecimal paidAmount, Order order) {
        BigDecimal newVendorMoney = vendor.getVendorMoney().add(paidAmount).setScale(2, RoundingMode.HALF_UP);
        VendorAccountLog account = new VendorAccountLog();
        account.setVendorMoney(vendor.getVendorMoney());
        account.setVendorId(order.getVendorId());
        account.setFrozenMoney(vendor.getFrozenMoney());
        account.setNewVendorMoney(newVendorMoney);
        account.setNewFrozenMoney(vendor.getFrozenMoney());
        account.setType(4);
        account.setAddTime(StringUtils.getCurrentTime());
        vendorAccountLogMapper.insert(account);
    }

    private void saveDescVendorAccountLog(Vendor vendor, BigDecimal refundAmount, Order order) {
        BigDecimal newVendorMoney = vendor.getVendorMoney().subtract(refundAmount).setScale(2, RoundingMode.HALF_UP);
        VendorAccountLog account = new VendorAccountLog();
        account.setVendorMoney(vendor.getVendorMoney());
        account.setVendorId(order.getVendorId());
        account.setFrozenMoney(vendor.getFrozenMoney());
        account.setNewVendorMoney(newVendorMoney);
        account.setNewFrozenMoney(vendor.getFrozenMoney());
        account.setType(4);
        account.setAddTime(StringUtils.getCurrentTime());
        vendorAccountLogMapper.insert(account);
    }

    @RabbitListener(queues = RabbitMQConfig.PRINT_ORDER_QUEUE)
    @RetryLimit
    public void receivePrintOrderMessage(PrintOrderDTO printOrderDTO) {
        log.info("收到打印订单消息：{}", JSONUtil.toJsonStr(printOrderDTO));
        Print print = printOrderDTO.getPrint();
        if (print == null) {
            log.error("【错误】打印消息缺少 print 信息：{}", printOrderDTO);
            return;
        }

        // 1. 预写一条 PENDING(0) 日志，triggerType 由消息携带（默认 AUTO=1）
        Integer triggerType = printOrderDTO.getTriggerType() == null
                ? com.tigshop.bean.enums.print.PrintTriggerTypeEnum.AUTO.getCode()
                : printOrderDTO.getTriggerType();
        com.tigshop.bean.model.print.PrintLog logEntry = printLogService.createPending(
                print, printOrderDTO.getOrderId(), triggerType);

        try {
            // 2. 获取打印机配置（模板跨平台共用）
            PrintConfigVO configVO = printConfigService.getConfigsByPrintId(print.getPrintId());
            if (configVO == null) {
                log.error("【错误】未找到打印机配置：{}", print.getPrintId());
                printLogService.markFailed(logEntry.getLogId(), new IllegalStateException("未找到打印机配置"));
                return;
            }

            // 3. 按平台渲染打印内容（飞鹅：富文本标签；商米：UTF-8 hex + ESC/POS）
            //    configVO.paperSize 已经是 PrintPaperSizeEnum，renderer 内部全程持枚举
            String content = rendererFactory.of(print.getPlatform())
                    .render(printOrderDTO.getOrderId(),
                            configVO.getTemplate(),
                            configVO.getPaperSize());

            // 4. 设置打印联数
            Integer printNumber = print.getPrintNumber();
            if (printNumber == null || printNumber <= 0) {
                printNumber = 1;
            }

            // 5. 构造语音播报文本。仅当三个条件全部满足才生成 mediaText，任一不满足则传 null：
            //    - triggerType = AUTO（手动补打不播）
            //    - print.voiceEnabled = 1（打印机层级开关；null 兼容老数据按开启）
            //    - Order 查得到
            //
            //    文案与后台"已付款订单"（AdminMsg msgType=12）通知 title 同源，
            //    由 OrderAdminMsgTemplate 常量类统一维护，后台通知/打印语音不会出现文案漂移。
            //
            //    平台差异说明：
            //    - 商米：mediaText 非空时拼进 pushContent.media_text + cycle=1，机器 TTS 播报
            //    - 飞鹅：mediaText 非空时 content 开头拼 <PLUGIN>，触发外接音响/钱箱；
            //      云播报机型硬件自带喇叭会自动响（与 <PLUGIN> 无关）
            String mediaText = null;
            boolean isAuto = com.tigshop.bean.enums.print.PrintTriggerTypeEnum.AUTO.getCode().equals(triggerType);
            boolean voiceOn = print.getVoiceEnabled() == null || print.getVoiceEnabled() == 1;
            if (isAuto && voiceOn) {
                com.tigshop.bean.model.order.Order ord = orderService.getById(printOrderDTO.getOrderId());
                if (ord != null) {
                    // voiceContentType: null/0=订单号+金额，1=仅金额
                    mediaText = com.tigshop.common.constant.msg.OrderAdminMsgTemplate
                            .paidOrderVoice(ord.getOrderSn(), ord.getTotalAmount(), print.getVoiceContentType());
                }
            }

            // 6. 按平台调用适配器推送（mediaText：商米拼进 pushContent.media_text；飞鹅忽略）
            com.tigshop.bean.dto.print.adapter.PushResult result = printAdapterFactory.of(print.getPlatform())
                    .push(print, String.valueOf(printOrderDTO.getOrderId()), content, printNumber, mediaText);

            // 7. 回写日志（PUSHED=1 / PUSH_FAILED=3 / OFFLINE=4）
            printLogService.markPushed(logEntry.getLogId(), result);
            if (result != null && result.isSuccess()) {
                log.info("打印订单成功：订单ID={}, 打印机={}, 平台={}",
                        printOrderDTO.getOrderId(), print.getPrintName(), print.getPlatform());
            } else {
                log.error("【错误】推送打印任务失败：平台={}, 打印机={}, 订单={}, resp={}",
                        print.getPlatform(), print.getPrintName(), printOrderDTO.getOrderId(),
                        result == null ? "null" : result.getRawResponse());
            }
        } catch (Exception e) {
            log.error("【错误】处理打印任务异常：订单={}", printOrderDTO.getOrderId(), e);
            printLogService.markFailed(logEntry.getLogId(), e);
            // 不 rethrow：打印失败不影响支付主流程（REQ-011）；@RetryLimit 对已记录失败不再重复拉起
        }
    }

    // ===== 原 generate* 小票渲染私有方法已迁移至 FeieyunReceiptRenderer（逻辑零修改）=====
    // 参考：tigshop-service/.../print/renderer/impl/FeieyunReceiptRenderer.java


    @RabbitListener(queues = RabbitMQConfig.GENERATE_STATEMENT_QUEUE)
    @RetryLimit
    public void generateStatement(StatementSaveParam param) {
        statementService.saveStatement(param);
    }
}
