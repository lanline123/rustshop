package com.tigshop.service.finance.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.enums.finance.CreditOrderSettlementStatusEnum;
import com.tigshop.bean.enums.finance.RefundLogTypeEnum;
import com.tigshop.bean.enums.msg.MessageTypeEnum;
import com.tigshop.bean.enums.order.AftersalesStatusEnum;
import com.tigshop.bean.enums.order.OrderStatusEnum;
import com.tigshop.bean.enums.order.OrderType;
import com.tigshop.bean.enums.order.ShippingStatusEnum;
import com.tigshop.bean.enums.promotion.JoinStatusEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.finance.*;
import com.tigshop.bean.model.order.Aftersales;
import com.tigshop.bean.model.order.AftersalesItem;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.model.promotion.GrouponPromotionRecord;
import com.tigshop.bean.model.product.ECard;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.model.promotion.PointsExchange;
import com.tigshop.bean.model.salesman.SalesmanOrder;
import com.tigshop.bean.param.finance.refundapply.RefundApplyAuditParam;
import com.tigshop.bean.param.finance.refundapply.RefundApplyEditParam;
import com.tigshop.bean.param.finance.refundapply.RefundApplySaveParam;
import com.tigshop.bean.param.finance.refundapply.OfflineAuditParam;
import com.tigshop.bean.query.finance.RefundApplyPageQuery;
import com.tigshop.bean.vo.finance.refundapply.RefundApplyDetailVO;
import com.tigshop.bean.vo.finance.refundapply.RefundApplyVO;
import com.tigshop.bean.vo.order.OrderExtraSkuDataDTO;
import com.tigshop.bean.vo.order.OrderPresaleVO;
import com.tigshop.bean.vo.order.pay.PayNotifyVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.finance.RefundApplyMapper;
import com.tigshop.mapper.order.OrderItemMapper;
import com.tigshop.mapper.product.ECardMapper;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.mapper.product.ProductSkuMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.*;
import com.tigshop.service.msgTemplate.MessageService;
import com.tigshop.service.order.AftersalesItemService;
import com.tigshop.service.order.AftersalesLogService;
import com.tigshop.service.order.AftersalesService;
import com.tigshop.service.order.OrderPresaleService;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.pay.NewPayChannelRegistry;
import com.tigshop.service.pay.PayService;
import com.tigshop.service.pay.PaymentService;
import com.tigshop.service.pay.WechatPayModeResolver;
import com.tigshop.service.pay.impl.AliPayServiceImpl;
import com.tigshop.service.pay.impl.PaypalServiceImpl;
import com.tigshop.service.pay.impl.WechatPayServiceImpl;
import com.tigshop.service.promotion.GrouponPromotionRecordService;
import com.tigshop.service.promotion.PointsExchangeService;
import com.tigshop.service.content.WechatLiveOrderSyncService;
import com.tigshop.service.salesman.SalesmanOrderService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.user.UserBalanceService;
import com.tigshop.service.user.UserPointsLogService;
import com.tigshop.service.user.UserRankService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import jakarta.annotation.PostConstruct;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.content.WechatLiveOrderSyncConstants.EVENT_REFUND_UPDATED;
import static com.tigshop.common.constant.finance.RefundApplyConstants.*;

/**
 * 退款申请服务实现类
 *
 * @author kidd
 * @create 2025/7/7
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class RefundApplyServiceImpl extends BaseServiceImpl<RefundApplyMapper, RefundApply> implements RefundApplyService {

    /** 退款流水失败态。0申请中/1成功由 {@code PaylogRefundServiceImpl.OCCUPIED_STATUS} 计入已占用额度，2 不计入 */
    private static final int PAYLOG_REFUND_STATUS_FAILED = 2;

    private final OrderFinanceAftersalesService orderFinanceAftersalesService;
    private final AftersalesService aftersalesService;
    private final OrderService orderService;
    private final OrderItemMapper orderItemMapper;
    private final PaylogService paylogService;
    private final PaylogRefundService paylogRefundService;
    private final WechatPayServiceImpl wechatPayService;
    // 只用于解析退款候选二级商户号；形态判定与查单确认全在微信实现内，本类不感知支付形态
    private final WechatPayModeResolver wechatPayModeResolver;
    private final AliPayServiceImpl aliPayService;
    private final PaypalServiceImpl paypalService;
    // 新增在线支付渠道（汇付/ApplePay/拉卡拉）统一走局部工厂分发；既有渠道保持各自分支
    private final NewPayChannelRegistry newPayChannelRegistry;
    private final PaymentService paymentService;
    private final RefundLogService refundLogService;
    private final UserBalanceService userBalanceService;
    private final AftersalesItemService aftersalesItemService;
    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ECardMapper eCardMapper;
    private final UserRankService userRankService;
    private final MessageService messageService;
    private final UserPointsLogService userPointsLogService;
    private final RefundApplyPaylogRefundService refundApplyPaylogRefundService;
    private final OrderPresaleService orderPresaleService;
    private final SalesmanOrderService salesmanOrderService;
    private final ConfigService configService;
    private final PointsExchangeService pointsExchangeService;
    private final AftersalesLogService aftersalesLogService;
    private final GrouponPromotionRecordService grouponPromotionRecordService;
    private final CreditOrderService creditOrderService;
    private final CreditUserService creditUserService;
    private final WechatLiveOrderSyncService wechatLiveOrderSyncService;

    private final PlatformTransactionManager transactionManager;

    /**
     * 退款流水专用的独立事务：整个退款审核在一个大事务里，渠道扣款成功后若后续步骤抛异常，
     * 流水记录会随之回滚，形成「钱已退出、本地无据」，用户重新申请还会再退一次。
     * 故流水的落库与失败置位都用 REQUIRES_NEW 独立提交，不受外层回滚影响。
     */
    private TransactionTemplate refundRecordTx;

    @PostConstruct
    private void initRefundRecordTx() {
        this.refundRecordTx = new TransactionTemplate(transactionManager);
        this.refundRecordTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public Map<Integer, String> config() {
        return REFUND_STATUS_NAME;
    }

    @Override
    public Page<RefundApplyVO> list(RefundApplyPageQuery pageQuery) {
        Page<RefundApply> page = buildSortOrder(pageQuery);
        Page<RefundApplyVO> resultPage = this.baseMapper.page(page, pageQuery);

        List<RefundApplyVO> records = resultPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return new Page<>();
        }

        List<Integer> aftersalesIds = records.stream().map(RefundApplyVO::getAftersaleId).toList();
        List<OrderFinanceAftersales> orderFinanceAftersalesList = orderFinanceAftersalesService.listByIds(aftersalesIds);
        // 构建售后ID到售后信息的映射
        Map<Integer, OrderFinanceAftersales> aftersalesMap = orderFinanceAftersalesList.stream()
                .collect(Collectors.toMap(OrderFinanceAftersales::getAftersaleId, a -> a, (a, b) -> a));

        List<Integer> orderIds = records.stream().map(RefundApplyVO::getOrderId).filter(Objects::nonNull).distinct().toList();
        Map<Integer, CreditOrder> creditOrderMap = new java.util.HashMap<>();
        if (!orderIds.isEmpty()) {
            List<CreditOrder> creditOrders = creditOrderService.lambdaQuery().in(CreditOrder::getOrderId, orderIds).list();
            if (CollUtil.isNotEmpty(creditOrders)) {
                creditOrderMap.putAll(creditOrders.stream().collect(Collectors.toMap(CreditOrder::getOrderId, c -> c)));
            }
        }

        records.forEach(record -> {
            record.assembleData(orderFinanceAftersalesList);
            CreditOrder creditOrder = creditOrderMap.get(record.getOrderId());
            if (creditOrder != null) {
                record.setIsCreditOrder(1);
                record.setCreditAmount(creditOrder.getCreditAmount());
                OrderFinanceAftersales aftersalesInfo = aftersalesMap.get(record.getAftersaleId());
                record.setCreditRefundAmount(aftersalesInfo != null ? aftersalesInfo.getRefundAmount() : BigDecimal.ZERO);
            } else {
                record.setIsCreditOrder(0);
                record.setCreditAmount(BigDecimal.ZERO);
                record.setCreditRefundAmount(BigDecimal.ZERO);
            }
        });
        return resultPage;
    }

    @Override
    public RefundApplyDetailVO detail(Integer id) {
        RefundApply refundApply = this.getById(id);
        Assert.notNull(refundApply, () -> new GlobalException("该申请不存在"));

        Order order = orderService.lambdaQuery().eq(Order::getOrderId, refundApply.getOrderId()).one();
        List<OrderItem> orderItems = orderItemMapper.selectList(
                Wrappers.lambdaQuery(OrderItem.class).eq(OrderItem::getOrderId, order.getOrderId())
        );

        // 排除退款成功的支付金额
        List<RefundApply> completedRefundApplies = this.lambdaQuery()
                .eq(RefundApply::getOrderId, refundApply.getOrderId())
                // 按退款类型隔离：refund_apply 的 orderId 仅在同一 refundType 内唯一，避免将来扩类型后跨类型串单
                .eq(RefundApply::getRefundType, refundApply.getRefundType())
                .in(RefundApply::getRefundStatus, REFUND_STATUS_PROCESSING, REFUND_STATUS_PROCESSED)
                .list();
        BigDecimal completeOnlineBalance = completedRefundApplies.stream()
                .map(RefundApply::getOnlineBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // 已完成的总金额
        BigDecimal totalCompleteAmount = completedRefundApplies.stream()
                .map(item -> item.getRefundBalance().add(item.getOnlineBalance()).add(item.getOfflineBalance()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 售后协商可退金额必填 refund_amount
        Aftersales aftersales = aftersalesService.lambdaQuery().eq(Aftersales::getAftersaleId, refundApply.getAftersaleId()).one();
        BigDecimal refundAmount = aftersales.getRefundAmount();


        List<AftersalesItem> aftersalesItems = aftersalesItemService.lambdaQuery().eq(AftersalesItem::getAftersaleId, refundApply.getAftersaleId()).list();
        // 按退款类型隔离：避免将来扩 refundType 多类型后按 orderId 跨类型串单
        List<RefundApply> refundApplies = this.lambdaQuery().eq(RefundApply::getOrderId, refundApply.getOrderId()).eq(RefundApply::getRefundType, refundApply.getRefundType()).list();
        List<Aftersales> orderCompleteAftersales = aftersalesService.lambdaQuery()
                .eq(Aftersales::getOrderId, refundApply.getOrderId())
                .eq(Aftersales::getStatus, AftersalesStatusEnum.COMPLETE.getCode())
                .list();
        List<Integer> aftersalesIds = orderCompleteAftersales.stream().map(Aftersales::getAftersaleId).toList();
        List<AftersalesItem> orderAllAftersalesItems = Objects.isNull(aftersalesIds)
                || aftersalesIds.isEmpty() ? Collections.emptyList() : aftersalesItemService.lambdaQuery().in(AftersalesItem::getAftersaleId, aftersalesIds).list();
        refundAmount = calculateFinanceRefundAmount(order, aftersales, orderItems, aftersalesItems, refundApplies, orderAllAftersalesItems);

        // 真实的线上剩余可退金额
        BigDecimal effectiveOnlineBalance = order.getOnlinePaidAmount().subtract(completeOnlineBalance);
        if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            effectiveOnlineBalance = effectiveOnlineBalance.compareTo(refundAmount) > 0 ? refundAmount : effectiveOnlineBalance;
        }

        List<Integer> productIds = orderItems.stream().map(OrderItem::getProductId).toList();
        List<Product> products = productMapper.selectBatchIds(productIds);

        List<Integer> skuIds = orderItems.stream().map(OrderItem::getSkuId).toList();
        List<ProductSku> productSkus = productSkuMapper.selectBatchIds(skuIds);

        List<Integer> orderItemIds = orderItems.stream().map(OrderItem::getItemId).toList();
        List<ECard> eCards = eCardMapper.selectList(
                Wrappers.lambdaQuery(ECard.class).in(ECard::getOrderItemId, orderItemIds)
        );


        RefundApplyDetailVO vo = new RefundApplyDetailVO(refundApply, order, orderItems, products, productSkus, eCards, aftersales, aftersalesItems, effectiveOnlineBalance, totalCompleteAmount, refundApplies, orderAllAftersalesItems);

        CreditOrder creditOrder = creditOrderService.getByOrderId(refundApply.getOrderId());
        if (creditOrder != null) {
            vo.setIsCreditOrder(1);
            vo.setCreditAmount(creditOrder.getCreditAmount());
            BigDecimal creditRefundAmount = getCreditRefundAmount(
                    order, aftersales, orderItems, aftersalesItems, refundApplies, orderAllAftersalesItems);
            vo.setCreditRefundAmount(creditRefundAmount);
        } else {
            vo.setIsCreditOrder(0);
            vo.setCreditAmount(BigDecimal.ZERO);
            vo.setCreditRefundAmount(BigDecimal.ZERO);
        }

        // 拆单场景统一订单编号：顶层 orderSn 取"售后对应的拆单子订单号"（aftersales.orderId），
        // 与售后详情页(AftersalesServiceImpl 用 aftersales.orderId 取 sn)保持一致；
        // 仅覆盖展示字段，不影响金额计算所用的 order（父单聚合金额不变）。子订单缺失时保留原值。
        if (aftersales != null && aftersales.getOrderId() != null
                && !aftersales.getOrderId().equals(refundApply.getOrderId())) {
            Order aftersalesOrder = orderService.getById(aftersales.getOrderId());
            if (aftersalesOrder != null && StrUtil.isNotBlank(aftersalesOrder.getOrderSn())) {
                vo.setOrderSn(aftersalesOrder.getOrderSn());
            }
        }

        return vo;
    }

    @Transactional
    @Override
    public void audit(RefundApplyAuditParam param) {
        param.initParam();

        RefundApply refundApply = this.getById(param.getRefundId());
        Assert.notNull(refundApply, () -> new GlobalException("该申请不存在"));
        Assert.isTrue(refundApply.getRefundStatus() == 0, () -> new GlobalException("申请状态值错误"));
        Assert.isTrue(param.getRefundStatus() == 1 || param.getRefundStatus() == 3 || param.getRefundStatus() == 4,
                () -> new GlobalException("审核状态值错误"));

        // 平台财务拒绝退款：拒绝原因必填，且只更新待处理申请，不进入任何退款金额或资金副作用流程。
        if (param.getRefundStatus() == REFUND_STATUS_REJECTED) {
            String rejectReason = StrUtil.trim(param.getRefundNote());
            Assert.isTrue(StrUtil.isNotBlank(rejectReason), () -> new GlobalException("拒绝原因不能为空"));
            Assert.isTrue(rejectReason.length() <= 255, () -> new GlobalException("拒绝原因不能超过255个字符"));

            int updated = this.baseMapper.update(null, new UpdateWrapper<RefundApply>()
                    .eq("refund_id", refundApply.getRefundId())
                    .eq("refund_status", REFUND_STATUS_WAIT)
                    .set("refund_status", REFUND_STATUS_REJECTED)
                    .set("refund_note", rejectReason));
            Assert.isTrue(updated == 1, () -> new GlobalException("退款申请已处理，请刷新后重试"));

            Aftersales aftersales = aftersalesService.getById(refundApply.getAftersaleId());
            if (aftersales != null) {
                Aftersales updateAftersales = new Aftersales();
                updateAftersales.setAftersaleId(aftersales.getAftersaleId());
                updateAftersales.setStatus(AftersalesStatusEnum.REFUSE.getCode());
                updateAftersales.setReply(rejectReason);
                boolean aftersalesUpdated = aftersalesService.updateById(updateAftersales);
                Assert.isTrue(aftersalesUpdated, () -> new GlobalException("关联售后状态更新失败，请重试"));

                aftersalesLogService.addAftersaleLog(aftersales.getAftersaleId(),
                        "财务审核不通过：" + rejectReason,
                        aftersales.getShopId(), aftersales.getVendorId());
            }
            return;
        }

        // 财务审核不通过：退回售后人员重新处理
        if (param.getRefundStatus() == 3) {
            refundApply.setRefundStatus(3);
            if (StrUtil.isNotBlank(param.getRefundNote())) {
                refundApply.setRefundNote(param.getRefundNote());
            }
            this.updateById(refundApply);

            Aftersales aftersales = aftersalesService.getById(refundApply.getAftersaleId());
            if (aftersales != null) {
                Aftersales updateAftersales = new Aftersales();
                updateAftersales.setAftersaleId(aftersales.getAftersaleId());
                updateAftersales.setStatus(AftersalesStatusEnum.APPROVED_FOR_PROCESSING.getCode());
                aftersalesService.updateById(updateAftersales);

                aftersalesLogService.addAftersaleLog(aftersales.getAftersaleId(),
                        "财务审核不通过，退回售后人员处理：" + param.getRefundNote());
            }
            return;
        }

        Order order = orderService.getById(refundApply.getOrderId());
        CreditOrder creditOrder = creditOrderService.getByOrderId(refundApply.getOrderId());
        boolean isCreditPaymentOrder = creditOrder != null;

        if (StrUtil.isNotBlank(param.getRefundNote())) {
            refundApply.setRefundNote(param.getRefundNote());
        }

        BigDecimal totalRefundAmount = this.checkAudit(param, refundApply, order);

        if (param.getRefundStatus() == 1) {
            if (isCreditPaymentOrder) {
                this.handleCreditOrderRefund(param, refundApply, creditOrder, totalRefundAmount);
            } else {
                this.createRefundFlow(param, refundApply, order);
            }
        }

        if (StrUtil.isNotBlank(param.getPaymentVoucher())
                && StrUtil.isNotBlank(param.getOfflineBalance())
                && new BigDecimal(param.getOfflineBalance()).compareTo(BigDecimal.ZERO) > 0) {
            refundApply.setPaymentVoucher(param.getPaymentVoucher());
        }

        this.updateById(refundApply);
        wechatLiveOrderSyncService.onRefundChanged(this.getById(refundApply.getRefundId()), EVENT_REFUND_UPDATED);

        // 只有审核通过时才执行以下操作
        if (param.getRefundStatus() == 1) {
            // 退回库存
            aftersalesItemService.returnStock(refundApply.getAftersaleId());

            // 返还用户下单时使用的积分（积分兑换订单）
            this.returnUsePoints(refundApply, order);

            // 处理分销订单佣金：根据售后商品项精确匹配，支持部分退货按比例扣减
            this.handleSalesmanOrderOnRefund(refundApply);

            // 自动退款走的也是本方法，其协商记录已由售后侧写入，此处只补财务人工审核通过的留痕
            if (!Integer.valueOf(1).equals(param.getSystemAutoRefund())) {
                this.addFinanceAuditPassedLog(refundApply, totalRefundAmount);
            }
        }
        // 发送退款消息
//        if (refundApply.getRefundStatus() == 2) {
//            messageService.sendUserMessage(refundApply.getUserId(), refundApply.getOrderId(), MessageTypeEnum.ORDER_REFUND, totalRefundAmount);
//        }
    }

    private void addFinanceAuditPassedLog(RefundApply refundApply, BigDecimal totalRefundAmount) {
        Integer aftersaleId = refundApply.getAftersaleId();
        if (aftersaleId == null || aftersaleId <= 0) {
            return;
        }
        Aftersales aftersales = aftersalesService.getById(aftersaleId);
        if (aftersales == null) {
            return;
        }
        String amount = safeAmount(totalRefundAmount).setScale(2, RoundingMode.HALF_UP).toPlainString();
        aftersalesLogService.addAftersaleLog(aftersaleId,
                "财务审核通过，已执行退款：¥" + amount,
                aftersales.getShopId(), aftersales.getVendorId());
    }

    private BigDecimal checkAudit(RefundApplyAuditParam param, RefundApply refundApply, Order order) {
        // 排除退款成功的支付金额
        List<RefundApply> refundApplies = this.lambdaQuery()
                .eq(RefundApply::getOrderId, refundApply.getOrderId())
                // 按退款类型隔离：避免将来扩 refundType 多类型后按 orderId 跨类型串单
                .eq(RefundApply::getRefundType, refundApply.getRefundType())
                .in(RefundApply::getRefundStatus, 1, 2).list();
        BigDecimal completeOnlineBalance = refundApplies.stream().map(RefundApply::getOnlineBalance).reduce(BigDecimal.ZERO, BigDecimal::add);

        // 售后协商可退金额
        Aftersales aftersales = aftersalesService.getById(refundApply.getAftersaleId());

        BigDecimal refundAmount;

        List<OrderItem> orderItems = orderItemMapper.selectList(
                Wrappers.lambdaQuery(OrderItem.class).eq(OrderItem::getOrderId, order.getOrderId())
        );
        List<AftersalesItem> aftersalesItems = aftersalesItemService.lambdaQuery()
                .eq(AftersalesItem::getAftersaleId, refundApply.getAftersaleId())
                .list();
        List<Aftersales> orderCompleteAftersales = aftersalesService.lambdaQuery()
                .eq(Aftersales::getOrderId, refundApply.getOrderId())
                .eq(Aftersales::getStatus, AftersalesStatusEnum.COMPLETE.getCode())
                .list();
        List<Integer> aftersalesIds = orderCompleteAftersales.stream().map(Aftersales::getAftersaleId).toList();
        List<AftersalesItem> orderAllAftersalesItems = aftersalesIds.isEmpty()
                ? Collections.emptyList()
                : aftersalesItemService.lambdaQuery().in(AftersalesItem::getAftersaleId, aftersalesIds).list();

        List<RefundApply> allRefundApplies = this.lambdaQuery()
                .eq(RefundApply::getOrderId, refundApply.getOrderId())
                // 按退款类型隔离：避免将来扩 refundType 多类型后按 orderId 跨类型串单
                .eq(RefundApply::getRefundType, refundApply.getRefundType()).list();
        refundAmount = calculateFinanceRefundAmount(order, aftersales, orderItems, aftersalesItems, allRefundApplies, orderAllAftersalesItems);

        BigDecimal effectiveOnlineBalance = order.getOnlinePaidAmount().subtract(completeOnlineBalance);
        if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            effectiveOnlineBalance = effectiveOnlineBalance.compareTo(refundAmount) > 0 ? refundAmount : effectiveOnlineBalance;
        }
        // 真实的线上剩余可退金额
        Assert.isTrue(new BigDecimal(param.getOnlineBalance()).compareTo(effectiveOnlineBalance) <= 0, () -> new GlobalException("填写的线上金额不能超过可退的在线支付金额"));
        Assert.isTrue(new BigDecimal(param.getRefundBalance()).compareTo(refundAmount) <= 0, () -> new GlobalException("填写的余额不能超过可退款总金额"));
        Assert.isTrue(new BigDecimal(param.getOfflineBalance()).compareTo(refundAmount) <= 0, () -> new GlobalException("填写的线下金额不能超过可退款总金额"));

        // 计算退款总金额（包含账期退款金额）
        BigDecimal creditRefundAmount = StrUtil.isNotEmpty(param.getCreditRefundAmount())
                ? new BigDecimal(param.getCreditRefundAmount()) : BigDecimal.ZERO;
        BigDecimal totalRefundAmount = new BigDecimal(param.getOnlineBalance())
                .add(new BigDecimal(param.getOfflineBalance()))
                .add(new BigDecimal(param.getRefundBalance()))
                .add(creditRefundAmount);

        BigDecimal expectedRefundAmount = refundAmount;
        boolean systemAutoRefund = Integer.valueOf(1).equals(param.getSystemAutoRefund());
        boolean hasPointRelatedAmount = (order.getPointsAmount() != null && order.getPointsAmount().compareTo(BigDecimal.ZERO) > 0)
                || (order.getUsePoints() != null && order.getUsePoints() > 0)
                || Integer.valueOf(1).equals(order.getIsExchangeOrder());
        boolean hasOrderLevelDiscount = (order.getDiscountAmount() != null && order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0)
                || (order.getCouponAmount() != null && order.getCouponAmount().compareTo(BigDecimal.ZERO) > 0);
        if (systemAutoRefund && (hasPointRelatedAmount || hasOrderLevelDiscount)) {
            BigDecimal processedBalance = refundApplies.stream()
                    .map(RefundApply::getRefundBalance)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal processedOffline = refundApplies.stream()
                    .map(RefundApply::getOfflineBalance)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal balanceAvailable = safeAmount(order.getBalance()).subtract(processedBalance).max(BigDecimal.ZERO);
            BigDecimal offlineAvailable = safeAmount(order.getOfflinePaidAmount()).subtract(processedOffline).max(BigDecimal.ZERO);
            BigDecimal autoRefundableMoney = effectiveOnlineBalance
                    .add(balanceAvailable)
                    .add(offlineAvailable)
                    .add(creditRefundAmount);
            expectedRefundAmount = refundAmount.min(autoRefundableMoney);
        }

        // 可退款总额如果为0则可以跳过判断
        if (BigDecimal.ZERO.compareTo(expectedRefundAmount) != 0 && param.getRefundStatus() == 1) {
            Assert.isTrue(totalRefundAmount.compareTo(BigDecimal.ZERO) != 0, () -> new GlobalException("退款总金额不能为0"));
        }

        BigDecimal finalRefundAmount = expectedRefundAmount;
        Assert.isTrue(totalRefundAmount.compareTo(expectedRefundAmount) == 0, () -> new GlobalException("填写的退款总金额要等于售后可退款金额:" + finalRefundAmount));

        return totalRefundAmount;
    }

    private void createRefundFlow(RefundApplyAuditParam param, RefundApply refundApply, Order order) {
        RefundLog refundLog = RefundLog.builder()
                .orderId(refundApply.getOrderId())
                .refundApplyId(refundApply.getRefundId())
                .userId(refundApply.getUserId())
                .build();

        // 线上退款
        BigDecimal onlineBalance = new BigDecimal(param.getOnlineBalance());
        if (onlineBalance.compareTo(BigDecimal.ZERO) > 0) {
            refundLog.setLogId(null);
            refundLog.setRefundType(RefundLogTypeEnum.ONLINE_REFUND.getCode());
            refundLog.setRefundAmount(onlineBalance);
            refundLog.setAddTime(StringUtils.getCurrentTime());
            refundLogService.save(refundLog);

            boolean onlineCompleted = this.refundFlow(order, onlineBalance, param.getRefundNote(), refundApply);
            refundApply.setIsOnline(onlineCompleted ? 2 : Constants.YES);
            refundApply.setOnlineBalance(onlineBalance);
        }

        // 余额退款
        BigDecimal refundBalance = new BigDecimal(param.getRefundBalance());
        if (refundBalance.compareTo(BigDecimal.ZERO) > 0) {
            refundLog.setLogId(null);
            refundLog.setRefundType(RefundLogTypeEnum.BALANCE_REFUND.getCode());
            refundLog.setRefundAmount(refundBalance);
            refundLog.setAddTime(StringUtils.getCurrentTime());
            refundLogService.save(refundLog);

            userBalanceService.incBalance(refundBalance, refundApply.getUserId(), "退款到账");
            refundApply.setIsReceive(2);
            refundApply.setRefundBalance(refundBalance);
        }

        // 线下退款
        BigDecimal offlineBalance = new BigDecimal(param.getOfflineBalance());
        if (offlineBalance.compareTo(BigDecimal.ZERO) > 0) {
            refundLog.setLogId(null);
            refundLog.setRefundType(RefundLogTypeEnum.OFFLINE_REFUND.getCode());
            refundLog.setRefundAmount(offlineBalance);
            refundLog.setAddTime(StringUtils.getCurrentTime());
            refundLogService.save(refundLog);

            refundApply.setIsOffline(1);
            refundApply.setOfflineBalance(offlineBalance);
            refundApply.setPaymentVoucher(param.getPaymentVoucher());
            refundApply.setRefundStatus(1);
        }

        handleRefundPostProcess(refundApply, order);

        if (refundApply.getIsOffline() != 1 && refundApply.getIsOnline() != 1 && refundApply.getIsReceive() != 1) {
            refundApply.setRefundStatus(2);
            // 退款成功，扣减成长值并重新评估会员等级（可能降级）
            userRankService.reduceGrowth(refundApply.getRefundId().longValue());
        }
    }

    private void handleCreditOrderRefund(RefundApplyAuditParam param, RefundApply refundApply, CreditOrder creditOrder, BigDecimal totalRefundAmount) {
        // 优先使用前端传入的账期退款金额，否则使用计算的总退款金额
        BigDecimal refundAmount;
        if (StrUtil.isNotEmpty(param.getCreditRefundAmount())) {
            refundAmount = new BigDecimal(param.getCreditRefundAmount());
        } else {
            refundAmount = Optional.ofNullable(totalRefundAmount).orElse(BigDecimal.ZERO);
        }
        if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            creditUserService.restoreAmount(refundApply.getUserId(), refundAmount,
                    "财务审核退款恢复账期额度，退款申请ID：" + refundApply.getRefundId());

            BigDecimal originCreditAmount = Optional.ofNullable(creditOrder.getCreditAmount()).orElse(BigDecimal.ZERO);
            BigDecimal newCreditAmount = originCreditAmount.subtract(refundAmount).max(BigDecimal.ZERO);

            CreditOrder creditOrderUpdate = new CreditOrder();
            creditOrderUpdate.setCreditOrderId(creditOrder.getCreditOrderId());
            creditOrderUpdate.setCreditAmount(newCreditAmount);
            creditOrderUpdate.setUpdateTime(StringUtils.getCurrentTime());
            // 无论退款后金额是否为0，都保持待结算状态，在待结算列表中展示
            creditOrderUpdate.setSettlementStatus(CreditOrderSettlementStatusEnum.UNSETTLED.getCode());
            creditOrderService.updateById(creditOrderUpdate);

            Order order = orderService.getById(refundApply.getOrderId());
            if (order != null) {
                order.setCreditAmount(newCreditAmount);
                orderService.updateById(order);
            }
        }

        refundApply.setRefundStatus(2);
        refundApply.setRefundNote(param.getRefundNote());
        refundApply.setIsOnline(0);
        refundApply.setIsOffline(0);
        refundApply.setIsReceive(0);
        refundApply.setOnlineBalance(BigDecimal.ZERO);
        refundApply.setOfflineBalance(BigDecimal.ZERO);
        // 账期支付退款：余额退款金额为0，账期退款金额从售后可退金额获取
        refundApply.setRefundBalance(BigDecimal.ZERO);
        // 退款成功，扣减成长值并重新评估会员等级（可能降级）
        userRankService.reduceGrowth(refundApply.getRefundId().longValue());
    }

    private void handleRefundPostProcess(RefundApply refundApply, Order order) {
        List<Aftersales> aftersalesList = aftersalesService.lambdaQuery()
                .eq(Aftersales::getOrderId, order.getOrderId())
                .eq(Aftersales::getStatus, AftersalesStatusEnum.COMPLETE.getCode())
                .list();
        if (CollUtil.isNotEmpty(aftersalesList)) {
            double refundAmount = aftersalesList.stream().mapToDouble(item -> item.getRefundAmount().doubleValue()).sum();
            // 已发货订单不退运费，退款金额与订单总额不会相等，故同时按"订单商品是否已被整单售后完成覆盖"判定
            boolean fullyRefunded = new BigDecimal(refundAmount).compareTo(order.getTotalAmount()) == 0
                    || aftersalesService.isFullOrderAftersalesCompleted(order.getOrderId());
            if (fullyRefunded) {
                String cancelRemark = aftersalesList.stream()
                        .filter(item -> Objects.equals(item.getAftersaleId(), refundApply.getAftersaleId()))
                        .map(Aftersales::getAftersaleReason)
                        .filter(StrUtil::isNotBlank)
                        .findFirst()
                        .orElse(null);
                var orderUpdate = orderService.lambdaUpdate()
                        .eq(Order::getOrderId, order.getOrderId())
                        .set(Order::getPayStatus, 4)
                        .set(Order::getOrderStatus, OrderStatusEnum.CANCELLED.getCode());
                if (cancelRemark != null) {
                    orderUpdate.set(Order::getCancelRemark, cancelRemark);
                }
                orderUpdate.update();

                // 拼团订单全额退款：将待成团记录标记为拼团失败
                if (OrderType.PIN.getCode() == order.getOrderType()) {
                    grouponPromotionRecordService.lambdaUpdate()
                            .eq(GrouponPromotionRecord::getOrderId, order.getOrderId())
                            .eq(GrouponPromotionRecord::getJoinStatus, JoinStatusEnum.WAIT_GROUP.getCode())
                            .set(GrouponPromotionRecord::getJoinStatus, JoinStatusEnum.FAIL.getCode())
                            .update();
                }
            }
        }

        // 2026-03 规则调整：退款后不再扣回下单赠送积分
//        UserPointsLog userPointsLog = userPointsLogService.lambdaQuery()
//                .eq(UserPointsLog::getRelationId, order.getOrderId())
//                .last("limit 1")
//                .one();
//        if (userPointsLog != null && userPointsLog.getPoints() > 0) {
//            List<RefundApply> allRefundApplies = this.lambdaQuery().eq(RefundApply::getOrderId, refundApply.getOrderId()).list();
//            long isReviewCount = allRefundApplies.stream().filter(item -> item.getRefundStatus() == 0).count();
//            if (isReviewCount == 1) {
//                userPointsLogService.subPoints(userPointsLog.getPoints(), refundApply.getUserId(), "退货扣积分", order.getOrderId());
//            }
//        }
    }

    @Transactional
    @Override
    public void offlineAudit(OfflineAuditParam param) {
        RefundApply refundApply = this.getById(param.getRefundId());
        Assert.notNull(refundApply, () -> new GlobalException("该申请不存在"));
        Assert.isTrue(refundApply.getIsOffline() == 1, () -> new GlobalException("该状态下不能确认线下已退款"));

        refundApply.setIsOffline(2);

        if (StrUtil.isNotBlank(param.getPaymentVoucher())) {
            refundApply.setPaymentVoucher(param.getPaymentVoucher());
        }
        if (StrUtil.isNotBlank(param.getRefundNote())) {
            refundApply.setRefundNote(param.getRefundNote());
        }

        if (refundApply.getIsOffline() != 1 && refundApply.getIsOnline() != 1 && refundApply.getIsReceive() != 1) {
            refundApply.setRefundStatus(2);

            // 扣减成长值
            userRankService.reduceGrowth(param.getRefundId());
        }

        this.updateById(refundApply);

        BigDecimal offBal = refundApply.getOfflineBalance() != null ? refundApply.getOfflineBalance() : BigDecimal.ZERO;
        BigDecimal onBal = refundApply.getOnlineBalance() != null ? refundApply.getOnlineBalance() : BigDecimal.ZERO;
        BigDecimal refBal = refundApply.getRefundBalance() != null ? refundApply.getRefundBalance() : BigDecimal.ZERO;
        BigDecimal totalRefundAmount = offBal.add(onBal).add(refBal);

        try {
            messageService.sendUserMessage(refundApply.getUserId(), refundApply.getOrderId(), MessageTypeEnum.ORDER_REFUND, totalRefundAmount);
        } catch (Exception ex) {
            log.error("退款通知 ORDER_REFUND 发送失败，不影响退款主流程，refundApplyId={}: {}", refundApply.getRefundId(), ex.getMessage(), ex);
        }
    }

    @Override
    public void create(RefundApplySaveParam param) {
        RefundApply refundApply = new RefundApply();
        BeanUtils.copyProperties(param, refundApply);
        refundApply.setAddTime(StringUtils.getCurrentTime());
        this.save(refundApply);
    }

    @Override
    public void update(RefundApplyEditParam param) {
        RefundApply refundApply = new RefundApply();
        BeanUtils.copyProperties(param, refundApply);
        this.updateById(refundApply);
    }

    @Override
    public RefundApply getByPaylogRefundId(Integer refundPayLogId) {
        RefundApplyPaylogRefund refundApplyPaylogRefund = refundApplyPaylogRefundService.lambdaQuery().eq(RefundApplyPaylogRefund::getPaylogRefundId, refundPayLogId).one();
        RefundApply apply;
        if (Objects.isNull(refundApplyPaylogRefund)) {
            //兼容老版本
            apply = this.lambdaQuery().eq(RefundApply::getPaylogRefundId, refundPayLogId).one();
        } else {
            Integer refundApplyId = refundApplyPaylogRefund.getRefundApplyId();
            apply = getById(refundApplyId);
        }
        return apply;
    }

    private boolean refundFlow(Order order, BigDecimal refundAmount, String paylogDesc, RefundApply refundApply) {
        // 1. 查询订单的所有已支付的支付记录
        List<Paylog> paylogs = paylogService.lambdaQuery().eq(Paylog::getOrderSn, order.getOrderSn()).eq(Paylog::getPayStatus, 1).isNotNull(Paylog::getTransactionId).orderByAsc(Paylog::getAddTime)
                .list();

        if (paylogs.isEmpty()) {
            throw new GlobalException("订单支付记录不存在");
        }

        // 2. 判断是否为预售订单
        boolean isPresaleOrder = order.getIsPresale() != null && order.getIsPresale() == 1 && order.getPresaleType() != null && order.getPresaleType() == 2;  // 定金预售


        boolean completed;
        if (isPresaleOrder && paylogs.size() > 1) {
            // 预售订单且有多次支付
            completed = processPresaleOrderRefund(paylogs, refundAmount, paylogDesc, refundApply, order);
        } else {
            // 单次支付，直接退款
            PaylogRefund paylogRefund = processPaylogRefund(paylogs.get(0), refundAmount, paylogDesc, refundApply, "");
            completed = Objects.equals(paylogRefund.getStatus(), 1);
        }

        // 3. 兼容旧数据：更新 refund_apply 表的 paylog_refund_id 字段
        if (refundApply.getPaylogRefundId() != null) {
            RefundApply update = new RefundApply();
            update.setRefundId(refundApply.getRefundId());
            update.setPaylogRefundId(refundApply.getPaylogRefundId());
            updateById(update);
        }
        return completed;
    }

    /**
     * 处理预售订单的退款（按支付金额从大到小优先退款）
     */
    private boolean processPresaleOrderRefund(List<Paylog> paylogs, BigDecimal totalRefundAmount, String paylogDesc, RefundApply refundApply, Order order) {
        boolean allCompleted = true;
        BigDecimal remainingRefundAmount = totalRefundAmount;
        try {
            // 按支付金额从大到小排序
            List<Paylog> sortedPaylogs = new ArrayList<>(paylogs);
            sortedPaylogs.sort((p1, p2) -> p2.getPayAmount().compareTo(p1.getPayAmount()));

            //排除掉paylog是余额的情况
            sortedPaylogs = sortedPaylogs.stream().filter(paylog -> !"balance".equals(paylog.getPayCode())).toList();

            // 从最大的支付记录开始退款
            for (int i = 0; i < sortedPaylogs.size(); i++) {
                if (remainingRefundAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }

                Paylog paylog = sortedPaylogs.get(i);
                BigDecimal payAmount = paylog.getPayAmount();

                // 计算本次退款金额
                BigDecimal refundAmountForThisPaylog;
                if (remainingRefundAmount.compareTo(payAmount) >= 0) {
                    // 退款金额大于等于当前支付金额，全额退款
                    refundAmountForThisPaylog = payAmount;
                } else {
                    // 退款金额小于当前支付金额，部分退款
                    refundAmountForThisPaylog = remainingRefundAmount;
                }

                // 确定退款标签
                String refundLabel = "第" + (i + 1) + "笔";
                // 如果是预售订单，尝试标注定金或尾款
                try {
                    OrderPresaleVO presaleVO = orderPresaleService.getPresaleVOByOrderId(order.getOrderId());
                    if (presaleVO != null && presaleVO.getDepositAmount() != null) {
                        // 判断是定金还是尾款
                        if (payAmount.compareTo(presaleVO.getDepositAmount()) == 0) {
                            refundLabel = "定金";
                        } else if (presaleVO.getFinalPaymentAmount() != null && payAmount.compareTo(presaleVO.getFinalPaymentAmount()) == 0) {
                            refundLabel = "尾款";
                        }
                    }
                } catch (Exception e) {
                    // 忽略标签判断异常
                }

                // 处理退款
                PaylogRefund paylogRefund = processPaylogRefund(paylog, refundAmountForThisPaylog, paylogDesc, refundApply, refundLabel);
                allCompleted = allCompleted && Objects.equals(paylogRefund.getStatus(), 1);

                // 更新剩余退款金额
                remainingRefundAmount = remainingRefundAmount.subtract(refundAmountForThisPaylog);
            }
        } catch (Exception e) {
            log.error("处理预售订单退款异常，订单ID: " + order.getOrderId(), e);
            throw e;
        }
        return allCompleted && remainingRefundAmount.compareTo(BigDecimal.ZERO) <= 0;
    }


    /**
     * 处理单笔支付记录的退款
     */
    private PaylogRefund processPaylogRefund(Paylog paylog, BigDecimal refundAmount, String paylogDesc, RefundApply refundApply, String refundLabel) {
        if ("wechat".equals(paylog.getPayCode())) {
            // 与所有分账发起路径锁同一订单，由 audit/外层售后事务持有至提交或回滚。
            Order locked = orderService.lambdaQuery().eq(Order::getOrderId, paylog.getOrderId())
                    .last("FOR UPDATE").one();
            if (locked == null) {
                throw new GlobalException("退款订单不存在");
            }
        }
        // 发起前先在本地校验可退余额：此前无任何前置判断，已退光的支付记录也会一路打到渠道，
        // 由渠道报「订单已全额退款」兜底，用户可反复提交且每次都留下一条作废流水

        PaylogRefund existingRefund = findExistingRefund(refundApply, paylog);
        if (existingRefund != null) {
            Assert.isTrue(existingRefund.getRefundAmount() != null
                            && existingRefund.getRefundAmount().compareTo(refundAmount) == 0,
                    () -> new GlobalException("原退款流水金额与本次审核金额不一致，不能复用原退款单号"));
            if (Objects.equals(existingRefund.getStatus(), 1)) {
                // 已明确成功的同一分量只回显成功，不再次调用渠道。
                return existingRefund;
            }
            if (!Objects.equals(existingRefund.getStatus(), PAYLOG_REFUND_STATUS_FAILED)) {
                // 0 表示渠道结果未知/处理中，不能因为后台重复点击而换单号重放。
                throw new GlobalException("该退款已有未确认的渠道流水，请先核对退款结果");
            }
        }

        String refundSn = existingRefund == null ? createRefundSn() : existingRefund.getRefundSn();
        Integer adminId = SecurityUtils.getCurrentAdminId();

        String finalPaylogDesc = paylogDesc;
        if (StrUtil.isNotBlank(refundLabel)) {
            finalPaylogDesc = refundLabel + "退款：" + paylogDesc;
        }
        String refundDescription = finalPaylogDesc;


        PaylogRefund paylogRefund = existingRefund == null
                ? PaylogRefund.builder()
                    .paylogId(paylog.getPaylogId())
                    .refundSn(refundSn).paylogDesc(refundDescription)
                    .orderId(paylog.getOrderId()).refundAmount(refundAmount)
                    .addTime(System.currentTimeMillis() / 1000).adminId(adminId).payCode(paylog.getPayCode()).build()
                : existingRefund;
        // 独立事务先行提交：外层审核事务在渠道扣款成功后仍可能因后续步骤失败而整体回滚，
        // 若流水随之消失，这笔已退出去的钱既无据可查，也不再占用可退额度，用户重提会二次退款
        refundRecordTx.executeWithoutResult(status -> {
            // 在锁内用新事务读取占额，避免外层事务旧快照漏掉另一笔已提交退款。
            assertRefundable(paylog, refundAmount);
            if (existingRefund == null) {
                if (!paylogRefundService.save(paylogRefund) || paylogRefund.getPaylogRefundId() == null) {
                    throw new GlobalException("退款流水保存失败，未调用渠道");
                }
                if (!refundApplyPaylogRefundService.createRelation(refundApply.getRefundId(), paylogRefund.getPaylogRefundId())) {
                    throw new GlobalException("退款申请与退款流水关联保存失败，未调用渠道");
                }
            } else {
                boolean reopened = paylogRefundService.lambdaUpdate()
                        .eq(PaylogRefund::getPaylogRefundId, existingRefund.getPaylogRefundId())
                        .eq(PaylogRefund::getStatus, PAYLOG_REFUND_STATUS_FAILED)
                        .set(PaylogRefund::getStatus, 0)
                        .set(PaylogRefund::getAdminId, adminId)
                        .set(PaylogRefund::getPaylogDesc, refundDescription)
                        .update();
                if (!reopened) {
                    throw new GlobalException("退款流水状态已变化，请刷新后重试");
                }
            }
        });

        // 兼容旧数据：设置第一个退款记录ID到 refund_apply 表
        if (refundApply.getPaylogRefundId() == null || refundApply.getPaylogRefundId()<=0) {
            refundApply.setPaylogRefundId(paylogRefund.getPaylogRefundId());
            refundApply.setRefundStatus(null);  // 防止支付宝支付时状态被修改
        }

        // 调用支付接口进行退款
        PayNotifyVO payNotifyVO;


        BigDecimal totalAmount = paylog.getPayAmount();

        //查询父paylog(拆单)，拿到paysn, 同时拿到父订单id的总金额
        String paySn = paylog.getPaySn();
        Integer orderId = paylog.getOrderId();
        Order curOrder = orderService.lambdaQuery().eq(Order::getOrderId, orderId).one();
        Integer parentOrderId = curOrder.getParentOrderId();
        if(parentOrderId!=null){
            Paylog parentPaylog = paylogService.lambdaQuery().eq(Paylog::getOrderId, parentOrderId)
                    .eq(Paylog::getPaySn, paySn).one();
            if(parentPaylog!=null){
                totalAmount = parentPaylog.getPayAmount();
            }
        }

        boolean refundRequested = false;
        try {
            // 回退失败时尚未发起支付退款，也要将独立提交的退款流水置为失败，释放占额供重试。
            // 汇付/支付宝的回退已内联在各自 payRefund 内，避免同一交易双重回退。

            // 微信线上分账的二级商户号候选值（按店铺现查）；其余渠道恒传 null。
            String wechatSubMchId = "wechat".equals(paylog.getPayCode())
                    ? wechatPayModeResolver.resolveRefundSubMchId(paylog.getOrderId())
                    : null;

            refundRequested = true;
            payNotifyVO = switch (paylog.getPayCode()) {
                case "wechat" ->
                        wechatPayService.payRefund(paylog.getPaySn(), refundSn, refundAmount, totalAmount, paylog.getPayCode(), wechatSubMchId);
                case "alipay" ->
                        aliPayService.payRefund(paylog.getPaySn(), refundSn, refundAmount, totalAmount, paylog.getPayCode(), null);
                case "paypal" ->
                        paypalService.payRefund(paylog.getPaySn(), refundSn, refundAmount, totalAmount, paylog.getPayCode(), null);
                // 新增渠道（汇付/ApplePay/拉卡拉）走局部工厂；未登记则视为不支持
                default -> {
                    PayService newChannel = newPayChannelRegistry.get(paylog.getPayCode());
                    if (newChannel == null) {
                        throw new GlobalException("该支付方式不存在");
                    }
                    yield newChannel.payRefund(paylog.getPaySn(), refundSn, refundAmount, totalAmount, paylog.getPayCode(), null);
                }
            };
        } catch (Exception e) {
            log.error("调用支付退款接口失败，退款单号: " + refundSn + "，错误: " + e.getMessage(), e);
            if (!refundRequested || e instanceof WechatPayServiceImpl.RefundNotRequestedException
                    || !"wechat".equals(paylog.getPayCode())) {
                markRefundRecordFailed(refundSn, "渠道调用前失败或其他渠道异常");
            }
            throw new GlobalException(e.getMessage());
        }

        if (payNotifyVO == null) {
            log.error("退款渠道未返回结果，refund_sn:{}, payCode:{}", refundSn, paylog.getPayCode());
            if (!"wechat".equals(paylog.getPayCode())) {
                markRefundRecordFailed(refundSn, "渠道未返回结果");
            }
            throw new GlobalException("退款失败：支付渠道未返回结果");
        }
        String refundCode = payNotifyVO.getCode();
        if ("SUCCESS".equals(refundCode)) {
            // 同步明确成功：立即回写退款成功
            paymentService.refundSuccess(refundSn);
        } else if ("FAIL".equals(refundCode)) {
            // 退款失败不再静默：抛出让整笔退款审核事务回滚，避免“退款失败却显示处理中(isOnline=1)”
            log.error("退款失败，refund_sn:{}, payCode:{}, msg:{}", refundSn, paylog.getPayCode(), payNotifyVO.getMessage());
            markRefundRecordFailed(refundSn, "渠道返回失败");
            throw new GlobalException("退款失败：" + payNotifyVO.getMessage());
        } else {
            // 处理中(PROCESSING)等异步渠道：保持 isOnline=1，等待退款异步通知(refundNotify)回写为已处理(2)
            log.info("退款受理处理中，等待异步通知回写，refund_sn:{}, payCode:{}, code:{}", refundSn, paylog.getPayCode(), refundCode);
        }

        PaylogRefund latest = paylogRefundService.getById(paylogRefund.getPaylogRefundId());
        return latest == null ? paylogRefund : latest;
    }

    /**
     * 找到本申请在同一支付记录上的历史退款流水。失败态可复用原 refundSn，
     * 申请中/成功态则分别代表结果未知/已完成，均禁止重新生成退款单号。
     */
    private PaylogRefund findExistingRefund(RefundApply refundApply, Paylog paylog) {
        List<RefundApplyPaylogRefund> relations = refundApplyPaylogRefundService.lambdaQuery()
                .eq(RefundApplyPaylogRefund::getRefundApplyId, refundApply.getRefundId()).list();
        PaylogRefund successful = null;
        PaylogRefund failed = null;
        for (RefundApplyPaylogRefund relation : relations) {
            PaylogRefund candidate = paylogRefundService.getById(relation.getPaylogRefundId());
            if (candidate != null && Objects.equals(candidate.getPaylogId(), paylog.getPaylogId())) {
                if (Objects.equals(candidate.getStatus(), 0)) {
                    return candidate;
                }
                if (Objects.equals(candidate.getStatus(), 1)) {
                    successful = candidate;
                } else if (failed == null || candidate.getPaylogRefundId() > failed.getPaylogRefundId()) {
                    failed = candidate;
                }
            }
        }
        if (successful != null) {
            return successful;
        }
        if (failed != null) {
            return failed;
        }
        // 兼容关联表上线前的旧记录。
        if (refundApply.getPaylogRefundId() != null) {
            PaylogRefund legacy = paylogRefundService.getById(refundApply.getPaylogRefundId());
            if (legacy != null && Objects.equals(legacy.getPaylogId(), paylog.getPaylogId())) {
                return legacy;
            }
        }
        return null;
    }


    /**
     * 校验本笔支付记录还能退多少。可退余额 = 实付金额 − 已占用（成功 + 处理中）。
     * <p>退款流水独立提交；明确失败释放占额，微信请求结果不明时保留占额，等待渠道确认。
     */
    private void assertRefundable(Paylog paylog, BigDecimal refundAmount) {
        BigDecimal paidAmount = paylog.getPayAmount() == null ? BigDecimal.ZERO : paylog.getPayAmount();
        BigDecimal occupied = paylogRefundService.sumOccupiedRefundAmount(paylog.getPaylogId());
        BigDecimal refundable = paidAmount.subtract(occupied);

        if (refundable.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("[退款前置校验] 支付记录已无可退余额, paylogId={}, paySn={}, 实付={}, 已占用={}",
                    paylog.getPaylogId(), paylog.getPaySn(), paidAmount, occupied);
            throw new GlobalException("该笔支付已全额退款，无需重复发起");
        }
        if (refundAmount != null && refundAmount.compareTo(refundable) > 0) {
            log.warn("[退款前置校验] 退款金额超出可退余额, paylogId={}, 本次={}, 可退={}",
                    paylog.getPaylogId(), refundAmount, refundable);
            throw new GlobalException("退款金额超出可退余额，本笔最多可退 " + refundable.setScale(2, RoundingMode.HALF_UP));
        }
    }

    /**
     * 渠道未扣款时把独立事务已提交的流水置为失败态，释放其占用的可退额度。
     *
     * <p>流水由 REQUIRES_NEW 先行提交，外层回滚已带不走它；若不置位，这条 0(申请中) 会被
     * {@code sumOccupiedRefundAmount} 永久计入已占用，导致该笔支付再也无法退款。
     *
     * <p>置位本身也走独立事务，且异常只记日志不外抛——此处已在处理失败路径，
     * 真正要抛给调用方的是渠道失败原因，不能被置位异常顶替。
     */
    private void markRefundRecordFailed(String refundSn, String reason) {
        try {
            refundRecordTx.executeWithoutResult(status -> paylogRefundService.lambdaUpdate()
                    .eq(PaylogRefund::getRefundSn, refundSn)
                    .eq(PaylogRefund::getStatus, 0)
                    .set(PaylogRefund::getStatus, PAYLOG_REFUND_STATUS_FAILED)
                    .update());
            log.warn("[退款] 流水置为失败态，refund_sn:{}, 原因:{}", refundSn, reason);
        } catch (Exception e) {
            log.error("[退款] 流水失败态持久化异常，需人工核对可退额度，refund_sn:{}", refundSn, e);
        }
    }

    private String createRefundSn() {
        String refundSn;
        int payLogRefundId;

        do {
            refundSn = RandomStringUtils.randomAlphanumeric(16);
            payLogRefundId = this.getPayLogRefundId(refundSn);
        } while (payLogRefundId > 0);

        return refundSn;
    }

    public int getPayLogRefundId(String refundSn) {
        if (StrUtil.isBlank(refundSn)) {
            return 0;
        }

        PaylogRefund paylogRefund = paylogRefundService.lambdaQuery()
                .eq(PaylogRefund::getRefundSn, refundSn)
                .one();

        return paylogRefund != null ? paylogRefund.getPaylogRefundId() : 0;
    }

    /**
     * 处理退款时的分销订单佣金
     * 根据售后商品项精确匹配分销订单，支持部分退货按比例扣减佣金
     *
     * @param refundApply 退款申请
     */
    private void handleSalesmanOrderOnRefund(RefundApply refundApply) {
        // 获取售后商品项列表
        List<AftersalesItem> aftersalesItems = aftersalesItemService.lambdaQuery()
                .eq(AftersalesItem::getAftersaleId, refundApply.getAftersaleId())
                .list();

        if (CollUtil.isEmpty(aftersalesItems)) {
            return;
        }

        for (AftersalesItem aftersalesItem : aftersalesItems) {
            // 根据售后商品项的orderItemId找到对应的分销订单
            List<SalesmanOrder> salesmanOrders = salesmanOrderService.lambdaQuery()
                    .eq(SalesmanOrder::getOrderId, refundApply.getOrderId())
                    .eq(SalesmanOrder::getItemId, aftersalesItem.getOrderItemId())
                    .list();

            if (CollUtil.isEmpty(salesmanOrders)) {
                continue;
            }

            // 获取原订单商品项，获取购买数量
            OrderItem orderItem = orderItemMapper.selectById(aftersalesItem.getOrderItemId());
            if (orderItem == null) {
                continue;
            }

            int buyQuantity = orderItem.getQuantity(); // 购买数量
            int refundQuantity = aftersalesItem.getNumber(); // 本次退货数量

            // 查询该商品项已完成的售后退货数量（不包括当前这次）
            List<Aftersales> completedAftersales = aftersalesService.lambdaQuery()
                    .eq(Aftersales::getOrderId, refundApply.getOrderId())
                    .eq(Aftersales::getStatus, AftersalesStatusEnum.COMPLETE.getCode())
                    .ne(Aftersales::getAftersaleId, refundApply.getAftersaleId())
                    .list();

            int previousRefundQuantity = 0;
            if (CollUtil.isNotEmpty(completedAftersales)) {
                List<Integer> completedAftersaleIds = completedAftersales.stream()
                        .map(Aftersales::getAftersaleId)
                        .toList();
                List<AftersalesItem> previousRefundItems = aftersalesItemService.lambdaQuery()
                        .in(AftersalesItem::getAftersaleId, completedAftersaleIds)
                        .eq(AftersalesItem::getOrderItemId, aftersalesItem.getOrderItemId())
                        .list();
                previousRefundQuantity = previousRefundItems.stream()
                        .mapToInt(AftersalesItem::getNumber)
                        .sum();
            }

            // 计算总退货数量
            int totalRefundQuantity = previousRefundQuantity + refundQuantity;

            for (SalesmanOrder salesmanOrder : salesmanOrders) {
                // 只处理待结算的分销订单
                if (salesmanOrder.getStatus() != 0) {
                    continue;
                }

                if (totalRefundQuantity >= buyQuantity) {
                    // 全部退货：佣金失效
                    salesmanOrder.setStatus(2);
                } else {
                    // 部分退货：按比例扣减佣金金额
                    // 剩余有效数量 = 购买数量 - 总退货数量
                    int remainQuantity = buyQuantity - totalRefundQuantity;
                    BigDecimal ratio = new BigDecimal(remainQuantity)
                            .divide(new BigDecimal(buyQuantity), 4, java.math.RoundingMode.HALF_UP);

                    // 获取原始佣金金额（首次退货时需要从当前金额反推原始金额）
                    BigDecimal originalAmount;
                    BigDecimal originalOrderAmount;
                    if (previousRefundQuantity > 0) {
                        // 之前已有退货，需要从原始购买数量反推
                        int previousRemainQuantity = buyQuantity - previousRefundQuantity;
                        BigDecimal previousRatio = new BigDecimal(previousRemainQuantity)
                                .divide(new BigDecimal(buyQuantity), 4, java.math.RoundingMode.HALF_UP);
                        originalAmount = salesmanOrder.getAmount()
                                .divide(previousRatio, 4, java.math.RoundingMode.HALF_UP);
                        originalOrderAmount = salesmanOrder.getOrderAmount()
                                .divide(previousRatio, 4, java.math.RoundingMode.HALF_UP);
                    } else {
                        // 首次退货，当前金额即为原始金额
                        originalAmount = salesmanOrder.getAmount();
                        originalOrderAmount = salesmanOrder.getOrderAmount();
                    }

                    // 计算新的佣金金额
                    BigDecimal newAmount = originalAmount.multiply(ratio)
                            .setScale(2, java.math.RoundingMode.HALF_UP);
                    BigDecimal newOrderAmount = originalOrderAmount.multiply(ratio)
                            .setScale(2, java.math.RoundingMode.HALF_UP);

                    salesmanOrder.setAmount(newAmount);
                    salesmanOrder.setOrderAmount(newOrderAmount);
                    // 状态保持为0（待结算）
                }
                salesmanOrderService.updateById(salesmanOrder);
            }
        }
    }

    /**
     * 返还用户下单时使用的积分（仅积分兑换订单）
     *
     * @param refundApply 退款申请
     * @param order       订单
     */
    private void returnUsePoints(RefundApply refundApply, Order order) {
        // 订单没有使用积分，直接返回
        if (order.getUsePoints() == null || order.getUsePoints() <= 0) {
            return;
        }

        // 2026-03 规则调整：仅积分兑换订单返还兑换积分，普通订单不返还下单使用积分
        if (order.getIsExchangeOrder() == null || order.getIsExchangeOrder() != 1) {
//            // 历史逻辑（已停用）：普通订单（积分抵扣）按数量比例返还
//            int currentReturnPoints = calculateNormalOrderReturnPoints(aftersalesItems, orderItems, order, refundApply);
            return;
        }

        // 获取本次售后的商品项
        List<AftersalesItem> aftersalesItems = aftersalesItemService.lambdaQuery()
                .eq(AftersalesItem::getAftersaleId, refundApply.getAftersaleId())
                .list();

        if (CollUtil.isEmpty(aftersalesItems)) {
            return;
        }

        // 获取订单所有商品项
        List<OrderItem> orderItems = orderItemMapper.selectList(
                Wrappers.lambdaQuery(OrderItem.class).eq(OrderItem::getOrderId, order.getOrderId())
        );

        // 积分兑换订单：按商品的实际兑换积分返还
        int currentReturnPoints = calculateExchangeOrderReturnPoints(aftersalesItems, orderItems);

        if (currentReturnPoints > 0) {
            String configVal = configService.getConfigVal(SettingsEnum.INTEGRAL_NAME);
            userPointsLogService.incPoints(
                    currentReturnPoints,
                    refundApply.getUserId(),
                    "退货退款返还" + (StrUtil.isNotBlank(configVal) ? configVal : "积分"),
                    order.getOrderId()
            );
        }
    }

    /**
     * 计算积分兑换订单应返还的积分
     * 根据退货商品的实际兑换积分计算
     */
    private int calculateExchangeOrderReturnPoints(List<AftersalesItem> aftersalesItems, List<OrderItem> orderItems) {
        int returnPoints = 0;

        // 构建 orderItemId -> OrderItem 映射
        Map<Integer, OrderItem> orderItemMap = orderItems.stream()
                .collect(Collectors.toMap(OrderItem::getItemId, item -> item));

        for (AftersalesItem aftersalesItem : aftersalesItems) {
            OrderItem orderItem = orderItemMap.get(aftersalesItem.getOrderItemId());
            if (orderItem == null) {
                continue;
            }

            // 根据商品ID和SKU获取积分兑换配置
            PointsExchange pointsExchange = pointsExchangeService.getInfoByProductId(
                    orderItem.getProductId(),
                    orderItem.getSkuId() != null ? orderItem.getSkuId() : 0
            );

            if (pointsExchange != null && pointsExchange.getExchangeIntegral() != null) {
                // 返还积分 = 单件商品兑换积分 * 退货数量
                returnPoints += pointsExchange.getExchangeIntegral() * aftersalesItem.getNumber();
            }
        }

        return returnPoints;
    }

    /**
     * 计算普通订单（积分抵扣）应返还的积分
     * 按退货商品数量比例计算
     */
    private int calculateNormalOrderReturnPoints(List<AftersalesItem> aftersalesItems, List<OrderItem> orderItems,
                                                  Order order, RefundApply refundApply) {
        // 计算订单商品总数量
        int totalOrderQuantity = orderItems.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        if (totalOrderQuantity <= 0) {
            return 0;
        }

        // 计算本次退货的商品数量
        int currentRefundQuantity = aftersalesItems.stream()
                .mapToInt(AftersalesItem::getNumber)
                .sum();

        // 查询已完成的售后退货数量（不包括当前这次）
        List<Aftersales> completedAftersales = aftersalesService.lambdaQuery()
                .eq(Aftersales::getOrderId, order.getOrderId())
                .eq(Aftersales::getStatus, AftersalesStatusEnum.COMPLETE.getCode())
                .ne(Aftersales::getAftersaleId, refundApply.getAftersaleId())
                .list();

        int previousRefundQuantity = 0;
        if (CollUtil.isNotEmpty(completedAftersales)) {
            List<Integer> completedAftersaleIds = completedAftersales.stream()
                    .map(Aftersales::getAftersaleId)
                    .toList();
            List<AftersalesItem> previousRefundItems = aftersalesItemService.lambdaQuery()
                    .in(AftersalesItem::getAftersaleId, completedAftersaleIds)
                    .list();
            previousRefundQuantity = previousRefundItems.stream()
                    .mapToInt(AftersalesItem::getNumber)
                    .sum();
        }

        // 计算已返还的积分（根据之前退货比例）
        int alreadyReturnedPoints = 0;
        if (previousRefundQuantity > 0) {
            alreadyReturnedPoints = (int) Math.round(
                    (double) order.getUsePoints() * previousRefundQuantity / totalOrderQuantity
            );
        }

        // 计算本次应返还的积分
        int totalRefundQuantity = previousRefundQuantity + currentRefundQuantity;
        int totalShouldReturnPoints;

        if (totalRefundQuantity >= totalOrderQuantity) {
            // 全部退货，返还全部剩余积分
            totalShouldReturnPoints = order.getUsePoints();
        } else {
            // 部分退货，按比例计算应返还的总积分
            totalShouldReturnPoints = (int) Math.round(
                    (double) order.getUsePoints() * totalRefundQuantity / totalOrderQuantity
            );
        }

        // 本次应返还积分 = 应返还总积分 - 已返还积分
        return totalShouldReturnPoints - alreadyReturnedPoints;
    }

    /**
     * 账期退款金额兼容老数据：售后已含运费则直接用售后金额，否则按财务原规则补运费。
     */
    private BigDecimal getCreditRefundAmount(Order order, Aftersales aftersales, List<OrderItem> orderItems,
                                             List<AftersalesItem> aftersalesItems, List<RefundApply> refundApplies,
                                             List<AftersalesItem> orderAllAftersalesItems) {
        return calculateFinanceRefundAmount(order, aftersales, orderItems, aftersalesItems, refundApplies, orderAllAftersalesItems);
    }

    private BigDecimal calculateFinanceRefundAmount(Order order, Aftersales aftersales, List<OrderItem> orderItems,
                                                    List<AftersalesItem> aftersalesItems, List<RefundApply> refundApplies,
                                                    List<AftersalesItem> orderAllAftersalesItems) {
        // 退款审核以售后协商金额为准，不再自动追加运费（管理员填多少退多少）
        return Optional.ofNullable(aftersales.getRefundAmount()).orElse(BigDecimal.ZERO);
    }

    private BigDecimal safeAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}
