package com.tigshop.api.controller.order;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.model.finance.Paylog;
import com.tigshop.bean.model.finance.UserRechargeOrder;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.param.order.pay.ApplePayConfirmByPmParam;
import com.tigshop.bean.param.order.pay.ApplePayConfirmParam;
import com.tigshop.bean.param.order.pay.ApplePayValidateMerchantParam;
import com.tigshop.bean.vo.order.pay.ApplePayConfirmVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.service.finance.PaylogService;
import com.tigshop.service.finance.UserRechargeOrderService;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.pay.ApplePayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Apple Pay 支付控制器（VORT 门控 common.pay.applePay 专属，整文件可删）。
 *
 * <p>从共享 {@code PayController} 拆出原生流程端点（validateMerchant / confirm /
 * confirmByPaymentMethod / UMS 异步通知），使 PayController 不再引用
 * {@link ApplePayService} 及 Apple Pay 参数/VO 类型；关闭 Apple Pay 时整文件删除，
 * 无需在 PayController 内联 {@code @vort-if}。路由 path 与原实现保持一致。</p>
 *
 * @author Tigshop团队
 * @since 2026/07/16
 */
@RestController
@RequestMapping("/api/order/pay")
@Validated
@Tag(name = "订单支付-Apple Pay", description = "Apple Pay 支付")
public class ApplePayController {

    @Resource
    private ApplePayService applePayService;

    @Resource
    private OrderService orderService;

    @Resource
    private PaylogService paylogService;

    @Resource
    private UserRechargeOrderService userRechargeOrderService;

    @PostMapping("/applepay/validateMerchant")
    @Operation(summary = "Apple Pay 商户校验")
    public String applePayValidateMerchant(@RequestBody @Validated ApplePayValidateMerchantParam param) {
        verifyPaySnOwnership(param.getPaySn(), requireUserId());
        return applePayService.validateMerchant(param.getPaySn(), param.getValidationUrl(), param.getDisplayDomain());
    }

    @PostMapping("/applepay/confirm")
    @Operation(summary = "Apple Pay 支付确认（自建原生流程：原始 token）")
    public ApplePayConfirmVO applePayConfirm(@RequestBody @Validated ApplePayConfirmParam param) {
        verifyPaySnOwnership(param.getPaySn(), requireUserId());
        String paymentTokenJson = param.getPaymentToken() == null ? null : JSON.toJSONString(param.getPaymentToken());
        return applePayService.confirm(param.getPaySn(), paymentTokenJson);
    }

    @PostMapping("/applepay/confirmByPaymentMethod")
    @Operation(summary = "Apple Pay 支付确认（PSP 前端驱动：paymentMethodId，如 Stripe）")
    public ApplePayConfirmVO applePayConfirmByPaymentMethod(@RequestBody @Validated ApplePayConfirmByPmParam param) {
        verifyPaySnOwnership(param.getPaySn(), requireUserId());
        return applePayService.confirmByPaymentMethod(param.getPaySn(), param.getPaymentMethodId());
    }

    @PostMapping(value = "/notify/ums", consumes = "application/json")
    @Operation(summary = "银联商务(UMS) Apple Pay 支付结果异步通知")
    public String umsApplePayNotify(@RequestBody Map<String, Object> resMap) {
        // 服务端到服务端回调（白名单放行）：验签 + 支付成功则置为已支付
        return applePayService.payNotify(resMap);
    }

    private Integer requireUserId() {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null || userId == 0) {
            throw new GlobalException("用户未登录");
        }
        return userId;
    }

    private void verifyOrderOwnership(Integer orderId, Integer userId) {
        if (orderId == null) {
            return;
        }
        Order order = orderService.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new GlobalException("订单不存在");
        }
    }

    /**
     * 校验支付流水归属当前用户（防止越权操作他人支付单）。
     */
    private void verifyPaySnOwnership(String paySn, Integer userId) {
        Paylog paylog = paylogService.getOne(new LambdaQueryWrapper<Paylog>().eq(Paylog::getPaySn, paySn));
        if (paylog == null) {
            throw new GlobalException("找不到原支付记录");
        }
        Integer orderId = paylog.getOrderId();
        if (orderId == null) {
            throw new GlobalException("订单不存在");
        }
        // 按 orderType 分别校验归属，避免充值单/商品单 ID 数值碰撞导致误匹配（#5）
        if (Integer.valueOf(1).equals(paylog.getOrderType())) {
            UserRechargeOrder rechargeOrder = userRechargeOrderService.getById(orderId);
            if (rechargeOrder == null || !userId.equals(rechargeOrder.getUserId())) {
                throw new GlobalException("订单不存在");
            }
        } else {
            verifyOrderOwnership(orderId, userId);
        }
    }
}
