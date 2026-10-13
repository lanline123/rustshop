package com.tigshop.service.pay;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.bo.pay.WechatPayContext;
import com.tigshop.bean.enums.order.OrderStatusEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.finance.SubMerchant;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.service.finance.SubMerchantService;
import com.tigshop.service.setting.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 微信支付下单形态判定器
 * <p>在支付创建时一次性算出 {@link WechatPayContext}，供下单、paylog 建行、回调与退款复用。
 * 判定键是「去重后的二级商户号数量」而非子单数：{@code splitOrderAtSubmit} 按 {@code (shopId, vendorId)}
 * 分组，单店多供应商也会拆出 shopId 相同的子单，按子单数判定会构造出 sub_mchid 重复的合单请求。
 * <p>任何判定异常或数据不一致都降级为 {@link com.tigshop.bean.enums.pay.WechatPayMode#DIRECT}，
 * 保证交易不被阻断，代价是该单不参与本次微信线上分账。
 *
 * @author Tigshop团队
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WechatPayModeResolver {

    private static final String PAY_CHANNEL_WECHAT = "WECHAT";

    /**
     * 微信合单支付 subOrders 条数上限，超限只能整单降级直连
     */
    private static final int MAX_COMBINE_SUB_ORDERS = 50;

    private final ConfigService configService;
    private final OrderMapper orderMapper;
    private final SubMerchantService subMerchantService;

    /**
     * 判定订单的微信下单形态。
     *
     * @param detail 订单详情，需已完成父单 paylog 创建（COMBINE 形态要取其 paySn 作合单商户单号）
     * @return 恒非 null；判定不通过时为 DIRECT 形态
     */
    public WechatPayContext resolve(OrderVO detail) {
        try {
            return doResolve(detail);
        } catch (Exception e) {
            log.error("[微信支付形态] 判定异常，降级直连支付, orderId={}", detail == null ? null : detail.getOrderId(), e);
            return WechatPayContext.direct();
        }
    }

    /**
     * 是否启用微信线上分账（服务商模式 + 线上分账方式 + 渠道单选微信 + 总开关），
     * 口径对齐 D1133 多分账渠道路由。
     */
    public boolean isOnlineProfitSharing() {
        if (!configService.isProfitSharingEnabled()) {
            return false;
        }
        String mchidType = configService.getConfigVal(SettingsEnum.WECHAT_MCHID_TYPE);
        String splitMethod = configService.getConfigVal(SettingsEnum.SPLIT_PAYMENT_METHOD);
        String channel = configService.getConfigVal(SettingsEnum.PROFIT_SHARING_ONLINE_CHANNEL);
        boolean channelWechat = StrUtil.isBlank(channel) || "wechat".equals(StrUtil.trimToEmpty(channel));
        return Objects.equals("2", StrUtil.trimToEmpty(mchidType))
                && Objects.equals("2", StrUtil.trimToEmpty(splitMethod))
                && channelWechat;
    }

    private WechatPayContext doResolve(OrderVO detail) {
        if (detail == null || detail.getOrderId() == null) {
            return WechatPayContext.direct();
        }
        if (!isOnlineProfitSharing()) {
            return WechatPayContext.direct();
        }
        BigDecimal parentUnpaid = detail.getUnpaidAmount();
        if (parentUnpaid == null || parentUnpaid.compareTo(BigDecimal.ZERO) <= 0) {
            // 余额全额抵扣等场景不进微信支付链路，提前短路
            return WechatPayContext.direct();
        }

        List<Order> children = listValidChildren(detail.getOrderId());
        return children.isEmpty()
                ? resolveSingleOrder(detail)
                : resolveSplitOrder(detail, children, parentUnpaid);
    }

    /**
     * 未拆单：直接按父单自身店铺解析二级商户
     */
    private WechatPayContext resolveSingleOrder(OrderVO detail) {
        Integer shopId = detail.getShopId();
        if (shopId == null || shopId < 0) {
            // 无店铺归属（充值等），无从解析收款方
            return WechatPayContext.direct();
        }
        SubMerchant subMerchant = subMerchantService.getActiveByShop(shopId.longValue(), PAY_CHANNEL_WECHAT);
        if (subMerchant == null || StrUtil.isBlank(subMerchant.getSubMchId())) {
            log.info("[微信支付形态] 店铺[{}]未激活微信二级商户，降级直连支付, orderId={}", shopId, detail.getOrderId());
            return WechatPayContext.direct();
        }
        return WechatPayContext.partner(subMerchant.getSubMchId(), subMerchant.getSubAppid(),
                hasProfitSharingTarget(shopId));
    }

    /**
     * 提交时已拆单：逐子单解析二级商户，任一未进件则整单降级直连
     */
    private WechatPayContext resolveSplitOrder(OrderVO detail, List<Order> children, BigDecimal parentUnpaid) {
        List<WechatPayContext.SubOrderCtx> subOrders = new ArrayList<>(children.size());
        for (Order child : children) {
            Integer shopId = child.getShopId();
            if (shopId == null || shopId < 0) {
                log.info("[微信支付形态] 子单[{}]无店铺归属，整单降级直连支付, orderId={}", child.getOrderId(), detail.getOrderId());
                return WechatPayContext.direct();
            }
            SubMerchant subMerchant = subMerchantService.getActiveByShop(shopId.longValue(), PAY_CHANNEL_WECHAT);
            if (subMerchant == null || StrUtil.isBlank(subMerchant.getSubMchId())) {
                // 合单要求每个子单都是二级商户，做不到部分子单直连，故整单降级
                log.info("[微信支付形态] 店铺[{}]未激活微信二级商户，整单降级直连支付, orderId={}", shopId, detail.getOrderId());
                return WechatPayContext.direct();
            }
            WechatPayContext.SubOrderCtx ctx = new WechatPayContext.SubOrderCtx();
            ctx.setOrderId(child.getOrderId());
            ctx.setOrderSn(child.getOrderSn());
            ctx.setShopId(shopId);
            ctx.setSubMchId(subMerchant.getSubMchId());
            ctx.setSubAppId(subMerchant.getSubAppid());
            ctx.setAmount(child.getUnpaidAmount() == null ? BigDecimal.ZERO : child.getUnpaidAmount());
            ctx.setProfitSharing(hasProfitSharingTarget(shopId));
            subOrders.add(ctx);
        }

        if (!isAmountConserved(subOrders, parentUnpaid, detail)) {
            return WechatPayContext.direct();
        }

        Set<String> distinctSubMchIds = subOrders.stream()
                .map(WechatPayContext.SubOrderCtx::getSubMchId)
                .collect(Collectors.toSet());
        if (distinctSubMchIds.size() >= 2) {
            if (subOrders.size() > MAX_COMBINE_SUB_ORDERS) {
                log.error("[微信支付形态] 子单数 {} 超过微信合单上限 {}，降级直连支付, orderId={}",
                        subOrders.size(), MAX_COMBINE_SUB_ORDERS, detail.getOrderId());
                return WechatPayContext.direct();
            }
            return WechatPayContext.combine(detail.getPaySn(), subOrders);
        }

        String subMchId = distinctSubMchIds.iterator().next();
        long distinctShopCount = subOrders.stream()
                .map(WechatPayContext.SubOrderCtx::getShopId)
                .distinct()
                .count();
        if (distinctShopCount > 1) {
            log.warn("[微信支付形态] 订单[{}]的 {} 个子单跨 {} 家店铺却共用同一二级商户号[{}]，"
                            + "按服务商单商户支付处理，请核对子商户进件配置",
                    detail.getOrderId(), subOrders.size(), distinctShopCount, subMchId);
        }
        // 单店多供应商拆单落在此分支：整笔资金只进一个二级商户，但子单仍需各自建 paylog 行承载交易号。
        // 只要有一个子单存在分账对象就得下发分账标识，否则该子单的服务费无从收回。
        WechatPayContext context = WechatPayContext.partner(subMchId, subOrders.get(0).getSubAppId(),
                subOrders.stream().anyMatch(WechatPayContext.SubOrderCtx::isProfitSharing));
        context.setSubOrders(subOrders);
        return context;
    }

    /**
     * 退款用的候选二级商户号：按订单所属店铺现查当前绑定。
     * <p>只是「候选」而非结论：店铺可能在支付后换绑，故调用方须再向微信查单确认真实归属
     * （见 DESIGN D5）。未启用线上分账、无店铺归属或未激活子商户时返回 null，调用方按直连退款处理。
     *
     * @param orderId 退款所针对的订单（拆单场景为子单）
     * @return 候选二级商户号，无则 null
     */
    public String resolveRefundSubMchId(Integer orderId) {
        if (orderId == null || !isOnlineProfitSharing()) {
            return null;
        }
        try {
            Order order = orderMapper.selectById(orderId);
            Integer shopId = order == null ? null : order.getShopId();
            if (shopId == null || shopId < 0) {
                return null;
            }
            SubMerchant subMerchant = subMerchantService.getActiveByShop(shopId.longValue(), PAY_CHANNEL_WECHAT);
            return subMerchant == null ? null : StrUtil.trimToNull(subMerchant.getSubMchId());
        } catch (Exception e) {
            log.warn("[微信退款] 订单[{}]候选二级商户号解析失败，按直连退款处理", orderId, e);
            return null;
        }
    }

    /**
     * 回调一致性告警：回调回传的二级商户号与订单所属店铺当前绑定不一致时只告警，不阻断回调。
     * <p>资金实际去向以微信回传为准，本地绑定可能在支付后被换绑；此处留痕供退款与分账排查。
     *
     * @param orderId         回调对应的订单（合单场景为子单）
     * @param notifySubMchId  回调回传的二级商户号
     */
    public void warnIfSubMchIdChanged(Integer orderId, String notifySubMchId) {
        if (orderId == null || StrUtil.isBlank(notifySubMchId)) {
            return;
        }
        try {
            Order order = orderMapper.selectById(orderId);
            Integer shopId = order == null ? null : order.getShopId();
            if (shopId == null || shopId < 0) {
                return;
            }
            SubMerchant subMerchant = subMerchantService.getActiveByShop(shopId.longValue(), PAY_CHANNEL_WECHAT);
            String localSubMchId = subMerchant == null ? null : subMerchant.getSubMchId();
            if (!Objects.equals(localSubMchId, notifySubMchId)) {
                log.warn("[微信支付回调] 订单[{}]店铺[{}]回调二级商户号[{}]与本地绑定[{}]不一致，以微信回传为准",
                        orderId, shopId, notifySubMchId, localSubMchId);
            }
        } catch (Exception e) {
            log.warn("[微信支付回调] 订单[{}]二级商户号一致性校验失败，不阻断回调", orderId, e);
        }
    }

    /**
     * 是否存在分账对象。平台自营（shopId=0）可进件为二级商户参与收款，但没有可分账的商家主体，
     * 口径与 {@code ProfitSharingOrderServiceImpl} 跳过 shopId&lt;=0 的建单逻辑保持一致。
     */
    private static boolean hasProfitSharingTarget(Integer shopId) {
        return shopId != null && shopId > 0;
    }

    /**
     * 子单待付金额合计必须严格等于父单待付金额，微信对合单金额零容忍
     */
    private boolean isAmountConserved(List<WechatPayContext.SubOrderCtx> subOrders, BigDecimal parentUnpaid, OrderVO detail) {
        BigDecimal sum = subOrders.stream()
                .map(WechatPayContext.SubOrderCtx::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.compareTo(parentUnpaid) != 0) {
            log.error("[微信支付形态] 子单金额合计不等于父单待付金额，降级直连支付, orderId={}, 子单合计={}, 父单待付={}",
                    detail.getOrderId(), sum, parentUnpaid);
            return false;
        }
        return true;
    }

    /**
     * 有效子单：parentOrderId 命中 且 未取消 且 未删除
     */
    private List<Order> listValidChildren(Integer parentOrderId) {
        return orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getParentOrderId, parentOrderId)
                .ne(Order::getOrderStatus, OrderStatusEnum.CANCELLED.getCode())
                .eq(Order::getIsDel, 0));
    }
}
