// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.pay.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.enums.msg.MessageTypeEnum;
import com.tigshop.bean.enums.order.OrderStatusEnum;
import com.tigshop.bean.enums.order.PaymentStatus;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.enums.user.ChangTypeEnum;
import com.tigshop.bean.model.finance.*;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.order.OrderPresale;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.ServletUtils;
import com.tigshop.mapper.finance.RefundApplyMapper;
import com.tigshop.mapper.finance.UserRechargeOrderMapper;
import com.tigshop.service.finance.*;
import com.tigshop.service.msgTemplate.MessageService;
import com.tigshop.service.order.OrderPresaleService;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.pay.PayBaseService;
import com.tigshop.service.product.ECardService;
import com.tigshop.service.product.ProductPresaleService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.shop.AccountService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.sl.draw.geom.GuideIf;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.spi.ObjectFactory;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

/**
 * 订单支付基础实现类
 *
 * @author Tigshop团队
 * @create 2025年02月18日 17:26
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class PayBaseServiceImpl implements PayBaseService {

    /**
     * 构造 H5 订单收银台地址，供外部支付失败或取消后重新选择支付方式。
     */
    protected String getH5CashierUrl(String h5Domain, Integer orderId, String paymentType) {
        return h5Domain + "/pages/order/cashier?orderId=" + orderId + "&paymentType=" + paymentType;
    }

    @Resource
    private ConfigService configService;
    @Resource
    private PaylogService paylogService;
    @Resource
    private OrderService orderService;
    @Resource
    private ECardService eCardService;
    @Resource
    private UserRechargeOrderMapper userRechargeOrderMapper;
    @Resource
    private UserBalanceLogService userBalanceLogService;
    @Resource
    protected PaylogRefundService payLogRefundService;
    @Resource
    private RefundApplyMapper refundApplyMapper;
    @Resource
    private MessageService messageService;
    @Resource
    private OrderPresaleService orderPresaleService;
    @Resource
    private ProductPresaleService productPresaleService;
    @Resource
    private ObjectProvider<RefundApplyService> refundApplyService;
    @Resource
    private AccountService accountService;



    /**
     * 获取回调地址
     *
     * @param payType 支付渠道
     * @param path    回调地址的路径
     * @return String
     */
    private String getNotifyUrl(String payType, String path) {
        String url = configService.getConfigByCode(SettingsEnum.PC_DOMAIN.getBizCode()).getBizVal();
        // 获取pc_domain并生成基础URL
        if (url.isEmpty()) {
            url = ServletUtils.getRequestSchemeAndHost() + path;
        } else {
            url = url + path;
        }

        // 如果支付渠道不为空，附加pay_code参数
        if (payType != null) {
            url = StrUtil.format("{}/{}", url, payType);
            System.out.println(url);
        }

        return url;
    }

    /**
     * 获取退款回调地址
     *
     * @param payType 支付渠道
     * @return String
     */
    @Override
    public String getRefundNotifyUrl(String payType) {
        return getNotifyUrl(payType, "/api/order/pay/refundNotify");
    }

    @Override
    public String getPayNotifyUrl(String payType) {
        return getNotifyUrl(payType, "/api/order/pay/notify");
    }

    @Override
    public String getReturnUrl(Integer orderId) {
        String url = configService.getConfigByCode(SettingsEnum.PC_DOMAIN.getBizCode()).getBizVal();
        // 获取pc_domain并生成基础URL
        if (url.isEmpty()) {
            url = ServletUtils.getRequestSchemeAndHost();
        }

        // 如果支付渠道不为空，附加pay_code参数
        if (orderId != null && orderId > 0) {
            if(orderService.lambdaQuery().eq(Order::getOrderId, orderId).count() > 0) {
                //订单
                String clientType = HeaderUtils.getClientType();
                if (Objects.equals(clientType, "pc")) {
                    url += "/member/order/info?id=" + orderId;
                }
                if (Objects.equals(clientType, "h5")) {
                    url = configService.getConfigByCode(SettingsEnum.H5_DOMAIN.getBizCode()).getBizVal() + "/pages/user/order/info?id=" + orderId;
                }
            }else if(userRechargeOrderMapper.selectById(orderId) != null){
                //充值
                String clientType = HeaderUtils.getClientType();
                if (Objects.equals(clientType, "pc")) {
                    url += "/member/account/detail";
                }
                if (Objects.equals(clientType, "h5")) {
                    url = configService.getConfigByCode(SettingsEnum.H5_DOMAIN.getBizCode()).getBizVal() + "/pages/user/account/index";
                }
            }
        }

        return url;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkOrderPayStatus(String paySn) {
        Paylog paylog = paylogService.getOne(new LambdaQueryWrapper<Paylog>().eq(Paylog::getPaySn, paySn));
        if (Objects.isNull(paylog)) {
            // 未找到支付记录，视为已处理，避免无效/重复通知持续重试
            return true;
        }
        if (!isKnownPaylogOrderType(paylog.getOrderType())) {
            throw new GlobalException("不支持的支付订单类型");
        }
        // 先按支付日志状态兜底，已支付直接拦截重复处理
        if (Objects.equals(paylog.getPayStatus(), 1)) {
            recordOnlinePayment(paylog);
            return true;
        }

        // 充值订单（orderType=1）
        if (Objects.equals(paylog.getOrderType(), 1)) {
            UserRechargeOrder rechargeOrder = userRechargeOrderMapper.selectById(paylog.getOrderId());
            if (Objects.isNull(rechargeOrder)) {
                return true;
            }
            return !Objects.equals(rechargeOrder.getStatus(), 0);
        }

        // 商城订单（orderType=0）
        if (Objects.equals(paylog.getOrderType(), 0)) {
            Order order = orderService.getById(paylog.getOrderId());
            if (order == null) {
                return true;
            }
            return !Objects.equals(order.getOrderStatus(), OrderStatusEnum.PENDING.getCode());
        }

        throw new GlobalException("不支持的支付订单类型");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void paySuccess(String paySn, String tranceNo) {
        Paylog one = paylogService.getOne(new LambdaQueryWrapper<Paylog>().eq(Paylog::getPaySn, paySn));
        if (one == null) {
            throw new GlobalException("支付记录不存在");
        }
        if (!isKnownPaylogOrderType(one.getOrderType())) {
            throw new GlobalException("不支持的支付订单类型");
        }

        one.setPayStatus(1);
        one.setTransactionId(tranceNo);
        paylogService.updateById(one);

recordOnlinePayment(one);


        if (Objects.equals(one.getOrderType(), 1)) {
            // 充值
            setRechargePaid(one.getOrderId());
            return;
        }

        if (Objects.equals(one.getOrderType(), 0)) {
            // 下单
            Order order = orderService.getById(one.getOrderId());
            if (order != null && Objects.equals(order.getOrderStatus(), OrderStatusEnum.PENDING.getCode())) {
                // 未支付 处理逻辑
                if (one.getOrderType() == 0) {
                    // 订单
                    order.setOutTradeNo(one.getPaySn());
                    orderService.updateById(order);

                    //预售订单会支付两次
                    if (Objects.equals(1, order.getIsPresale()) && Objects.equals(2, order.getPresaleType())) {
                        //  第一次可能会选择余额(有可能全部抵扣了),这样的话就没有在线金额支付了
                        OrderPresale orderPresale = orderPresaleService.getByOrderId(order.getOrderId());
                        BigDecimal depositAmount = orderPresale.getDepositAmount();
                        if (Integer.valueOf(0).equals(orderPresale.getPresaleStatus())) {
                            //待支付定金
                            if (order.getBalance().compareTo(depositAmount) != 0) {
                                //余额没有全部抵扣定金
                                order.setOnlinePaidAmount(Optional.ofNullable(order.getOnlinePaidAmount()).orElse(BigDecimal.ZERO).add(one.getPayAmount()));
                            }//全部抵扣的话,就不需要online金额支付了
                        } else {
                            //待支付尾款,定金已支付
                            order.setOnlinePaidAmount(Optional.ofNullable(order.getOnlinePaidAmount()).orElse(BigDecimal.ZERO).add(one.getPayAmount()));
                        }
                    } else {
                        //普通的模式
                        order.setOnlinePaidAmount(one.getPayAmount());
                    }
                    orderService.updateOrderMoney(order, true);

                    // 【预售功能】处理预售订单支付成功
                    handlePresaleOrderPaySuccess(order.getOrderId());


                    // 卡券分配
                    eCardService.getCardByOrderId(order.getOrderId());
                }
            }
            return;
        }

        throw new GlobalException("不支持的支付订单类型");
    }

    /**
     * Paylog 是商城订单、充值订单和付费会员订单共用的表；未知类型必须拒绝，
     * 不能落入商城订单的兼容默认分支。
     */
    private boolean isKnownPaylogOrderType(Integer orderType) {
        if (Objects.equals(orderType, 0) || Objects.equals(orderType, 1)) {
            return true;
        }
        return false;
    }

    /**
     * 处理预售订单支付成功
     * 根据预售订单的状态判断是定金支付还是尾款支付，并更新相应的状态
     */
    private void handlePresaleOrderPaySuccess(Integer orderId) {
        Order order = orderService.getById(orderId);
        if (Objects.isNull(order)) {
            return;
        }

        try {
            // 检查是否是预售订单
            if (order.getIsPresale() == null || order.getIsPresale() != 1) {
                return;
            }

            // 检查是否是定金预售
            if (order.getPresaleType() == null || order.getPresaleType() != 2) {
                // 全款预售，不需要特殊处理
                return;
            }

            log.info("开始处理预售订单支付成功，订单ID: {}", order.getOrderId());

            // 查询预售订单信息
            com.tigshop.bean.model.order.OrderPresale orderPresale = orderPresaleService.getByOrderId(order.getOrderId());
            if (orderPresale == null) {
                log.error("预售订单信息不存在，订单ID: {}", order.getOrderId());
                return;
            }

            // 根据预售状态判断是定金支付还是尾款支付
            if (orderPresale.getPresaleStatus() == 0) {
                // 状态为0，表示是定金支付
                log.info("处理定金支付成功，订单ID: {}, 预售商品ID: {}", order.getOrderId(), orderPresale.getProductId());

                // 查询商品预售配置，获取尾款相对天数
                com.tigshop.bean.model.product.ProductPresale productPresale = productPresaleService.getByProductId(orderPresale.getProductId());

                if (productPresale == null) {
                    log.error("商品预售配置不存在，商品ID: {}", orderPresale.getProductId());
                    return;
                }

                Integer finalPaymentRelativeDays = productPresale.getFinalPaymentRelativeDays();
                if (finalPaymentRelativeDays == null) {
                    finalPaymentRelativeDays = 0; // 默认立即可以支付尾款
                }

                // 更新定金支付状态
                boolean success = orderPresaleService.updateDepositPaid(order.getOrderId(), finalPaymentRelativeDays);
                if (success) {
                    log.info("定金支付状态更新成功，订单ID: {}", order.getOrderId());
                } else {
                    log.error("定金支付状态更新失败，订单ID: {}", order.getOrderId());
                }

            } else if (orderPresale.getPresaleStatus() == 1 && Objects.equals(PaymentStatus.PAID.getCode(), order.getPayStatus())) {
                // 状态为1，表示是尾款支付
                log.info("处理尾款支付成功，订单ID: {}", order.getOrderId());

                // 更新尾款支付状态
                boolean success = orderPresaleService.updateFinalPaymentPaid(order.getOrderId());
                if (success) {
                    log.info("尾款支付状态更新成功，订单ID: {}", order.getOrderId());
                } else {
                    log.error("尾款支付状态更新失败，订单ID: {}", order.getOrderId());
                }

                // 尾款支付成功后，更新主订单状态为已确认（待发货）
                Order updateOrder = new Order();
                updateOrder.setOrderId(order.getOrderId());
                updateOrder.setOrderStatus(OrderStatusEnum.CONFIRMED.getCode());
                updateOrder.setPayStatus(PaymentStatus.PAID.getCode());
                orderService.updateById(updateOrder);

                // 调用订单拆分逻辑，创建配送订单（同城配送订单）
                orderService.splitStoreOrder(order.getOrderId());

                log.info("尾款支付成功后订单状态已更新为CONFIRMED，订单ID: {}", order.getOrderId());
            } else {
                log.warn("预售订单状态不正确，无需处理，订单ID: {}, 状态: {}", order.getOrderId(), orderPresale.getPresaleStatus());
            }

        } catch (Exception e) {
            log.error("处理预售订单支付成功失败，订单ID: " + order.getOrderId(), e);
            // 不抛出异常，避免影响正常订单支付流程
        }
    }



    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refundSuccess(String refundSn) {
        // 获取 PayLogRefund 对象
        PaylogRefund payLogRefund = payLogRefundService.getOne(new LambdaQueryWrapper<PaylogRefund>().eq(PaylogRefund::getRefundSn, refundSn));
        if (payLogRefund == null) {
            return;
        }
        if (payLogRefund.getOrderId() == null) {
            return;
        }
        // 与退款申请、分账发起使用同一订单行锁；加入已有事务时可重入，异步通知则等待前一操作提交。
        Order lockedOrder = orderService.lambdaQuery()
                .eq(Order::getOrderId, payLogRefund.getOrderId()).last("FOR UPDATE").one();
        if (lockedOrder == null) {
            throw new GlobalException("退款订单不存在，无法确认退款成功");
        }
        // 锁后使用当前读，不能继续使用等待行锁前的旧退款状态。
        payLogRefund = payLogRefundService.getOne(new LambdaQueryWrapper<PaylogRefund>()
                .eq(PaylogRefund::getPaylogRefundId, payLogRefund.getPaylogRefundId())
                .last("FOR UPDATE"));
        if (payLogRefund == null) {
            throw new GlobalException("退款流水不存在，无法确认退款成功");
        }
        if (Objects.equals(payLogRefund.getStatus(), 1)) {
            recordOnlineRefund(payLogRefund, lockedOrder);
            return;
        }
        try {
            // 修改通知状态
            PaylogRefund update = new PaylogRefund();
            update.setPaylogRefundId(payLogRefund.getPaylogRefundId());
            update.setPaylogId(payLogRefund.getPaylogId());
            update.setStatus(1);
            update.setNotifyTime(System.currentTimeMillis() / 1000);
            boolean recorded = payLogRefundService.update(update, new LambdaQueryWrapper<PaylogRefund>()
                    .eq(PaylogRefund::getPaylogRefundId, payLogRefund.getPaylogRefundId())
                    .ne(PaylogRefund::getStatus, 1));
            if (!recorded) {
                throw new GlobalException("退款成功状态保存失败，请重试通知");
            }
            // 通知退款成功
            int successFlag = onlineRefundSuccess(payLogRefund.getPaylogRefundId());
            if (successFlag > 0) {
                //里面进行更新退款金额(退款金额是通过在原来的基础上处理的)
                boolean amountUpdated = paylogService.lambdaUpdate()
                        .eq(Paylog::getPaylogId, payLogRefund.getPaylogId())
                        .setSql("refund_amount = IFNULL(refund_amount, 0) + " + payLogRefund.getRefundAmount())
                        .update();
                if (!amountUpdated) {
                    throw new GlobalException("支付流水退款金额更新失败，请重试通知");
                }
                recordOnlineRefund(payLogRefund, lockedOrder);
            } else {
                throw new GlobalException("退款申请成功状态保存失败，请重试通知");
            }
        } catch (Exception e) {
            throw new GlobalException(e.getMessage());
        }
    }

    /** 渠道收款包含待分账资金，不代表当前可提现余额；解冻时不重复记收入。 */
    private void recordOnlinePayment(Paylog paylog) {
        if (!Integer.valueOf(0).equals(paylog.getOrderType())
                || StrUtil.isBlank(paylog.getProfitSharingChannel())
                || paylog.getPayAmount() == null || paylog.getPayAmount().signum() <= 0) {
            return;
        }
        Order order = orderService.getById(paylog.getOrderId());
        if (order == null || order.getShopId() == null || order.getShopId() <= 0) {
            return;
        }
        accountService.recordOnlineChange(order.getShopId(), paylog.getProfitSharingChannel(),
                "PAY:" + paylog.getPaylogId(), paylog.getPayAmount(),
                "线上收款（待分账）：" + paylog.getPaySn(), System.currentTimeMillis() / 1000);
    }

    /** 仅凭原支付渠道快照识别线上资金，不按退款时的开关或绑定重新归类。 */
    private void recordOnlineRefund(PaylogRefund refund, Order order) {
        if (order.getShopId() == null || order.getShopId() <= 0) {
            return;
        }
        Paylog paylog = paylogService.getById(refund.getPaylogId());
        if (paylog == null || StrUtil.isBlank(paylog.getProfitSharingChannel())
                || refund.getRefundAmount() == null || refund.getRefundAmount().signum() <= 0) {
            return;
        }
        accountService.recordOnlineChange(order.getShopId(), paylog.getProfitSharingChannel(),
                "REFUND:" + refund.getPaylogRefundId(), refund.getRefundAmount().negate(),
                "线上退款：" + refund.getRefundSn(),
                refund.getNotifyTime() == null ? System.currentTimeMillis() / 1000 : refund.getNotifyTime());
    }

    /**
     * 设置充值支付成功
     *
     * @param orderId 订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    protected void setRechargePaid(Integer orderId) {
        // 获取订单详情
        UserRechargeOrder order = userRechargeOrderMapper.selectById(orderId);
        if (order == null) {
            throw new GlobalException("充值订单不存在");
        }

        if (order.getStatus() != 0) {
            // 重复回调直接跳过，保持幂等
            return;
        }

        // 更新订单状态为已支付
        UserRechargeOrder updateOrder = new UserRechargeOrder();
        updateOrder.setOrderId(orderId);
        updateOrder.setStatus(1);
        updateOrder.setPaidTime(System.currentTimeMillis() / 1000);
        userRechargeOrderMapper.updateById(updateOrder);

        // 修改用户余额
        UserBalanceLog log = new UserBalanceLog();
        log.setBalance(order.getAmount().add(order.getDiscountMoney()));
        log.setChangeDesc(order.getPostscript());
        log.setChangeType(ChangTypeEnum.INC_POINTS.getValue());

        userBalanceLogService.accountChange(order.getUserId(), log);
    }

    @Transactional
    protected int onlineRefundSuccess(Integer paylogRefundId) {
        //由于退款申请表和支付表是多对多的关系
        RefundApply refundApply = refundApplyService.getIfAvailable().getByPaylogRefundId(paylogRefundId);
//        RefundApply refundApply = refundApplyMapper.selectOne(new LambdaQueryWrapper<RefundApply>().eq(RefundApply::getPaylogRefundId, paylogRefundId));
        if (refundApply == null) {
            return 0;
        }
        refundApply.setIsOnline(2);
        if (refundApply.getIsOffline() != 1 && refundApply.getIsReceive() != 1) {
            refundApply.setRefundStatus(2);
        }
        try {
            messageService.sendUserMessage(refundApply.getUserId(), refundApply.getOrderId(), MessageTypeEnum.ORDER_REFUND, refundApply.getOnlineBalance());
        } catch (Exception ex) {
            log.error("退款通知 ORDER_REFUND 发送失败，不影响退款主流程，refundApplyId={}: {}", refundApply.getRefundId(), ex.getMessage(), ex);
        }
        return refundApplyMapper.updateById(refundApply);
    }
}
