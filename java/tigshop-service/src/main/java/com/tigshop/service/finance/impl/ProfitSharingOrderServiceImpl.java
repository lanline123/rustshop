package com.tigshop.service.finance.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.enums.finance.ProfitSharingFinishStatus;
import com.tigshop.bean.enums.finance.ProfitSharingStatus;
import com.tigshop.bean.enums.finance.ProfitSharingTriggerType;
import com.tigshop.bean.enums.finance.StatementType;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.dto.finance.profitsharing.CreateProfitSharingOrderRequest;
import com.tigshop.bean.dto.finance.profitsharing.FinishProfitSharingRequest;
import com.tigshop.bean.dto.finance.profitsharing.ProfitSharingReturnRequest;
import com.tigshop.bean.dto.finance.profitsharing.ProfitSharingOrderResult;
import com.tigshop.bean.dto.finance.profitsharing.ProfitSharingReturnResult;
import com.tigshop.bean.model.finance.Paylog;
import com.tigshop.bean.model.finance.PaylogRefund;
import com.tigshop.bean.model.finance.StatementOutput;
import com.tigshop.bean.model.finance.ProfitSharingOrder;
import com.tigshop.bean.model.finance.ProfitSharingReturnOrder;
import com.tigshop.bean.model.finance.SubMerchant;
import com.tigshop.bean.model.finance.Statement;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.mapper.finance.ProfitSharingReturnOrderMapper;
import com.tigshop.mapper.finance.PaylogRefundMapper;
import com.tigshop.mapper.finance.StatementOutputMapper;
import com.tigshop.mapper.finance.StatementMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.PaylogService;
import com.tigshop.service.finance.ProfitSharingOrderService;
import com.tigshop.service.finance.SubMerchantService;
import com.tigshop.service.finance.profitsharing.ProfitSharingClientRegistry;
import com.tigshop.service.salesman.SalesmanOrderService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.shop.AccountService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 微信线上分账业务 Service 实现
 *
 * <p>单体场景：每订单一条分账单，服务费取自该订单 statement 表 SERVICE_FEE 分录，
 * transactionId 取自 paylog。与微服务每订单创建分账单的核心逻辑保持一致。</p>
 *
 * @author Tigshop团队
 */
@Service
@Slf4j
public class ProfitSharingOrderServiceImpl
        extends BaseServiceImpl<com.tigshop.mapper.finance.ProfitSharingOrderMapper, ProfitSharingOrder>
        implements ProfitSharingOrderService {

    private static final String PAY_CHANNEL_WECHAT = "WECHAT";
    private static final String PAY_CHANNEL_HUIFU = "HUIFU";
    private static final String PAY_CHANNEL_ALIPAY = "ALIPAY";
    private static final BigDecimal MAX_SPLIT_RATIO = new BigDecimal("0.30");
    /** 结算主体类型：店铺 */
    private static final int SETTLEMENT_TYPE_SHOP = 1;
    /** 结算主体类型：供应商 */
    private static final int SETTLEMENT_TYPE_VENDOR = 2;

    /** 回退单状态：0 待处理 1 处理中 2 成功 3 失败 */
    private static final int RETURN_STATUS_PENDING = 0;
    private static final int RETURN_STATUS_PROCESSING = 1;
    private static final int RETURN_STATUS_SUCCESS = 2;
    private static final int RETURN_STATUS_FAILED = 3;
    /**
     * 已占用额度的回退单状态：待处理 / 处理中 / 成功。
     * 失败单不占额度，允许按同一退款申请重新发起。
     */
    private static final List<Integer> RETURN_STATUS_OCCUPIED =
            List.of(RETURN_STATUS_PENDING, RETURN_STATUS_PROCESSING, RETURN_STATUS_SUCCESS);

    private final OrderMapper orderMapper;
    private final ShopMapper shopMapper;
    private final com.tigshop.mapper.vendor.VendorMapper vendorMapper;
    private final StatementMapper statementMapper;
    private final SubMerchantService subMerchantService;
    private final PaylogService paylogService;
    private final ProfitSharingReturnOrderMapper profitSharingReturnOrderMapper;
    private final ProfitSharingClientRegistry profitSharingClientRegistry;
    private final ConfigService configService;
    private final SalesmanOrderService salesmanOrderService;
    private final PaylogRefundMapper paylogRefundMapper;
    private final StatementOutputMapper statementOutputMapper;
    private final TransactionTemplate orderOperationTxTemplate;
    @Resource
    private AccountService accountService;

    /**
     * 回退单落库与状态回写专用的独立事务。
     * <p>退款审核 {@code RefundApplyServiceImpl.audit} 自身是 {@code @Transactional}，回退方法无论是否
     * 标注 {@code @Transactional}（默认 REQUIRED）都只会加入该事务；一旦后续任一笔回退失败抛出，
     * 前面已在渠道侧真实退还的资金记录会被一并回滚，形成"钱动了但无据可查"，重试还会再退一次。
     * 因此这里必须用 REQUIRES_NEW 让记录脱离外层事务独立提交。
     */
    private final TransactionTemplate returnOrderTxTemplate;

    public ProfitSharingOrderServiceImpl(OrderMapper orderMapper,
                                         ShopMapper shopMapper,
                                         com.tigshop.mapper.vendor.VendorMapper vendorMapper,
                                         StatementMapper statementMapper,
                                         SubMerchantService subMerchantService,
                                         PaylogService paylogService,
                                         ProfitSharingReturnOrderMapper profitSharingReturnOrderMapper,
                                         ProfitSharingClientRegistry profitSharingClientRegistry,
                                         ConfigService configService,
                                         SalesmanOrderService salesmanOrderService,
                                         StatementOutputMapper statementOutputMapper,
                                         PaylogRefundMapper paylogRefundMapper,
                                         PlatformTransactionManager transactionManager) {
        this.orderMapper = orderMapper;
        this.shopMapper = shopMapper;
        this.vendorMapper = vendorMapper;
        this.statementMapper = statementMapper;
        this.subMerchantService = subMerchantService;
        this.paylogService = paylogService;
        this.profitSharingReturnOrderMapper = profitSharingReturnOrderMapper;
        this.profitSharingClientRegistry = profitSharingClientRegistry;
        this.configService = configService;
        this.salesmanOrderService = salesmanOrderService;
        this.paylogRefundMapper = paylogRefundMapper;
        this.statementOutputMapper = statementOutputMapper;
        this.orderOperationTxTemplate = new TransactionTemplate(transactionManager);
        this.returnOrderTxTemplate = new TransactionTemplate(transactionManager);
        this.returnOrderTxTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    // ==================== 列表 ====================

    @Override
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.tigshop.bean.vo.finance.ProfitSharingOrderVO> listPage(
            com.tigshop.bean.query.finance.ProfitSharingPageQuery query) {
        Page<ProfitSharingOrder> pageParam = buildSortOrder(query);
        LambdaQueryWrapper<ProfitSharingOrder> wrapper = new LambdaQueryWrapper<>();
        if (query.getSettlementType() != null) {
            wrapper.eq(ProfitSharingOrder::getSettlementType, query.getSettlementType());
        }
        if (query.getShopId() != null) {
            wrapper.eq(ProfitSharingOrder::getShopId, query.getShopId().longValue());
        }
        if (query.getVendorId() != null) {
            wrapper.eq(ProfitSharingOrder::getVendorId, query.getVendorId().longValue());
        }
        Integer statusCode = parseStatus(query.getStatus());
        if (statusCode != null) {
            wrapper.eq(ProfitSharingOrder::getStatus, statusCode);
        }
        Integer finishCode = parseFinishStatus(query.getFinishStatus());
        if (finishCode != null) {
            wrapper.eq(ProfitSharingOrder::getFinishStatus, finishCode);
        }
        Page<ProfitSharingOrder> entityPage = this.page(pageParam, wrapper);

        Page<com.tigshop.bean.vo.finance.ProfitSharingOrderVO> voPage =
                new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<com.tigshop.bean.vo.finance.ProfitSharingOrderVO> voList =
                entityPage.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        fillShopNames(voList);
        fillVendorNames(voList);
        voPage.setRecords(voList);
        return voPage;
    }

    // ==================== 批量发起 ====================

    @Override
    public void submitBatchProfitSharing(List<Long> orderIds, Integer triggerType, Long operatorId) {
        if (orderIds == null || orderIds.isEmpty()) {
            throw new GlobalException("订单ID列表不能为空");
        }
        int triggerCode = triggerType != null ? triggerType : ProfitSharingTriggerType.MANUAL.getCode();

        int successCount = 0;
        int skipCount = 0;
        List<String> errors = new ArrayList<>();

        for (Long orderId : orderIds) {
            try {
                boolean processed = withOrderLock(orderId,
                        () -> processSingleOrder(orderId, triggerCode, operatorId));
                if (processed) {
                    successCount++;
                } else {
                    skipCount++;
                }
            } catch (Exception e) {
                log.error("店铺分账处理失败, orderId={}", orderId, e);
                errors.add("订单[" + orderId + "]店铺分账: " + e.getMessage());
            }
            // 供应商分账：同一订单若存在供应商服务费分录，按订单所属店铺子商户再发起一笔供应商分账单
            try {
                boolean vendorProcessed = withOrderLock(orderId,
                        () -> processVendorProfitSharing(orderId, triggerCode, operatorId));
                if (vendorProcessed) {
                    successCount++;
                } else {
                    skipCount++;
                }
            } catch (Exception e) {
                log.error("供应商分账处理失败, orderId={}", orderId, e);
                errors.add("订单[" + orderId + "]供应商分账: " + e.getMessage());
            }
        }

        log.info("批量分账完成: 总数={}, 已处理={}, 跳过={}, 失败={}",
                orderIds.size(), successCount, skipCount, errors.size());

        if (!errors.isEmpty()) {
            throw new GlobalException("分账失败（已处理" + successCount + "笔，跳过" + skipCount + "笔）: "
                    + String.join("; ", errors));
        }
    }

    private <T> T withOrderLock(Long orderId, Supplier<T> operation) {
        return orderOperationTxTemplate.execute(status -> {
            Order locked = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                    .eq(Order::getOrderId, orderId).last("FOR UPDATE"));
            if (locked == null) {
                throw new GlobalException("订单不存在: " + orderId);
            }
            return operation.get();
        });
    }

    private boolean prepareWechatNetSplit(ProfitSharingOrder order) {
        if (!PAY_CHANNEL_WECHAT.equalsIgnoreCase(
                StrUtil.blankToDefault(order.getPayChannel(), PAY_CHANNEL_WECHAT))) {
            return true;
        }
        return Boolean.TRUE.equals(returnOrderTxTemplate.execute(status -> {
            LambdaQueryWrapper<PaylogRefund> query = new LambdaQueryWrapper<PaylogRefund>()
                    .eq(PaylogRefund::getOrderId, order.getOrderId())
                    .eq(PaylogRefund::getPayCode, "wechat")
                    .in(PaylogRefund::getStatus, 0, 1)
                    .gt(PaylogRefund::getRefundAmount, BigDecimal.ZERO);
            Long count = paylogRefundMapper.selectCount(query);
            if (count == null) {
                throw new GlobalException("无法确认退款状态，暂不能分账");
            }
            if (count == 0) {
                return true;
            }
            // 失败单可能已向渠道发送，不能复用旧单号修改金额；未知结果仍必须先查询。
            if (ProfitSharingStatus.FAILED.getCode().equals(order.getStatus())) {
                throw new GlobalException("订单已有退款，历史失败分账须先核对渠道结果，不能用原单号修改金额重试");
            }
            List<PaylogRefund> refunds = paylogRefundMapper.selectList(query);
            if (refunds.isEmpty() || refunds.stream().anyMatch(r -> !Integer.valueOf(1).equals(r.getStatus()))) {
                throw new GlobalException("订单退款处理中或结果待确认，暂不能分账");
            }
            if (paylogRefundMapper.countUnsettledSuccessfulRefunds(order.getOrderId()) > 0) {
                throw new GlobalException("退款已到账但售后账务尚未完成，暂不能按净额分账");
            }
            Paylog paylog = paylogService.getPaidProfitSharingPaylog(order.getOrderId().intValue());
            if (paylog == null || paylog.getPaylogId() == null
                    || refunds.stream().anyMatch(r -> !paylog.getPaylogId().equals(r.getPaylogId()))) {
                throw new GlobalException("退款与分账原支付流水无法唯一匹配，请核对后重试");
            }
            BigDecimal refunded = refunds.stream().map(PaylogRefund::getRefundAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal total = order.getTotalAmount();
            if (total == null || total.signum() <= 0 || paylog.getPayAmount() == null
                    || total.compareTo(paylog.getPayAmount()) != 0) {
                throw new GlobalException("分账原交易金额缺失或不一致，请核对后重试");
            }
            BigDecimal base = total.subtract(refunded);
            if (base.signum() <= 0) {
                return false;
            }
            if (order.getSettlementType() == null || order.getShopId() == null || StrUtil.isBlank(order.getOrderSn())) {
                throw new GlobalException("分账主体或原订单号缺失，不能推算净额");
            }
            long shopId = SETTLEMENT_TYPE_VENDOR == order.getSettlementType() ? 0L : order.getShopId();
            long vendorId = SETTLEMENT_TYPE_VENDOR == order.getSettlementType() ? order.getVendorId() : 0L;
            List<StatementOutput> original = statementOutputMapper.selectList(new LambdaQueryWrapper<StatementOutput>()
                    .eq(StatementOutput::getRecordId, order.getOrderId())
                    .eq(StatementOutput::getRecordSn, order.getOrderSn())
                    .eq(StatementOutput::getRecordType, StatementType.SERVICE_FEE.getCode())
                    .eq(StatementOutput::getShopId, shopId).eq(StatementOutput::getVendorId, vendorId));
            if (original.isEmpty()) {
                throw new GlobalException("缺少原始服务费出账，不能推算退款后的净分账金额");
            }
            if (statementOutputMapper.countMissingConfirmedRefundFees(order.getOrderId(), shopId, vendorId,
                    StatementType.SERVICE_FEE.getCode()) > 0) {
                throw new GlobalException("退款服务费冲回出账不完整，暂不能按净额分账");
            }
            BigDecimal originalFee = original.stream().map(StatementOutput::getExpenditure)
                    .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal reversedFee = statementOutputMapper.selectConfirmedRefundFees(order.getOrderId(),
                            shopId, vendorId, StatementType.SERVICE_FEE.getCode()).stream()
                    .map(StatementOutput::getIncome).filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal fee = originalFee.subtract(reversedFee).max(BigDecimal.ZERO);
            BigDecimal commission = vendorId > 0 ? BigDecimal.ZERO
                    : salesmanOrderService.getTotalCommissionByOrderId(order.getOrderId().intValue());
            commission = commission == null ? BigDecimal.ZERO : commission;
            order.setServiceFee(fee);
            order.setCommissionFee(commission);
            order.setSplitAmount(fee.add(commission));
            // 原交易总额不变，另存净基数供后续退款回退使用，不能再拿原交易额作分母。
            order.setSharingBaseAmount(base);
            order.setServiceFeeRate(fee.multiply(new BigDecimal("100")).divide(base, 4, RoundingMode.HALF_UP));
            return order.getSplitAmount().signum() > 0;
        }));
    }

    @Override
    public void retryProfitSharing(Long profitSharingOrderId) {
        ProfitSharingOrder candidate = getById(profitSharingOrderId);
        if (candidate == null) {
            return;
        }
        withOrderLock(candidate.getOrderId(), () -> {
            ProfitSharingOrder current = returnOrderTxTemplate.execute(status -> getById(profitSharingOrderId));
            if (current == null || !ProfitSharingStatus.FAILED.getCode().equals(current.getStatus())
                    || (current.getRetryCount() != null && current.getRetryCount() >= 3)
                    || StrUtil.hasBlank(current.getTransactionId(), current.getSubMchId())) {
                return false;
            }
            current.setRetryCount((current.getRetryCount() == null ? 0 : current.getRetryCount()) + 1);
            callCreateOrder(current, current.getSplitAmount(), true);
            return true;
        });
    }

    private boolean processSingleOrder(Long orderId, int triggerCode, Long operatorId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new GlobalException("订单不存在: " + orderId);
        }
        Integer shopId = order.getShopId();
        if (shopId == null || shopId <= 0) {
            log.info("订单[{}]非店铺订单, 跳过分账", orderId);
            return false;
        }

        long existing = returnOrderTxTemplate.execute(status -> lambdaQuery()
                .eq(ProfitSharingOrder::getOrderId, orderId)
                .eq(ProfitSharingOrder::getSettlementType, SETTLEMENT_TYPE_SHOP)
                .count());
        if (existing > 0) {
            // 已有店铺分账记录，推进已存在的 PENDING 单
            return advanceExistingPending(orderId, SETTLEMENT_TYPE_SHOP);
        }

        BigDecimal serviceFee = sumServiceFee(orderId.intValue(), shopId);
        // 分账金额 = 服务费 + 分销佣金（平台从二级户收回垫付佣金，店铺真正承担）
        BigDecimal commissionFee = salesmanOrderService.getTotalCommissionByOrderId(orderId.intValue());
        if (commissionFee == null) {
            commissionFee = BigDecimal.ZERO;
        }
        BigDecimal splitAmount = serviceFee.add(commissionFee);
        if (splitAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("订单[{}]服务费+佣金为0, 跳过分账", orderId);
            return false;
        }

        // 只认真正进了二级商户的那笔收款（profit_sharing_channel 仅在服务商/合单支付时写入）。
        // 余额、线下、直连微信都不会命中，这类订单的服务费已在入账环节从 shop_money 扣除，
        // 再建分账单等于向空账户抽钱，只会挂成永远等不到交易号的 PENDING。
        Paylog paylog = paylogService.getPaidProfitSharingPaylog(orderId.intValue());
        if (paylog == null) {
            log.info("订单[{}]无线上分账渠道收款, 跳过分账", orderId);
            return false;
        }
        String payChannel = resolvePayChannel(paylog);
        SubMerchant subMerchant = subMerchantService.getActiveByShop(shopId.longValue(), payChannel);
        if (subMerchant == null) {
            throw new GlobalException("店铺[" + shopId + "]未绑定有效的" + payChannel + "子商户");
        }

        String transactionId = paylog.getTransactionId();
        BigDecimal payAmount = paylog.getPayAmount();
        String orderSn = order.getOrderSn();

        if (!PAY_CHANNEL_WECHAT.equalsIgnoreCase(payChannel)
                && payAmount != null && payAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal ratio = splitAmount.divide(payAmount, 4, RoundingMode.HALF_UP);
            BigDecimal maxRatio = resolveMaxSplitRatio(subMerchant, payChannel);
            if (ratio.compareTo(maxRatio) > 0) {
                log.error("订单[{}]分账比例超过上限{}: splitAmount={}(serviceFee={}, commission={}), payAmount={}",
                        orderId, maxRatio, splitAmount, serviceFee, commissionFee, payAmount);
                throw new GlobalException("订单[" + orderId + "]分账比例超过" + maxRatio.multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString() + "%");
            }
        }

        String outOrderNo = "PS_" + orderId + "_" + System.currentTimeMillis();

        ProfitSharingOrder ps = new ProfitSharingOrder();
        ps.setOutOrderNo(outOrderNo);
        ps.setOrderId(orderId);
        ps.setOrderSn(orderSn);
        ps.setSettlementId(orderId);
        ps.setShopId(shopId.longValue());
        ps.setVendorId(0L);
        ps.setSettlementType(SETTLEMENT_TYPE_SHOP);
        ps.setSubMchId(subMerchant.getSubMchId());
        ps.setPayChannel(payChannel);
        ps.setTransactionId(transactionId);
        ps.setTotalAmount(payAmount);
        ps.setSplitAmount(splitAmount);
        ps.setServiceFee(serviceFee);
        ps.setCommissionFee(commissionFee);
        ps.setServiceFeeRate(payAmount != null && payAmount.compareTo(BigDecimal.ZERO) > 0
                ? serviceFee.divide(payAmount, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO);
        ps.setTriggerType(triggerCode);
        ps.setOperatorId(operatorId);
        ps.setRetryCount(0);
        ps.setCreateTime(new Date());
        ps.setModifyTime(new Date());

        if (StrUtil.isBlank(transactionId)) {
            if (!prepareWechatNetSplit(ps)) {
                return false;
            }
            ps.setStatus(ProfitSharingStatus.PENDING.getCode());
            ps.setFinishStatus(ProfitSharingFinishStatus.UNFINISHED.getCode());
            ps.setResultDesc("缺少transactionId, 等待补充后发起");
            save(ps);
            log.warn("订单[{}]缺少transactionId, 分账单已创建但未发起", orderId);
            return true;
        }

        if (!callCreateOrder(ps, splitAmount, true)) {
            return false;
        }
        log.info("分账单已创建: outOrderNo={}, orderId={}, splitAmount={}(serviceFee={}, commission={}), status={}",
                outOrderNo, orderId, splitAmount, serviceFee, commissionFee, ps.getStatus());
        return true;
    }

    /**
     * 处理单个订单的供应商微信线上分账。
     *
     * <p>供应商无独立子商户，分账从「订单所属店铺」的子商户同一笔交易中再切一刀，
     * 因此供应商分账单的 subMchId / transactionId 与店铺一致，仅 settlementType=供应商、vendorId 不同。</p>
     */
    private boolean processVendorProfitSharing(Long orderId, int triggerCode, Long operatorId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new GlobalException("订单不存在: " + orderId);
        }
        Integer vendorId = order.getVendorId();
        if (vendorId == null || vendorId <= 0) {
            // 非供应商订单，无供应商分账
            return false;
        }
        Integer shopId = order.getShopId();
        if (shopId == null || shopId <= 0) {
            log.info("订单[{}]供应商分账缺少所属店铺，无法发起线上分账, 跳过", orderId);
            return false;
        }

        long existing = returnOrderTxTemplate.execute(status -> lambdaQuery()
                .eq(ProfitSharingOrder::getOrderId, orderId)
                .eq(ProfitSharingOrder::getSettlementType, SETTLEMENT_TYPE_VENDOR)
                .count());
        if (existing > 0) {
            return advanceExistingPending(orderId, SETTLEMENT_TYPE_VENDOR);
        }

        BigDecimal serviceFee = sumVendorServiceFee(orderId.intValue(), vendorId);
        if (serviceFee.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("订单[{}]供应商服务费为0, 跳过供应商分账", orderId);
            return false;
        }

        // 与店铺分账同口径：无线上分账渠道收款则跳过，见 processSingleOrder 处说明
        Paylog paylog = paylogService.getPaidProfitSharingPaylog(orderId.intValue());
        if (paylog == null) {
            log.info("订单[{}]无线上分账渠道收款, 跳过供应商分账", orderId);
            return false;
        }
        String payChannel = resolvePayChannel(paylog);
        SubMerchant subMerchant = subMerchantService.getActiveByShop(shopId.longValue(), payChannel);
        if (subMerchant == null) {
            throw new GlobalException("订单所属店铺[" + shopId + "]未绑定有效的" + payChannel + "子商户，供应商无法分账");
        }

        String transactionId = paylog.getTransactionId();
        BigDecimal payAmount = paylog.getPayAmount();
        String orderSn = order.getOrderSn();

        // 组合护栏：同一笔交易的店铺+供应商累计分账比例不得超过渠道上限
        if (!PAY_CHANNEL_WECHAT.equalsIgnoreCase(payChannel)
                && payAmount != null && payAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal maxRatio = resolveMaxSplitRatio(subMerchant, payChannel);
            BigDecimal existingSplit = sumOrderActiveSplitAmount(orderId);
            BigDecimal combinedRatio = existingSplit.add(serviceFee).divide(payAmount, 4, RoundingMode.HALF_UP);
            BigDecimal vendorRatio = serviceFee.divide(payAmount, 4, RoundingMode.HALF_UP);
            if (vendorRatio.compareTo(maxRatio) > 0 || combinedRatio.compareTo(maxRatio) > 0) {
                log.error("订单[{}]供应商分账比例超限: vendorFee={}, existingSplit={}, payAmount={}, combinedRatio={}, maxRatio={}",
                        orderId, serviceFee, existingSplit, payAmount, combinedRatio, maxRatio);
                ProfitSharingOrder failed = buildVendorProfitSharingOrder(order, vendorId, shopId, subMerchant,
                        transactionId, payAmount, serviceFee, triggerCode, operatorId);
                failed.setStatus(ProfitSharingStatus.FAILED.getCode());
                failed.setFinishStatus(ProfitSharingFinishStatus.UNFINISHED.getCode());
                failed.setResultDesc("供应商分账累计比例超过上限，已拦截(combinedRatio=" + combinedRatio + ",max=" + maxRatio + ")");
                save(failed);
                throw new GlobalException(failed.getResultDesc());
            }
        }

        ProfitSharingOrder ps = buildVendorProfitSharingOrder(order, vendorId, shopId, subMerchant,
                transactionId, payAmount, serviceFee, triggerCode, operatorId);

        if (StrUtil.isBlank(transactionId)) {
            if (!prepareWechatNetSplit(ps)) {
                return false;
            }
            ps.setStatus(ProfitSharingStatus.PENDING.getCode());
            ps.setFinishStatus(ProfitSharingFinishStatus.UNFINISHED.getCode());
            ps.setResultDesc("缺少transactionId, 等待补充后发起");
            save(ps);
            log.warn("订单[{}]供应商分账缺少transactionId, 分账单已创建但未发起", orderId);
            return true;
        }

        if (!callCreateOrder(ps, serviceFee, true)) {
            return false;
        }
        log.info("供应商分账单已创建: outOrderNo={}, orderId={}, vendorId={}, serviceFee={}, status={}",
                ps.getOutOrderNo(), orderId, vendorId, serviceFee, ps.getStatus());
        return true;
    }

    private ProfitSharingOrder buildVendorProfitSharingOrder(Order order, Integer vendorId, Integer shopId,
                                                             SubMerchant subMerchant, String transactionId,
                                                             BigDecimal payAmount, BigDecimal serviceFee,
                                                             int triggerCode, Long operatorId) {
        Long orderId = order.getOrderId() == null ? null : order.getOrderId().longValue();
        ProfitSharingOrder ps = new ProfitSharingOrder();
        ps.setOutOrderNo("PSV_" + orderId + "_" + System.currentTimeMillis());
        ps.setOrderId(orderId);
        ps.setOrderSn(order.getOrderSn());
        ps.setSettlementId(orderId);
        ps.setShopId(shopId.longValue());
        ps.setVendorId(vendorId.longValue());
        ps.setSettlementType(SETTLEMENT_TYPE_VENDOR);
        ps.setSubMchId(subMerchant.getSubMchId());
        ps.setPayChannel(StrUtil.blankToDefault(subMerchant.getPayChannel(), PAY_CHANNEL_WECHAT));
        ps.setTransactionId(transactionId);
        ps.setTotalAmount(payAmount);
        ps.setSplitAmount(serviceFee);
        ps.setServiceFee(serviceFee);
        ps.setCommissionFee(BigDecimal.ZERO);
        ps.setServiceFeeRate(payAmount != null && payAmount.compareTo(BigDecimal.ZERO) > 0
                ? serviceFee.divide(payAmount, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO);
        ps.setTriggerType(triggerCode);
        ps.setOperatorId(operatorId);
        ps.setRetryCount(0);
        ps.setCreateTime(new Date());
        ps.setModifyTime(new Date());
        return ps;
    }

    /**
     * 同一订单已发起、未失败的分账金额合计（用于供应商分账组合比例护栏）。
     */
    private BigDecimal sumOrderActiveSplitAmount(Long orderId) {
        // 失败与作废的单都未占用渠道额度，不能计入，否则会压缩供应商分账的可用比例
        List<ProfitSharingOrder> orders = lambdaQuery()
                .eq(ProfitSharingOrder::getOrderId, orderId)
                .notIn(ProfitSharingOrder::getStatus,
                        ProfitSharingStatus.FAILED.getCode(), ProfitSharingStatus.CANCELLED.getCode())
                .list();
        return orders.stream()
                .map(o -> o.getSplitAmount() != null ? o.getSplitAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String resolvePayChannel(Paylog paylog) {
        String payCode = paylog == null ? null : paylog.getPayCode();
        if (StrUtil.isNotBlank(payCode)) {
            String lower = payCode.toLowerCase();
            if (lower.startsWith("huifu")) {
                return PAY_CHANNEL_HUIFU;
            }
            if (lower.startsWith("alipay") || lower.startsWith("ali")) {
                return PAY_CHANNEL_ALIPAY;
            }
        }
        return PAY_CHANNEL_WECHAT;
    }

    /**
     * 微信硬上限 30%；汇付/支付宝按子商户开通比例，缺省回退各自配置默认值（支付宝 30%、汇付 20%）。
     */
    private BigDecimal resolveMaxSplitRatio(SubMerchant subMerchant, String payChannel) {
        if (PAY_CHANNEL_HUIFU.equalsIgnoreCase(payChannel) || PAY_CHANNEL_ALIPAY.equalsIgnoreCase(payChannel)) {
            if (subMerchant != null && subMerchant.getMaxSplitRatio() != null
                    && subMerchant.getMaxSplitRatio().compareTo(BigDecimal.ZERO) > 0) {
                return subMerchant.getMaxSplitRatio().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            }
            SettingsEnum ratioKey = PAY_CHANNEL_ALIPAY.equalsIgnoreCase(payChannel)
                    ? SettingsEnum.ALIPAY_PROFIT_SHARING_MAX_RATIO
                    : SettingsEnum.HUIFU_PROFIT_SHARING_MAX_RATIO;
            String cfg = configService.getConfigVal(ratioKey);
            try {
                if (StrUtil.isNotBlank(cfg)) {
                    return new BigDecimal(cfg.trim()).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                }
            } catch (Exception ignored) {
                // fall through
            }
            // 支付宝兜底与官方上限一致 30%，汇付兜底 20%
            return PAY_CHANNEL_ALIPAY.equalsIgnoreCase(payChannel)
                    ? new BigDecimal("0.30")
                    : new BigDecimal("0.20");
        }
        return MAX_SPLIT_RATIO;
    }

    private com.tigshop.service.finance.profitsharing.ProfitSharingApiClient clientFor(ProfitSharingOrder order) {
        return profitSharingClientRegistry.get(StrUtil.blankToDefault(order.getPayChannel(), PAY_CHANNEL_WECHAT));
    }

    private boolean advanceExistingPending(Long orderId, int settlementType) {
        ProfitSharingOrder ps = returnOrderTxTemplate.execute(status -> lambdaQuery()
                .eq(ProfitSharingOrder::getOrderId, orderId)
                .eq(ProfitSharingOrder::getSettlementType, settlementType)
                .eq(ProfitSharingOrder::getStatus, ProfitSharingStatus.PENDING.getCode())
                .orderByDesc(ProfitSharingOrder::getId)
                .last("limit 1")
                .one());
        if (ps == null) {
            log.info("订单[{}]已有分账记录且无待发起单(settlementType={}), 跳过", orderId, settlementType);
            return false;
        }
        if (StrUtil.isBlank(ps.getTransactionId())) {
            // 补交易号只能取分账渠道那笔：混合支付下按时间取最新会撞到余额 paylog
            Paylog paylog = paylogService.getPaidProfitSharingPaylog(orderId.intValue());
            if (paylog == null || StrUtil.isBlank(paylog.getTransactionId())) {
                throw new GlobalException("订单[" + orderId + "]缺少渠道交易号，无法发起分账");
            }
            ps.setTransactionId(paylog.getTransactionId());
            if (ps.getTotalAmount() == null) {
                ps.setTotalAmount(paylog.getPayAmount());
            }
        }
        return callCreateOrder(ps, ps.getSplitAmount(), true);
    }

    /** 失败结果先持久化，再交给批量入口汇总，不能把失败建单计为处理成功。 */
    private void throwIfProfitSharingFailed(ProfitSharingOrder order) {
        if (Objects.equals(order.getStatus(), ProfitSharingStatus.FAILED.getCode())) {
            throw new GlobalException(StrUtil.blankToDefault(order.getResultDesc(), "分账渠道处理失败"));
        }
    }

    private boolean callCreateOrder(ProfitSharingOrder ps, BigDecimal splitAmount, boolean finish) {
        if (!prepareWechatNetSplit(ps)) {
            if (ps.getId() != null) {
                ps.setResultDesc("退款后无可分账金额，待分账单已作废");
                ps.setStatus(ProfitSharingStatus.CANCELLED.getCode());
                ps.setFinishStatus(ProfitSharingFinishStatus.FINISHED.getCode());
                ps.setModifyTime(new Date());
                Boolean cancelled = returnOrderTxTemplate.execute(status -> update(ps,
                        new LambdaQueryWrapper<ProfitSharingOrder>().eq(ProfitSharingOrder::getId, ps.getId())
                                .eq(ProfitSharingOrder::getStatus, ProfitSharingStatus.PENDING.getCode())));
                if (!Boolean.TRUE.equals(cancelled)) {
                    throw new GlobalException("分账状态已变化，请刷新后重试");
                }
            }
            return false;
        }
        splitAmount = ps.getSplitAmount();
        if (PAY_CHANNEL_WECHAT.equalsIgnoreCase(StrUtil.blankToDefault(ps.getPayChannel(), PAY_CHANNEL_WECHAT))) {
            BigDecimal base = ps.getSharingBaseAmount() == null ? ps.getTotalAmount() : ps.getSharingBaseAmount();
            if (base != null && base.signum() > 0) {
                List<ProfitSharingOrder> otherOrders = returnOrderTxTemplate.execute(status -> lambdaQuery()
                                .eq(ProfitSharingOrder::getOrderId, ps.getOrderId())
                                .ne(ps.getId() != null, ProfitSharingOrder::getId, ps.getId())
                                .in(ProfitSharingOrder::getStatus, ProfitSharingStatus.PROCESSING.getCode(),
                                        ProfitSharingStatus.SUCCESS.getCode()).list());
                BigDecimal otherSplit = otherOrders.stream().map(ProfitSharingOrder::getSplitAmount)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                // 渠道比例上限按原订单总额计算，退款不应把该上限误缩为净额的30%。
                if (splitAmount.add(otherSplit).compareTo(ps.getTotalAmount().multiply(MAX_SPLIT_RATIO)) > 0) {
                    throw new GlobalException("订单累计分账比例超过30%，已拦截");
                }
                BigDecimal retained = BigDecimal.ZERO;
                for (ProfitSharingOrder other : otherOrders) {
                    BigDecimal confirmedReturn = returnOrderTxTemplate.execute(status ->
                                    profitSharingReturnOrderMapper.selectList(new LambdaQueryWrapper<ProfitSharingReturnOrder>()
                                            .eq(ProfitSharingReturnOrder::getProfitSharingOrderId, other.getId())
                                            .eq(ProfitSharingReturnOrder::getStatus, RETURN_STATUS_SUCCESS))).stream()
                            .map(ProfitSharingReturnOrder::getReturnAmount).filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    retained = retained.add(other.getSplitAmount().subtract(confirmedReturn).max(BigDecimal.ZERO));
                }
                if (splitAmount.add(retained).compareTo(base) > 0) {
                    throw new GlobalException("累计净分账金额超过退款后剩余付款，已拦截");
                }
            }
        }
        CreateProfitSharingOrderRequest request = new CreateProfitSharingOrderRequest();
        request.setSubMchId(ps.getSubMchId());
        request.setTransactionId(ps.getTransactionId());
        // 汇付跨天分账定位：原交易请求日期取自原支付日（paylog.addTime），微信渠道忽略
        request.setOrgReqDate(resolveOrgReqDate(ps.getOrderId()));
        request.setSplitAmount(splitAmount);
        request.setTotalAmount(ps.getTotalAmount());
        request.setOutOrderNo(ps.getOutOrderNo());
        request.setFinish(finish);
        request.setDescription("平台服务费");
        // 订单行锁仍由外层事务持有；在独立事务中先占位，进程中断/外层回滚也不能丢失在途记录。
        Integer previousStatus = ps.getStatus();
        ps.setStatus(ProfitSharingStatus.PROCESSING.getCode());
        ps.setFinishStatus(ProfitSharingFinishStatus.UNFINISHED.getCode());
        ps.setResultDesc("分账请求已登记，等待渠道确认");
        ps.setModifyTime(new Date());
        Boolean reserved = returnOrderTxTemplate.execute(status -> ps.getId() == null
                ? save(ps)
                : update(ps, new LambdaQueryWrapper<ProfitSharingOrder>()
                        .eq(ProfitSharingOrder::getId, ps.getId())
                        .eq(ProfitSharingOrder::getStatus, previousStatus)));
        if (!Boolean.TRUE.equals(reserved) || ps.getId() == null) {
            throw new GlobalException("分账在途记录保存失败或状态已变化，未调用渠道，请刷新重试");
        }
        boolean uncertain = false;
        try {
            ProfitSharingOrderResult result = clientFor(ps).createOrder(request);
            ps.setChannelOrderId(result.getOrderId());
            ps.setResultDesc(result.getDescription());
            if ("FINISHED".equals(result.getStatus()) || "SUCCESS".equals(result.getStatus())) {
                ps.setStatus(ProfitSharingStatus.SUCCESS.getCode());
                ps.setFinishStatus(ProfitSharingFinishStatus.FINISHED.getCode());
                ps.setFinishTime(new Date());
            } else if ("FAILED".equals(result.getStatus()) || "FAIL".equals(result.getStatus())) {
                ps.setStatus(ProfitSharingStatus.FAILED.getCode());
                ps.setFinishStatus(ProfitSharingFinishStatus.UNFINISHED.getCode());
            } else {
                ps.setStatus(ProfitSharingStatus.PROCESSING.getCode());
                ps.setFinishStatus(finish ? ProfitSharingFinishStatus.FINISHED.getCode()
                        : ProfitSharingFinishStatus.UNFINISHED.getCode());
            }
        } catch (Exception e) {
            log.error("调用分账渠道失败, outOrderNo={}", ps.getOutOrderNo(), e);
            uncertain = true;
            ps.setStatus(ProfitSharingStatus.PROCESSING.getCode());
            ps.setFinishStatus(ProfitSharingFinishStatus.UNFINISHED.getCode());
            ps.setResultDesc("渠道结果待确认: " + e.getMessage());
        }
        ps.setModifyTime(new Date());
        Boolean updated = returnOrderTxTemplate.execute(status -> {
            boolean saved = update(ps, new LambdaQueryWrapper<ProfitSharingOrder>()
                    .eq(ProfitSharingOrder::getId, ps.getId())
                    .eq(ProfitSharingOrder::getStatus, ProfitSharingStatus.PROCESSING.getCode()));
            if (saved) {
                recordOnlineSplit(ps);
            }
            return saved;
        });
        if (!Boolean.TRUE.equals(updated)) {
            // 通知可能先于同步响应写入终态，禁止旧响应把成功/失败改回处理中。
            ProfitSharingOrder latest = returnOrderTxTemplate.execute(status -> getById(ps.getId()));
            if (latest == null) {
                throw new GlobalException("分账结果未能保存，请核对渠道状态");
            }
            ps.setStatus(latest.getStatus());
            ps.setResultDesc(latest.getResultDesc());
            returnOrderTxTemplate.executeWithoutResult(status -> recordOnlineSplit(latest));
        }
        throwIfProfitSharingFailed(ps);
        if (uncertain && !ProfitSharingStatus.SUCCESS.getCode().equals(ps.getStatus())) {
            throw new GlobalException(ps.getResultDesc());
        }
        return true;
    }

    /**
     * 汇付跨天分账定位用：原交易请求日期（yyyyMMdd），从订单最后一条 paylog 的 addTime 推导。
     * <p>缺失时返回 {@code null}，由渠道客户端退回当天。微信渠道忽略该值。</p>
     */
    private String resolveOrgReqDate(Long orderId) {
        if (orderId == null) {
            return null;
        }
        // 取分账渠道那笔的支付日：混合支付下余额与汇付两条 paylog 可能跨天
        Paylog paylog = paylogService.getPaidProfitSharingPaylog(orderId.intValue());
        if (paylog == null || paylog.getAddTime() == null) {
            return null;
        }
        return java.time.Instant.ofEpochSecond(paylog.getAddTime())
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    // ==================== 手动完结 ====================

    /**
     * 是否允许手动完结分账单（操作栏「手动完结」按钮显隐与后端校验共用此规则）。
     *
     * <p>仅当分账单未完结，且分账状态为「分账中」或「分账成功」，并已具备微信交易号时允许完结；
     * 「待分账」「分账失败」状态不允许完结。</p>
     */
    private static boolean canManualFinish(ProfitSharingOrder order) {
        if (order == null) {
            return false;
        }
        if (ProfitSharingFinishStatus.FINISHED.getCode().equals(order.getFinishStatus())) {
            return false;
        }
        boolean statusAllowed = ProfitSharingStatus.SUCCESS.getCode().equals(order.getStatus())
                || ProfitSharingStatus.PROCESSING.getCode().equals(order.getStatus());
        if (!statusAllowed) {
            return false;
        }
        return StrUtil.isNotBlank(order.getTransactionId());
    }

    /**
     * 是否允许作废：仅「待分账」且无渠道交易号的单——它从未向渠道发起过，也永远发不出去
     * （典型为历史上无线上收款却误建的单），作废只改本地状态、不调渠道。
     */
    private static boolean canCancel(ProfitSharingOrder order) {
        return order != null
                && ProfitSharingStatus.PENDING.getCode().equals(order.getStatus())
                && StrUtil.isBlank(order.getTransactionId());
    }

    @Override
    public void manualFinishProfitSharing(Long profitSharingOrderId, Long operatorId) {
        ProfitSharingOrder order = getById(profitSharingOrderId);
        if (order == null) {
            throw new GlobalException("分账单不存在");
        }
        if (ProfitSharingFinishStatus.FINISHED.getCode().equals(order.getFinishStatus())) {
            throw new GlobalException("分账单已完结, 无需重复操作");
        }
        if (canCancel(order)) {
            // 待分账且无交易号：渠道侧没有这笔分账，走本地作废而非完结，给挂起单一个出口
            order.setStatus(ProfitSharingStatus.CANCELLED.getCode());
            order.setFinishStatus(ProfitSharingFinishStatus.FINISHED.getCode());
            order.setFinishTime(new Date());
            order.setOperatorId(operatorId);
            order.setResultDesc("手动作废: 未发起过渠道分账");
            order.setModifyTime(new Date());
            boolean cancelled = update(order, new LambdaQueryWrapper<ProfitSharingOrder>()
                    .eq(ProfitSharingOrder::getId, order.getId())
                    .eq(ProfitSharingOrder::getStatus, ProfitSharingStatus.PENDING.getCode())
                    .and(query -> query.isNull(ProfitSharingOrder::getTransactionId)
                            .or().eq(ProfitSharingOrder::getTransactionId, "")));
            if (!cancelled) {
                throw new GlobalException("分账状态已变化，不能作废，请刷新后重试");
            }
            log.info("分账单手动作废: id={}, outOrderNo={}", profitSharingOrderId, order.getOutOrderNo());
            return;
        }
        if (!ProfitSharingStatus.SUCCESS.getCode().equals(order.getStatus())
                && !ProfitSharingStatus.PROCESSING.getCode().equals(order.getStatus())) {
            throw new GlobalException("分账单当前状态不允许完结, status=" + order.getStatus());
        }
        if (StrUtil.isBlank(order.getTransactionId())) {
            throw new GlobalException("分账单缺少transactionId, 无法完结");
        }

        FinishProfitSharingRequest request = new FinishProfitSharingRequest();
        request.setSubMchId(order.getSubMchId());
        request.setTransactionId(order.getTransactionId());
        request.setOutOrderNo(order.getOutOrderNo());
        request.setDescription("手动完结分账");
        clientFor(order).finishOrder(request);

        ProfitSharingOrder finished = new ProfitSharingOrder();
        finished.setId(order.getId());
        finished.setFinishStatus(ProfitSharingFinishStatus.FINISHED.getCode());
        finished.setFinishTime(new Date());
        finished.setOperatorId(operatorId);
        finished.setModifyTime(new Date());
        updateById(finished);
        log.info("分账单手动完结成功: id={}, outOrderNo={}", profitSharingOrderId, order.getOutOrderNo());
    }

    // ==================== 回调 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleNotify(String outOrderNo, String status, String channelOrderId) {
        if (StrUtil.isBlank(outOrderNo)) {
            log.warn("分账回调缺少outOrderNo, 忽略");
            return;
        }
        ProfitSharingOrder order = lambdaQuery().eq(ProfitSharingOrder::getOutOrderNo, outOrderNo).one();
        if (order == null) {
            log.warn("分账回调找不到对应分账单: outOrderNo={}", outOrderNo);
            return;
        }
        if (ProfitSharingStatus.SUCCESS.getCode().equals(order.getStatus())
                || ProfitSharingStatus.FAILED.getCode().equals(order.getStatus())) {
            recordOnlineSplit(order);
            log.info("分账单[{}]已处于终态, 忽略重复回调", outOrderNo);
            return;
        }
        if (StrUtil.isNotBlank(channelOrderId)) {
            order.setChannelOrderId(channelOrderId);
        }
        if ("SUCCESS".equalsIgnoreCase(status) || "FINISHED".equalsIgnoreCase(status)) {
            order.setStatus(ProfitSharingStatus.SUCCESS.getCode());
            order.setFinishStatus(ProfitSharingFinishStatus.FINISHED.getCode());
            order.setFinishTime(new Date());
            order.setResultDesc("分账成功(回调)");
        } else if ("FAILED".equalsIgnoreCase(status) || "FAIL".equalsIgnoreCase(status)) {
            order.setStatus(ProfitSharingStatus.FAILED.getCode());
            order.setResultDesc("分账失败(回调)");
        } else {
            order.setStatus(ProfitSharingStatus.PROCESSING.getCode());
            order.setResultDesc("分账处理中(回调), channelStatus=" + status);
        }
        order.setModifyTime(new Date());
        boolean saved = update(order, new LambdaQueryWrapper<ProfitSharingOrder>()
                .eq(ProfitSharingOrder::getId, order.getId())
                .eq(ProfitSharingOrder::getStatus, ProfitSharingStatus.PROCESSING.getCode()));
        if (saved) {
            recordOnlineSplit(order);
        }
        log.info("分账回调处理完成: outOrderNo={}, status={}", outOrderNo, order.getStatus());
    }

    // ==================== 回退（退款） ====================

    @Override
    public boolean requiresProfitSharingReturn(Long orderId) {
        if (orderId == null) {
            return false;
        }
        return !listSuccessfulProfitSharingForRefund(orderId).isEmpty();
    }

    /** 原分账在途时整体阻断退款；独立读取最新已提交状态，避免审核事务旧快照漏掉在途单。 */
    private List<ProfitSharingOrder> listSuccessfulProfitSharingForRefund(Long orderId) {
        List<ProfitSharingOrder> orders = returnOrderTxTemplate.execute(status -> lambdaQuery()
                .eq(ProfitSharingOrder::getOrderId, orderId)
                .in(ProfitSharingOrder::getStatus,
                        ProfitSharingStatus.SUCCESS.getCode(), ProfitSharingStatus.PROCESSING.getCode())
                .list());
        if (orders.stream().anyMatch(order -> Objects.equals(
                order.getStatus(), ProfitSharingStatus.PROCESSING.getCode()))) {
            throw new GlobalException("订单分账处理中，请等待分账完成后重试退款");
        }
        return orders;
    }

    @Override
    public com.tigshop.bean.dto.finance.profitsharing.RefundSplitReversal getRefundSplitReversal(
            Long orderId, BigDecimal refundAmount) {
        com.tigshop.bean.dto.finance.profitsharing.RefundSplitReversal reversal =
                new com.tigshop.bean.dto.finance.profitsharing.RefundSplitReversal();
        if (orderId == null || refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return reversal;
        }
        List<ProfitSharingOrder> successOrders = lambdaQuery()
                .eq(ProfitSharingOrder::getOrderId, orderId)
                .eq(ProfitSharingOrder::getStatus, ProfitSharingStatus.SUCCESS.getCode())
                .list();
        if (successOrders.isEmpty()) {
            return reversal;
        }
        BigDecimal totalFee = BigDecimal.ZERO;
        BigDecimal payAmount = BigDecimal.ZERO;
        String subMchId = null;
        for (ProfitSharingOrder o : successOrders) {
            totalFee = totalFee.add(o.getServiceFee() != null ? o.getServiceFee() : BigDecimal.ZERO);
            if (o.getTotalAmount() != null && o.getTotalAmount().compareTo(payAmount) > 0) {
                payAmount = o.getTotalAmount();
            }
            if (subMchId == null && StrUtil.isNotBlank(o.getSubMchId())) {
                subMchId = o.getSubMchId();
            }
        }
        if (totalFee.compareTo(BigDecimal.ZERO) <= 0) {
            return reversal;
        }
        // 部分退款按 refundAmount/totalAmount 比例回退平台服务费；全额退（或缺总额）回退全部
        BigDecimal proportionalFee;
        if (payAmount.compareTo(BigDecimal.ZERO) > 0 && refundAmount.compareTo(payAmount) < 0) {
            proportionalFee = totalFee.multiply(refundAmount)
                    .divide(payAmount, 2, RoundingMode.HALF_UP);
        } else {
            proportionalFee = totalFee.setScale(2, RoundingMode.HALF_UP);
        }
        reversal.setNeedsReversal(proportionalFee.compareTo(BigDecimal.ZERO) > 0);
        reversal.setPlatformServiceFee(proportionalFee);
        reversal.setSubMchId(subMchId);
        return reversal;
    }

    @Override
    public void initiateProfitSharingReturn(Long orderId, Long refundApplyId, BigDecimal returnAmount) {
        // 回退入口再次检查，不能只退成功部分而漏掉同订单仍在途的其他分账。
        List<ProfitSharingOrder> successOrders = listSuccessfulProfitSharingForRefund(orderId);
        if (successOrders.isEmpty()) {
            throw new GlobalException("未找到已成功的分账记录，无法回退");
        }

        // 独立事务读取最新回退状态，避免外层审核事务的旧快照漏掉已提交的渠道结果。
        // 同一申请有记录不等于全部成功；失败、待处理和在途记录都必须按原单号恢复。
        List<ProfitSharingReturnOrder> existing = returnOrderTxTemplate.execute(status -> profitSharingReturnOrderMapper.selectList(
                new LambdaQueryWrapper<ProfitSharingReturnOrder>()
                        .eq(ProfitSharingReturnOrder::getOrderId, orderId)
                        .eq(ProfitSharingReturnOrder::getRefundApplyId, refundApplyId)));
        Map<Long, ProfitSharingReturnOrder> existingByOrder = new LinkedHashMap<>();
        for (ProfitSharingReturnOrder row : existing) {
            if (existingByOrder.putIfAbsent(row.getProfitSharingOrderId(), row) != null) {
                throw new GlobalException("同一分账单存在多笔历史回退记录，请核对后重试");
            }
        }

        Map<Long, BigDecimal> allocation = allocateReturnAmounts(orderId, successOrders, returnAmount, existingByOrder);

        // 先落库（独立事务）再调渠道：原实现是「调渠道→插库→失败抛异常」，且整方法与外层退款审核同一事务，
        // 前一笔资金已在渠道侧退还、后一笔失败时会连同前一笔的记录一起回滚，形成"钱动了但无据可查"。
        List<ProfitSharingReturnOrder> returnOrders = new ArrayList<>(existing);
        List<ProfitSharingReturnOrder> newReturnOrders = new ArrayList<>();
        for (ProfitSharingOrder psOrder : successOrders) {
            BigDecimal amount = allocation.getOrDefault(psOrder.getId(), BigDecimal.ZERO);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            ProfitSharingReturnOrder returnOrder = new ProfitSharingReturnOrder();
            returnOrder.setOutReturnNo(buildOutReturnNo(refundApplyId, psOrder.getId()));
            returnOrder.setOutOrderNo(psOrder.getOutOrderNo());
            returnOrder.setProfitSharingOrderId(psOrder.getId());
            returnOrder.setOrderId(orderId);
            returnOrder.setRefundApplyId(refundApplyId);
            returnOrder.setSubMchId(psOrder.getSubMchId());
            returnOrder.setPayChannel(psOrder.getPayChannel());
            returnOrder.setTransactionId(psOrder.getTransactionId());
            returnOrder.setReturnAmount(amount);
            returnOrder.setStatus(RETURN_STATUS_PENDING);
            returnOrder.setRetryCount(0);
            returnOrder.setCreateTime(new Date());
            returnOrder.setModifyTime(new Date());
            newReturnOrders.add(returnOrder);
            returnOrders.add(returnOrder);
        }
        // 同一申请的新增回退计划一起提交，避免只保存前一笔就中断，留下半份计划。
        if (!newReturnOrders.isEmpty()) {
            returnOrderTxTemplate.executeWithoutResult(status ->
                    newReturnOrders.forEach(profitSharingReturnOrderMapper::insert));
        }
        if (returnOrders.isEmpty()) {
            log.info("[分账回退] 订单[{}]本次无可回退额度，跳过", orderId);
            return;
        }

        for (ProfitSharingReturnOrder returnOrder : returnOrders) {
            if (Objects.equals(returnOrder.getStatus(), RETURN_STATUS_SUCCESS)) {
                returnOrderTxTemplate.executeWithoutResult(status -> recordOnlineReturn(returnOrder, successOrders));
                continue;
            }
            boolean processing = Objects.equals(returnOrder.getStatus(), RETURN_STATUS_PROCESSING);
            String outReturnNo = returnOrder.getOutReturnNo();

            try {
                var client = profitSharingClientRegistry.get(
                        StrUtil.blankToDefault(returnOrder.getPayChannel(), PAY_CHANNEL_WECHAT));
                ProfitSharingReturnResult result;
                if (processing) {
                    result = client.queryReturnOrder(returnOrder.getSubMchId(), returnOrder.getOutOrderNo(), outReturnNo);
                } else {
                    // 重试使用落库时的资金归属、金额和单号，不能按当前绑定重建请求。
                    ProfitSharingReturnRequest request = new ProfitSharingReturnRequest();
                    request.setSubMchId(returnOrder.getSubMchId());
                    request.setOutOrderNo(returnOrder.getOutOrderNo());
                    request.setOutReturnNo(outReturnNo);
                    request.setReturnAmount(returnOrder.getReturnAmount());
                    request.setDescription("售后退款回退");
                    request.setOrgReqDate(resolveOrgReqDate(orderId));
                    request.setTransactionId(returnOrder.getTransactionId());
                    result = client.returnOrder(request);
                }
                returnOrder.setChannelReturnId(result.getReturnId());
                returnOrder.setResultDesc(result.getDescription());
                if ("SUCCESS".equals(result.getStatus())) {
                    returnOrder.setStatus(RETURN_STATUS_SUCCESS);
                } else if ("PROCESSING".equals(result.getStatus())) {
                    returnOrder.setStatus(RETURN_STATUS_PROCESSING);
                } else {
                    returnOrder.setStatus(RETURN_STATUS_FAILED);
                    if (StrUtil.isBlank(returnOrder.getResultDesc())) {
                        returnOrder.setResultDesc("回退失败: " + result.getStatus());
                    }
                }
            } catch (Exception e) {
                log.error("分账回退API调用失败: outOrderNo={}, outReturnNo={}", returnOrder.getOutOrderNo(), outReturnNo, e);
                // 查询失败不能释放仍在途的额度，也不能让退款继续。
                returnOrder.setStatus(processing ? RETURN_STATUS_PROCESSING : RETURN_STATUS_FAILED);
                returnOrder.setResultDesc("回退失败: " + e.getMessage());
            }

            returnOrder.setModifyTime(new Date());
            returnOrderTxTemplate.executeWithoutResult(status -> {
                profitSharingReturnOrderMapper.updateById(returnOrder);
                recordOnlineReturn(returnOrder, successOrders);
            });
            log.info("分账回退发起: outReturnNo={}, amount={}, status={}",
                    outReturnNo, returnOrder.getReturnAmount(), returnOrder.getStatus());

            // 仍然阻断退款：回退的目的就是把资金退回二级商户可用余额，回退没成功还继续退款，
            // 只会在渠道侧因余额不足再失败一次。与改造前的区别是回退单已独立提交、不会随异常丢失，
            // 补偿任务可以接手重试。
            if (returnOrder.getStatus() != RETURN_STATUS_SUCCESS) {
                throw new GlobalException("退款失败: 分账回退未成功 - "
                        + (StrUtil.isNotBlank(returnOrder.getResultDesc()) ? returnOrder.getResultDesc()
                        : "状态=" + returnOrder.getStatus()));
            }
        }
    }

    /**
     * 确定性回退单号，去掉原实现里的时间戳。
     * <p>时间戳会让每次重试都变成渠道眼中的新单，渠道无从拦截重复回退。
     */
    private String buildOutReturnNo(Long refundApplyId, Long profitSharingOrderId) {
        return "PSR_" + refundApplyId + "_" + profitSharingOrderId;
    }

    /** 记录渠道资金转出，不把分账完结或解冻重复当作线上收入。 */
    private void recordOnlineSplit(ProfitSharingOrder order) {
        if (!ProfitSharingStatus.SUCCESS.getCode().equals(order.getStatus())
                || order.getShopId() == null || order.getShopId() <= 0
                || order.getSplitAmount() == null || order.getSplitAmount().signum() <= 0) {
            return;
        }
        Date eventTime = order.getFinishTime() == null ? order.getModifyTime() : order.getFinishTime();
        accountService.recordOnlineChange(order.getShopId().intValue(),
                StrUtil.blankToDefault(order.getPayChannel(), PAY_CHANNEL_WECHAT),
                "PS_SPLIT:" + order.getId(), order.getSplitAmount().negate(),
                "线上分账支出：" + order.getOutOrderNo(),
                eventTime == null ? System.currentTimeMillis() / 1000 : eventTime.getTime() / 1000);
    }

    private void recordOnlineReturn(ProfitSharingReturnOrder returned, List<ProfitSharingOrder> orders) {
        if (!Integer.valueOf(RETURN_STATUS_SUCCESS).equals(returned.getStatus())
                || returned.getReturnAmount() == null || returned.getReturnAmount().signum() <= 0) {
            return;
        }
        ProfitSharingOrder order = orders.stream()
                .filter(item -> Objects.equals(item.getId(), returned.getProfitSharingOrderId()))
                .findFirst().orElse(null);
        if (order == null || order.getShopId() == null || order.getShopId() <= 0) {
            return;
        }
        accountService.recordOnlineChange(order.getShopId().intValue(),
                StrUtil.blankToDefault(returned.getPayChannel(), PAY_CHANNEL_WECHAT),
                "PS_RETURN:" + returned.getId(), returned.getReturnAmount(),
                "线上分账回退：" + returned.getOutReturnNo(),
                returned.getModifyTime() == null ? System.currentTimeMillis() / 1000 : returned.getModifyTime().getTime() / 1000);
    }

    /**
     * 计算本次退款应回退的总额，并按各分账单的剩余额度占比分摊。
     *
     * <p>原实现对每笔成功分账单独立取 {@code min(returnAmount, splitAmount)}，而一个订单同时存在
     * 店铺分账与供应商分账是供应商订单的常态，退 10 元会被回退 20 元。此处改为先按退款额占实付额
     * 的比例算出总额，再分摊到各笔，口径与 {@link #getRefundSplitReversal} 保持一致。
     *
     * @return 分账单 id → 本次回退额；无可回退额度的分账单不出现在结果中
     */
    private Map<Long, BigDecimal> allocateReturnAmounts(
            Long orderId, List<ProfitSharingOrder> successOrders, BigDecimal returnAmount,
            Map<Long, ProfitSharingReturnOrder> existingByOrder) {
        if (successOrders.stream().anyMatch(o -> o.getSharingBaseAmount() != null)) {
            return allocateNetReturnAmounts(orderId, successOrders, returnAmount, existingByOrder);
        }
        // 各分账单剩余可回退额度 = 已分金额 − 历史已回退（含在途），支持同一订单多次部分退款
        Map<Long, BigDecimal> remaining = new LinkedHashMap<>();
        BigDecimal totalRemaining = BigDecimal.ZERO;
        BigDecimal totalSplit = BigDecimal.ZERO;
        for (ProfitSharingOrder psOrder : successOrders) {
            BigDecimal split = psOrder.getSplitAmount() == null ? BigDecimal.ZERO : psOrder.getSplitAmount();
            totalSplit = totalSplit.add(split);
            // 本申请已有金额固定不变（包含失败单），只给缺失的分账单分配剩余预算。
            if (existingByOrder.containsKey(psOrder.getId())) {
                continue;
            }
            BigDecimal returned = sumReturnedAmount(psOrder.getId());
            BigDecimal left = split.subtract(returned).max(BigDecimal.ZERO);
            remaining.put(psOrder.getId(), left);
            totalRemaining = totalRemaining.add(left);
        }
        if (totalRemaining.compareTo(BigDecimal.ZERO) <= 0) {
            return Map.of();
        }

        // 比例始终以原始已分总额计算；剩余额度只作上限，不能作为下一次部分退款的基数。
        BigDecimal payAmount = successOrders.stream()
                .map(ProfitSharingOrder::getTotalAmount)
                .filter(Objects::nonNull)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        BigDecimal target;
        if (payAmount.compareTo(BigDecimal.ZERO) > 0 && returnAmount != null
                && returnAmount.compareTo(payAmount) < 0) {
            target = totalSplit.multiply(returnAmount)
                    .divide(payAmount, 2, RoundingMode.HALF_UP);
        } else {
            target = totalSplit;
        }
        BigDecimal existingAmount = existingByOrder.values().stream()
                .map(ProfitSharingReturnOrder::getReturnAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        target = target.subtract(existingAmount).max(BigDecimal.ZERO);
        // 总量护栏：任何情况下不得回退超过剩余已分金额
        target = target.min(totalRemaining);
        if (target.compareTo(BigDecimal.ZERO) <= 0) {
            return Map.of();
        }

        Map<Long, BigDecimal> allocation = new LinkedHashMap<>();
        BigDecimal allocated = BigDecimal.ZERO;
        List<Long> ids = new ArrayList<>(remaining.keySet());
        for (int index = 0; index < ids.size(); index++) {
            Long id = ids.get(index);
            BigDecimal left = remaining.get(id);
            BigDecimal amount;
            if (index == ids.size() - 1) {
                // 末笔用差额兜底，避免逐笔四舍五入导致合计与 target 对不上
                amount = target.subtract(allocated);
            } else {
                amount = target.multiply(left)
                        .divide(totalRemaining, 2, RoundingMode.HALF_UP);
            }
            amount = amount.min(left).max(BigDecimal.ZERO);
            allocation.put(id, amount);
            allocated = allocated.add(amount);
        }
        log.info("[分账回退] 订单[{}]本次回退分摊: 目标={}, 实际合计={}, 明细={}",
                orderId, target, allocated, allocation);
        return allocation;
    }

    /** 净额单按各自冻结的基数算累计应退，既支持混合基数，也能在最终全退时补齐分的尾差。 */
    private Map<Long, BigDecimal> allocateNetReturnAmounts(Long orderId, List<ProfitSharingOrder> orders,
            BigDecimal returnAmount, Map<Long, ProfitSharingReturnOrder> existingByOrder) {
        Paylog paylog = paylogService.getPaidProfitSharingPaylog(orderId.intValue());
        if (paylog == null || paylog.getPaylogId() == null || returnAmount == null || returnAmount.signum() <= 0) {
            throw new GlobalException("无法确认净额分账的原支付流水或退款金额");
        }
        BigDecimal refunded = returnOrderTxTemplate.execute(status -> paylogRefundMapper.selectList(
                        new LambdaQueryWrapper<PaylogRefund>()
                                .eq(PaylogRefund::getPaylogId, paylog.getPaylogId())
                                .eq(PaylogRefund::getStatus, 1))).stream()
                .map(PaylogRefund::getRefundAmount).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<Long, BigDecimal> allocation = new LinkedHashMap<>();
        for (ProfitSharingOrder order : orders) {
            if (existingByOrder.containsKey(order.getId())) {
                continue;
            }
            BigDecimal total = order.getTotalAmount();
            BigDecimal base = order.getSharingBaseAmount() == null ? total : order.getSharingBaseAmount();
            if (total == null || base == null || base.signum() <= 0 || base.compareTo(total) > 0
                    || paylog.getPayAmount() == null || total.compareTo(paylog.getPayAmount()) != 0) {
                throw new GlobalException("分账计费基数与原交易不一致，不能自动回退");
            }
            BigDecimal split = order.getSplitAmount();
            BigDecimal returned = sumReturnedAmount(order.getId());
            BigDecimal remaining = split.subtract(returned).max(BigDecimal.ZERO);
            BigDecimal afterSharingRefunded = refunded.add(returnAmount).subtract(total.subtract(base))
                    .max(BigDecimal.ZERO).min(base);
            BigDecimal target = split.multiply(afterSharingRefunded).divide(base, 2, RoundingMode.HALF_UP);
            BigDecimal amount = target.subtract(returned).max(BigDecimal.ZERO).min(remaining);
            if (amount.signum() > 0) {
                allocation.put(order.getId(), amount);
            }
        }
        return allocation;
    }

    /**
     * 某笔分账单历史已回退金额，含在途，避免多次部分退款累计超退。
     */
    private BigDecimal sumReturnedAmount(Long profitSharingOrderId) {
        if (profitSharingOrderId == null) {
            return BigDecimal.ZERO;
        }
        return profitSharingReturnOrderMapper.selectList(
                        new LambdaQueryWrapper<ProfitSharingReturnOrder>()
                                .eq(ProfitSharingReturnOrder::getProfitSharingOrderId, profitSharingOrderId)
                                .in(ProfitSharingReturnOrder::getStatus, RETURN_STATUS_OCCUPIED))
                .stream()
                .map(ProfitSharingReturnOrder::getReturnAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ==================== 工具 ====================

    /**
     * 汇总订单的店铺服务费（statement: type=SERVICE_FEE 且 shop_id=指定店铺）。
     * <p>分账金额另加分销佣金，见 processShopProfitSharing（CR-20260731-131859）。
     */
    private BigDecimal sumServiceFee(Integer orderId, Integer shopId) {
        List<Statement> statements = statementMapper.selectList(new LambdaQueryWrapper<Statement>()
                .eq(Statement::getRecordId, orderId)
                .eq(Statement::getType, StatementType.SERVICE_FEE.getCode())
                .eq(Statement::getShopId, shopId));
        return statements.stream()
                .map(s -> s.getAmount() != null ? s.getAmount().abs() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 汇总订单的供应商服务费（statement: type=SERVICE_FEE 且 vendor_id=指定供应商）。
     */
    private BigDecimal sumVendorServiceFee(Integer orderId, Integer vendorId) {
        List<Statement> statements = statementMapper.selectList(new LambdaQueryWrapper<Statement>()
                .eq(Statement::getRecordId, orderId)
                .eq(Statement::getType, StatementType.SERVICE_FEE.getCode())
                .eq(Statement::getVendorId, vendorId));
        return statements.stream()
                .map(s -> s.getAmount() != null ? s.getAmount().abs() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Integer parseStatus(String status) {
        if (StrUtil.isBlank(status)) {
            return null;
        }
        try {
            return Integer.parseInt(status.trim());
        } catch (NumberFormatException e) {
            try {
                return ProfitSharingStatus.valueOf(status.trim().toUpperCase()).getCode();
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
    }

    private Integer parseFinishStatus(String finishStatus) {
        if (StrUtil.isBlank(finishStatus)) {
            return null;
        }
        try {
            return Integer.parseInt(finishStatus.trim());
        } catch (NumberFormatException e) {
            try {
                return ProfitSharingFinishStatus.valueOf(finishStatus.trim().toUpperCase()).getCode();
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
    }

    private com.tigshop.bean.vo.finance.ProfitSharingOrderVO toVO(ProfitSharingOrder order) {
        com.tigshop.bean.vo.finance.ProfitSharingOrderVO vo = new com.tigshop.bean.vo.finance.ProfitSharingOrderVO();
        vo.setId(order.getId());
        vo.setOutOrderNo(order.getOutOrderNo());
        vo.setOrderId(order.getOrderId());
        vo.setOrderSn(order.getOrderSn());
        vo.setShopId(order.getShopId() != null ? order.getShopId().intValue() : null);
        vo.setVendorId(order.getVendorId() != null ? order.getVendorId().intValue() : null);
        vo.setSettlementType(order.getSettlementType());
        vo.setTransactionId(order.getTransactionId());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setSplitAmount(order.getSplitAmount());
        vo.setServiceFee(order.getServiceFee());
        vo.setCommissionFee(order.getCommissionFee());
        vo.setSettlementId(order.getSettlementId());
        vo.setResultDesc(order.getResultDesc());
        vo.setCreateTime(order.getCreateTime());
        vo.setModifyTime(order.getModifyTime());

        ProfitSharingStatus status = ProfitSharingStatus.fromCode(order.getStatus());
        if (status != null) {
            vo.setStatus(status.name());
            vo.setStatusDesc(status.getDesc());
        }
        ProfitSharingFinishStatus finishStatus = ProfitSharingFinishStatus.fromCode(order.getFinishStatus());
        if (finishStatus != null) {
            vo.setFinishStatus(finishStatus.name());
            vo.setFinishStatusDesc(finishStatus.getDesc());
        }
        ProfitSharingTriggerType triggerType = ProfitSharingTriggerType.fromCode(order.getTriggerType());
        if (triggerType != null) {
            vo.setTriggerType(triggerType.name());
        }
        vo.setCanFinish(canManualFinish(order));
        vo.setCanCancel(canCancel(order));
        return vo;
    }

    private void fillShopNames(List<com.tigshop.bean.vo.finance.ProfitSharingOrderVO> voList) {
        if (voList == null || voList.isEmpty()) {
            return;
        }
        List<Integer> shopIds = voList.stream()
                .map(com.tigshop.bean.vo.finance.ProfitSharingOrderVO::getShopId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());
        if (shopIds.isEmpty()) {
            return;
        }
        List<Shop> shops = shopMapper.selectBatchIds(shopIds);
        Map<Integer, String> shopNameMap = shops.stream()
                .collect(Collectors.toMap(Shop::getShopId, s -> StrUtil.nullToEmpty(s.getShopTitle()), (a, b) -> a));
        voList.forEach(vo -> vo.setShopName(vo.getShopId() == null ? "" : shopNameMap.getOrDefault(vo.getShopId(), "")));
    }

    private void fillVendorNames(List<com.tigshop.bean.vo.finance.ProfitSharingOrderVO> voList) {
        if (voList == null || voList.isEmpty()) {
            return;
        }
        List<Integer> vendorIds = voList.stream()
                .map(com.tigshop.bean.vo.finance.ProfitSharingOrderVO::getVendorId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());
        if (vendorIds.isEmpty()) {
            return;
        }
        List<com.tigshop.bean.model.vendor.Vendor> vendors = vendorMapper.selectOnboardedList(
                new LambdaQueryWrapper<com.tigshop.bean.model.vendor.Vendor>()
                        .in(com.tigshop.bean.model.vendor.Vendor::getVendorId, vendorIds));
        Map<Integer, String> vendorNameMap = vendors.stream()
                .collect(Collectors.toMap(com.tigshop.bean.model.vendor.Vendor::getVendorId,
                        v -> StrUtil.nullToEmpty(v.getVendorName()), (a, b) -> a));
        voList.forEach(vo -> {
            if (vo.getVendorId() != null && vo.getVendorId() > 0) {
                vo.setVendorName(vendorNameMap.getOrDefault(vo.getVendorId(), ""));
            }
        });
    }
}
