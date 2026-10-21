package com.tigshop.service.order.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.bo.PayPlatformStateBO;
import com.tigshop.bean.bo.pay.ProfitSharingPaymentContext;
import com.tigshop.bean.bo.pay.WechatPayContext;
import com.tigshop.bean.enums.order.OrderStatusEnum;
import com.tigshop.bean.enums.order.PaymentStatus;
import com.tigshop.bean.enums.order.PaymentType;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.enums.user.AuthorizeTypeEnum;
import com.tigshop.bean.feign.wechat.Code2SessionResult;
import com.tigshop.bean.model.finance.Paylog;
import com.tigshop.bean.model.finance.UserCashLog;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.model.user.UserAuthorize;
import com.tigshop.bean.vo.order.OrderItemVO;
import com.tigshop.bean.vo.order.OrderPresaleVO;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.bean.vo.order.pay.PayCreateVO;
import com.tigshop.bean.vo.order.pay.PayIndexVO;
import com.tigshop.bean.vo.product.ProductAvailabilityVO;
import com.tigshop.common.constant.finance.PayConstants;
import com.tigshop.common.enums.DataType;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.feign.WechatApiClient;
import com.tigshop.service.finance.PaylogService;
import com.tigshop.service.finance.UserCashLogService;
import com.tigshop.service.order.OrderPayService;
import com.tigshop.service.order.OrderPresaleService;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.order.OrderOnboardingGuard;
import com.tigshop.service.pay.NewPayChannelRegistry;
import com.tigshop.service.pay.PayService;
import com.tigshop.service.pay.PaymentService;
import com.tigshop.service.pay.ProfitSharingAwarePayService;
import com.tigshop.service.pay.ProfitSharingPaymentSnapshotResolver;
import com.tigshop.service.pay.ProfitSharingPaymentSnapshotResolver.PaymentScene;
import com.tigshop.service.pay.WechatModePayService;
import com.tigshop.service.pay.WechatPayModeResolver;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.user.UserAuthorizeService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 订单支付
 *
 * @author Tigshop团队
 * @create 2024-09-30 15:16:06
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class OrderPayServiceImpl implements OrderPayService {

    @Resource
    private OrderOnboardingGuard orderOnboardingGuard;

    private void assertOrderShopsCanPurchase(OrderVO detail) {
        if (detail == null) return;
        if (detail.getShopId() != null) orderOnboardingGuard.assertShopCanPurchase(detail.getShopId());
        if (detail.getItems() != null) {
            detail.getItems().forEach(item -> orderOnboardingGuard.assertShopCanPurchase(item.getShopId()));
        }
    }

    private static final Set<String> WECHAT_PROFIT_SHARING_CLIENT_TYPES =
            Set.of("wechat", "miniProgram", "app", "pc", "h5");

    @Resource
    private OrderService orderService;

    @Resource
    private PaymentService paymentService;

    @Resource
    private ProductService productService;

    @Resource
    private ConfigService configService;

    @Resource
    private PaylogService paylogService;

    @Resource
    TranslatePackageImpl translatePackage;

    @Resource
    private MessageHelper messageHelper;

    @Resource
    private TigshopProperties tigshopProperties;

    @Resource
    UserCashLogService userCashLogService;

    @Resource
    @Qualifier("WechatPayService")
    private PayService wechatPayService;

    @Resource
    private WechatModePayService wechatModePayService;

    @Resource
    private WechatPayModeResolver wechatPayModeResolver;

    @Resource
    @Qualifier("AliPayService")
    private PayService aliPayService;

    @Resource
    @Qualifier("PaypalService")
    private PayService paypalService;


    // 新增在线支付渠道（汇付/ApplePay/拉卡拉）统一走局部工厂分发；既有渠道保持各自 if-else
    @Resource
    private NewPayChannelRegistry newPayChannelRegistry;

    @Resource
    private ProfitSharingPaymentSnapshotResolver profitSharingPaymentSnapshotResolver;

    private final WechatApiClient wechatApiClient;
    @Autowired
    private UserAuthorizeService userAuthorizeService;

    @Autowired
    ApplicationContext applicationContext;

    @Resource
    private OrderPresaleService orderPresaleService;




    @Override
    public PayIndexVO getPaymentSelection() {
        PayIndexVO vo = new PayIndexVO();
        // 传非 "order" 的 type：getAvailablePayment 内部仅在 type="order" 时追加 offline，
        // 故下单前收银台的支付方式选择不含 offline（无订单上下文，offline 展示值无处取）。
        vo.setPaymentList(paymentService.getAvailablePayment("checkout"));
        vo.setOfflinePaymentList(new HashMap<>());
        return vo;
    }

    @Override
    public PayIndexVO getPayOrderInfo(Integer id) {
        PayIndexVO vo = new PayIndexVO();
        // 无订单时用于下单前收银台：不要加载依赖订单号的线下支付说明。
        // 否则空订单对象的 orderSn 会作为 String.replace 的 replacement 触发 NPE。
        List<String> paymentList = paymentService.getAvailablePayment(id == null ? "checkout" : "order");
        OrderVO detail = new OrderVO();
        if (id != null) {
            detail = orderService.detail(id);
            vo.setOrder(detail);

            detail.setMemberDiscountAmount(calculateMemberDiscountAmount(detail.getItems()));

            // 预计赠送积分（与支付成功后实际赠送同公式、同基数 totalAmount）
            detail.setWillSendPoints(orderService.previewSendPoints(detail.getTotalAmount(), detail.getUserId()));

            //预售金额处理
            Integer isPresale = detail.getIsPresale();
            if (Objects.equals(1, isPresale)) {
                OrderPresaleVO presaleInfo = detail.getPresaleInfo();
                if (Objects.isNull(presaleInfo)) {
                    throw new GlobalException(messageHelper.get("error.order.info.invalid"));
                }
                Integer saleMode = presaleInfo.getSaleMode();
                if (Integer.valueOf(2).equals(saleMode)) {
                    //定金模式
                    Integer presaleStatus = presaleInfo.getPresaleStatus();
                    if (Objects.equals(0, presaleStatus)) {
                        //待支付定金
                        BigDecimal depositAmount = presaleInfo.getDepositAmount();
                        BigDecimal balance = detail.getBalance();
                        detail.setUnpaidAmount(depositAmount.subtract(Optional.ofNullable(balance).orElse(BigDecimal.ZERO)));
                    }
                }
                //其他状态,后续会处理
            }

            if (!detail.getAvailableActions().getToPay()) {
                throw new GlobalException(messageHelper.get("error.order.already.paid"));
            }

            if (detail.getPayTypeId() == 1) {
                paymentList.remove("offline");
            } else if (detail.getPayTypeId() == 3) {
                paymentList = paymentList.stream().filter("offline"::equals).toList();
            }
        }





        vo.setPaymentList(paymentList);
        Map<String, String> offlinePaymentList = new HashMap<>();

        boolean offline = paymentList.contains("offline");
        if(!offline){
            String useOfflineVal = configService.getConfigVal(SettingsEnum.USE_OFFLINE);
            if (StrUtil.isNotBlank(useOfflineVal) && Integer.parseInt(useOfflineVal) == 1) {
                offline = true;
            }
        }
        if (offline) {
            String offlinePayBankEnable = configService.getConfigVal(SettingsEnum.OFFLINE_PAY_BANK_ENABLE);
            if ("1".equals(offlinePayBankEnable)){
                ConfigPO offlinePayBankConfig = configService.getConfigByCode(SettingsEnum.OFFLINE_PAY_BANK.getBizCode());
                offlinePaymentList.put("offlinePayBank", resolveOfflinePaymentDisplayVal(offlinePayBankConfig, detail));
            }

            String offlinePayCompanyEnable = configService.getConfigVal(SettingsEnum.OFFLINE_PAY_COMPANY_ENABLE);
            if ("1".equals(offlinePayCompanyEnable)){
                ConfigPO offlinePayCompanyConfig = configService.getConfigByCode(SettingsEnum.OFFLINE_PAY_COMPANY.getBizCode());
                offlinePaymentList.put("offlinePayCompany", resolveOfflinePaymentDisplayVal(offlinePayCompanyConfig, detail));
            }

            String thirdPartyPaymentTransferEnable = configService.getConfigVal(SettingsEnum.THIRD_PARTY_PAYMENT_TRANSFER_ENABLE);
            if ("1".equals(thirdPartyPaymentTransferEnable)){
                ConfigPO thirdPartyPaymentTransferConfig = configService.getConfigByCode(SettingsEnum.THIRD_PARTY_PAYMENT_TRANSFER.getBizCode());
                offlinePaymentList.put("thirdPartyPaymentTransfer", resolveOfflinePaymentDisplayVal(thirdPartyPaymentTransferConfig, detail));
            }

            String otherPaymentRemittanceEnable = configService.getConfigVal(SettingsEnum.OTHER_PAYMENT_REMITTANCE_ENABLE);
            if ("1".equals(otherPaymentRemittanceEnable)){
                ConfigPO otherPaymentRemittanceConfig = configService.getConfigByCode(SettingsEnum.OTHER_PAYMENT_REMITTANCE.getBizCode());
                offlinePaymentList.put("otherPaymentRemittance", resolveOfflinePaymentDisplayVal(otherPaymentRemittanceConfig, detail));
            }

        }
        vo.setOfflinePaymentList(offlinePaymentList);

        return vo;

    }

    /**
     * 结算页会员优惠只计算当前商品基础价与下单会员价的差额，不包含促销、优惠券和改价。
     */
    BigDecimal calculateMemberDiscountAmount(List<OrderItemVO> items) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal memberDiscountAmount = BigDecimal.ZERO;
        for (OrderItemVO item : items) {
            if (Objects.equals(item.getIsGift(), 1)
                    || item.getProductId() == null
                    || item.getOriginPrice() == null
                    || item.getQuantity() == null
                    || item.getQuantity() <= 0) {
                continue;
            }
            ProductAvailabilityVO product = productService.getProductFinalPrice(
                    item.getProductId(), item.getSkuId(), 1, null, item.getShopId(), 0);
            BigDecimal productPrice = product == null ? null : product.getPrice();
            if (productPrice == null) {
                continue;
            }
            BigDecimal discount = productPrice.subtract(item.getOriginPrice());
            if (discount.compareTo(BigDecimal.ZERO) > 0) {
                memberDiscountAmount = memberDiscountAmount.add(
                        discount.multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }
        return memberDiscountAmount;
    }

    /**
     * 线下支付说明：先按跨境配置翻译模板（保留 {$order_sn}），再替换订单号。
     */
    private String resolveOfflinePaymentDisplayVal(ConfigPO config, OrderVO detail) {
        String bizVal = config.getBizVal();
        return bizVal.replace("{$order_sn}", StrUtil.nullToEmpty(detail.getOrderSn()));
    }

    /**
     *
     * @param id       订单ID
     * @param payLogId 支付日志ID
     * @return 1 支付成功 0 待支付
     */
    @Override
    public Integer getCheckStatus(Integer id, Integer payLogId) {
        if (id == null && payLogId == null) {
            throw new GlobalException(messageHelper.get("error.param.invalid"));
        }
        Map<String, Integer> map = new HashMap<>();
        Integer payStatus;
        if (id != null) {
            payStatus = orderService.getById(id).getPayStatus();
            if (payStatus == 2) {
                //支付成功了,直接返回
                return 1;
            }
        } else {
            payStatus = paylogService.getById(payLogId).getPayStatus();
        }
        // 商品订单：orderType=0
        Paylog paylog = paylogService.getLastByOrderId(id, 0);
        if (paylog == null) {
            return payStatus;
        }
        String payCode = paylog.getPayCode();
        if (!"wechat".equals(payCode)) {
            return payStatus;
        }
        //微信特殊处理 (付款码支付的时候需要前端轮训处理支付结果)
        String posPayType = HeaderUtils.getPosPayType();
        if (PayConstants.CODE_PAY_CODE.equals(posPayType)) {
            //查询状态,如果状态是支付成功的话,调用支付的paySuccess
            PayPlatformStateBO payPlatformStateBO = wechatPayService.payPlatformStatus(paylog.getPaySn(), paylog.getTransactionId(), posPayType);
            if (Objects.isNull(payPlatformStateBO)) {
                return payStatus;
            }
            //可查看微信支付状态转移图 https://pay.weixin.qq.com/doc/v2/merchant/4011937125
            String tradeState = payPlatformStateBO.getTradeState();
            if (PayConstants.WX_PAY_SUCCESS_CODE.equals(tradeState)) {
                wechatPayService.paySuccess(payPlatformStateBO.getOutTradeNo(), payPlatformStateBO.getTradeNo());
                return payStatus;
            }
        }
        log.info("微信支付状态监测：id :{}, payLogId:{}, payStatus:{}", id, payLogId, payStatus);
        return payStatus > 0 ? 1 : 0;
    }


    @Override
    public PayCreateVO create(Integer id, String payType, String code) {
        PayCreateVO payCreateVO = new PayCreateVO();
        if (payType == null) {
            throw new GlobalException(messageHelper.get("error.pay.method.required"));
        }
        String clientType = HeaderUtils.getClientType();
        String adminType = HeaderUtils.getAdminType();
        if ("pos".equals(adminType)) {
            clientType = HeaderUtils.getPosPayType();
        }


        String openid = null;
        if (code != null) {
            // 获取微信配置
            String appId = configService.getConfigByCode(SettingsEnum.WECHAT_MINI_PROGRAM_APP_ID.getBizCode()).getBizVal();
            String appSecret = configService.getConfigByCode(SettingsEnum.WECHAT_MINI_PROGRAM_SECRET.getBizCode()).getBizVal();
            String result = wechatApiClient.code2Session(appId, appSecret, code, "authorization_code");
            Code2SessionResult code2SessionResult = JsonUtil.fromJson(result, Code2SessionResult.class);
            // 错误code -1,40029，45011,40226
            if (code2SessionResult.getErrcode() != null && code2SessionResult.getErrcode() != 0) {
                throw new GlobalException(code2SessionResult.getErrmsg());
            }

            openid = code2SessionResult.getOpenid();
        }
        if (ObjectUtil.equals("wechat", clientType) && "wechat".equals(payType)) {
            // 微信内支付 - 需要获取公众号的openid (authorizeType=1)
            UserAuthorize userAuthorize = userAuthorizeService.getOne(new LambdaQueryWrapper<UserAuthorize>()
                    .eq(UserAuthorize::getUserId, SecurityUtils.getCurrentUserId())
                    .eq(UserAuthorize::getAuthorizeType, AuthorizeTypeEnum.WECHAT.getCode()));
            if (userAuthorize != null) {
                openid = userAuthorize.getOpenId();
            }
        }
        OrderVO detail = orderService.detail(id);
        assertOrderShopsCanPurchase(detail);

        // 【预售功能】检查是否是尾款支付
        if (detail.getIsPresale() != null && detail.getIsPresale() == 1 && detail.getPresaleType() == 2) {
            // 定金预售订单，检查是否是尾款支付
             OrderPresaleVO orderPresaleVO = orderPresaleService.getPresaleVOByOrderId(id);
            if (orderPresaleVO != null && orderPresaleVO.getPresaleStatus() == 0) {
                throw new GlobalException(messageHelper.get("error.presale.deposit.required"));
            }
//            if (orderPresaleVO != null && orderPresaleVO.getPresaleStatus() == 1) {
//                // 状态为1表示已支付定金，待支付尾款
//                // 校验尾款开始支付时间
//                if (orderPresaleVO.getFinalPaymentStartTime() != null) {
//                    long currentTime = System.currentTimeMillis() / 1000;
//                    if (currentTime < orderPresaleVO.getFinalPaymentStartTime()) {
//                        throw new GlobalException(translatePackage.translate("尚未到尾款支付时间，请稍后再试"));
//                    }
//                }
//                log.info("订单[{}]尾款支付校验通过，开始创建尾款支付单", id);
//            }
        }

        if (!detail.getAvailableActions().getToPay()) {
            if (detail.getOrderStatus().equals(OrderStatusEnum.CANCELLED.getCode())) {
                throw new GlobalException(messageHelper.get("error.order.already.cancelled"));
            } else {
                throw new GlobalException(messageHelper.get("error.order.already.paid"));
            }
        }
        PayService newChannel = newPayChannelRegistry.get(payType);
        PayService snapshotClient = "alipay".equals(payType) ? aliPayService : newChannel;
        boolean profitSharingAware = isProfitSharingAware(payType, snapshotClient);
        ProfitSharingPaymentContext paymentContext = profitSharingAware
                ? profitSharingPaymentSnapshotResolver.resolve(payType, clientType, PaymentScene.ORDER)
                : ProfitSharingPaymentContext.none();
        if (paymentContext == null) {
            paymentContext = ProfitSharingPaymentContext.none();
        }
        Paylog paylog = profitSharingAware
                ? paylogService.creatPayLog(null, detail, payType, 0,
                        detail.getUnpaidAmount(), paymentContext.getChannel())
                : paylogService.creatPayLog(null, detail, payType, 0, detail.getUnpaidAmount());
        if (paylog == null || StrUtil.isBlank(paylog.getPaySn())) {
            throw new GlobalException("支付日志创建失败");
        }
        detail.setPaySn(paylog.getPaySn());
        PayCreateVO.PayInfo payResult = new PayCreateVO.PayInfo();
        if ("wechat".equals(payType)) {
            payResult = payWechat(detail, openid, clientType, adminType, payType);
        }
        if ("alipay".equals(payType)) {
            payResult = payWithProfitSharingContext(aliPayService, detail, openid, clientType, paymentContext);
        }
        if ("paypal".equals(payType)) {
            payResult = paypalService.pay(detail, openid, clientType);
        }
        // 新增渠道（汇付/ApplePay/拉卡拉）走局部工厂
        if (newChannel != null) {
            if (isHuifuPayType(payType)) {
                payResult = payWithProfitSharingContext(newChannel, detail, openid, clientType, paymentContext);
            } else {
                payResult = newChannel.pay(detail, openid, clientType);
            }
        }
        payCreateVO.setPayInfo(payResult);
        payCreateVO.setOrderId(detail.getOrderId());
        payCreateVO.setOrderSn(detail.getOrderSn());
        payCreateVO.setOrderAmount(detail.getUnpaidAmount());
        return payCreateVO;
    }

    private boolean isProfitSharingAware(String payType, PayService payService) {
        return ("alipay".equals(payType) || isHuifuPayType(payType))
                && payService instanceof ProfitSharingAwarePayService;
    }

    private PayCreateVO.PayInfo payWithProfitSharingContext(
            PayService payService, OrderVO detail, String openid, String clientType,
            ProfitSharingPaymentContext context) {
        if (payService instanceof ProfitSharingAwarePayService awarePayService) {
            return awarePayService.pay(detail, openid, clientType, context);
        }
        return payService.pay(detail, openid, clientType);
    }

    private boolean isHuifuPayType(String payType) {
        return "huifuWechat".equals(payType) || "huifuAlipay".equals(payType);
    }

    /**
     * 微信下单：先判定形态（合单 / 服务商 / 直连），非直连时为已拆的子单补建 paylog 行再下单。
     * <p>POS 收银台恒直连：其支付方式为付款码，不在平台收付通的支持范围内。
     *
     * @param detail     订单详情，父单 paylog 必须已创建（合单商户单号取其 paySn）
     * @param adminType  请求来源，{@code pos} 表示 POS 收银台
     * @param payType    支付方式编码，同时作为子单 paylog 的 payCode
     */
    private PayCreateVO.PayInfo payWechat(OrderVO detail, String openid, String clientType, String adminType, String payType) {
        if ("pos".equals(adminType)) {
            return wechatPayService.pay(detail, openid, clientType);
        }
        WechatPayContext context = wechatPayModeResolver.resolve(detail);
        if (context == null || context.isDirect()) {
            return wechatModePayService.payByMode(detail, openid, clientType, context);
        }
        if ((!context.isPartner() && !context.isCombine())
                || !WECHAT_PROFIT_SHARING_CLIENT_TYPES.contains(clientType)) {
            throw new GlobalException("当前客户端不支持微信分账支付，请重新发起支付");
        }
        paylogService.bindProfitSharingChannel(detail.getPaySn(), "WECHAT");
        paylogService.createSplitPaylogs(context, detail, payType);
        return wechatModePayService.payByMode(detail, openid, clientType, context);
    }

    @Override
    public String payRefundNotify(Map<String, Object> resMap, String payType) {
        if ("wechat".equals(payType)) {
            return wechatPayService.refundNotify(resMap);
        }
        if ("wechatPartner".equals(payType)) {
            return wechatModePayService.partnerRefundNotify(resMap);
        }
        if ("ali".equals(payType)) {
            return aliPayService.refundNotify(resMap);
        }
        if ("paypal".equals(payType)) {
            return paypalService.refundNotify(resMap);
        }
        // 新增渠道（汇付/ApplePay/拉卡拉）走局部工厂
        PayService newChannel = newPayChannelRegistry.get(payType);
        if (newChannel != null) {
            return newChannel.refundNotify(resMap);
        }
        return null;
    }

    @Override
    public String payNotify(Map<String, Object> resMap, String payType) {
        if ("wechat".equals(payType)) {
            return wechatPayService.payNotify(resMap);
        }
        // 合单与服务商各用独立回调地址，按地址分派解析器，不对报文结构做猜测
        if ("wechatCombine".equals(payType)) {
            return wechatModePayService.combineNotify(resMap);
        }
        if ("wechatPartner".equals(payType)) {
            return wechatModePayService.partnerNotify(resMap);
        }
        if ("ali".equals(payType)) {
            return aliPayService.payNotify(resMap);
        }
        if ("paypal".equals(payType)) {
            return paypalService.payNotify(resMap);
        }
        // 新增渠道（汇付/ApplePay/拉卡拉）走局部工厂
        PayService newChannel = newPayChannelRegistry.get(payType);
        if (newChannel != null) {
            return newChannel.payNotify(resMap);
        }
        return null;
    }

    /**
     * 创建定金支付订单（预售订单专用）
     * 此方法专门用于预售订单的定金支付，与普通订单支付逻辑分离
     */
    @Override
    public PayCreateVO createDepositPayment(Integer orderId, String payType, String code) {
        log.info("开始创建定金支付，订单ID: {}, 支付类型: {}", orderId, payType);

        PayCreateVO payCreateVO = new PayCreateVO();
        if (payType == null) {
            throw new GlobalException(messageHelper.get("error.pay.method.required"));
        }

        // 获取客户端类型
        String clientType = HeaderUtils.getClientType();
        String adminType = HeaderUtils.getAdminType();
        if ("pos".equals(adminType)) {
            clientType = HeaderUtils.getPosPayType();
        }

        // 获取 openid（微信支付需要）
        String openid = null;
        if (code != null) {
            // 获取微信配置
            String appId = configService.getConfigByCode(SettingsEnum.WECHAT_MINI_PROGRAM_APP_ID.getBizCode()).getBizVal();
            String appSecret = configService.getConfigByCode(SettingsEnum.WECHAT_MINI_PROGRAM_SECRET.getBizCode()).getBizVal();
            String result = wechatApiClient.code2Session(appId, appSecret, code, "authorization_code");
            Code2SessionResult code2SessionResult = JsonUtil.fromJson(result, Code2SessionResult.class);
            // 错误code -1,40029，45011,40226
            if (code2SessionResult.getErrcode() != null && code2SessionResult.getErrcode() != 0) {
                throw new GlobalException(code2SessionResult.getErrmsg());
            }
            openid = code2SessionResult.getOpenid();
        }
        if (ObjectUtil.equals("wechat", clientType) && "wechat".equals(payType)) {
            // 微信内支付 - 需要获取公众号的openid (authorizeType=1)
            UserAuthorize userAuthorize = userAuthorizeService.getOne(new LambdaQueryWrapper<UserAuthorize>()
                    .eq(UserAuthorize::getUserId, SecurityUtils.getCurrentUserId())
                    .eq(UserAuthorize::getAuthorizeType, AuthorizeTypeEnum.WECHAT.getCode()));
            if (userAuthorize != null) {
                openid = userAuthorize.getOpenId();
            }
        }

        // 查询订单详情
        OrderVO orderDetail = orderService.detail(orderId);
        assertOrderShopsCanPurchase(orderDetail);

        // 校验订单是否是预售订单
        if (orderDetail.getIsPresale() == null || orderDetail.getIsPresale() != 1) {
            throw new GlobalException(messageHelper.get("error.presale.not.presale.order"));
        }

        // 校验是否是定金预售
        if (orderDetail.getPresaleType() == null || orderDetail.getPresaleType() != 2) {
            throw new GlobalException(messageHelper.get("error.presale.not.deposit.order"));
        }

        // 查询预售订单信息
        com.tigshop.bean.vo.order.OrderPresaleVO orderPresaleVO = orderPresaleService.getPresaleVOByOrderId(orderId);
        if (orderPresaleVO == null) {
            throw new GlobalException(messageHelper.get("error.presale.info.not.exist"));
        }

        // 校验预售订单状态（必须是待支付定金状态）
        if (orderPresaleVO.getPresaleStatus() != 0) {
            throw new GlobalException(messageHelper.get("error.presale.deposit.paid.or.invalid"));
        }

        // 校验订单是否可以支付
        if (!orderDetail.getAvailableActions().getToPay()) {
            if (orderDetail.getOrderStatus().equals(OrderStatusEnum.CANCELLED.getCode())) {
                throw new GlobalException(messageHelper.get("error.order.already.cancelled"));
            } else {
                throw new GlobalException(messageHelper.get("error.order.already.paid"));
            }
        }

        // 【关键修复】将订单的未支付金额临时设置为定金金额
        // 这样 creatPayLog 和支付服务会使用定金金额而不是订单总金额
        BigDecimal depositAmount = orderPresaleVO.getDepositAmount();
        if (depositAmount == null || depositAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new GlobalException(messageHelper.get("error.presale.deposit.amount.config.error"));
        }

        log.info("订单[{}]定金支付，订单总金额: {}, 定金金额: {}", orderId, orderDetail.getTotalAmount(), depositAmount);

        // 临时修改订单的未支付金额为定金金额
        orderDetail.setUnpaidAmount(depositAmount);

        // 创建支付日志（此时会使用定金金额）
        Paylog paylog = paylogService.creatPayLog(null, orderDetail, payType, 0);
        orderDetail.setPaySn(paylog.getPaySn());

        // 调用相应的支付服务
        PayCreateVO.PayInfo payResult = new PayCreateVO.PayInfo();
        if ("wechat".equals(payType)) {
            payResult = wechatPayService.pay(orderDetail, openid, clientType);
        } else if ("alipay".equals(payType)) {
            payResult = aliPayService.pay(orderDetail, openid, clientType);
        } else if ("paypal".equals(payType)) {
            payResult = paypalService.pay(orderDetail, openid, clientType);
        } else if (newPayChannelRegistry.get(payType) != null) {
            // 新增渠道（汇付/ApplePay/拉卡拉）走局部工厂
            payResult = newPayChannelRegistry.get(payType).pay(orderDetail, openid, clientType);
        } else if (PayConstants.CASH_PAY_CODE.equals(payType)) {
            // 现金支付（POS收银台）
            throw new GlobalException(messageHelper.get("error.presale.deposit.cash.not.supported"));
        } else {
            throw new GlobalException(messageHelper.get("error.pay.method.unsupported"));
        }

        // 构建返回结果
        payCreateVO.setPayInfo(payResult);
        payCreateVO.setOrderId(orderDetail.getOrderId());
        payCreateVO.setOrderSn(orderDetail.getOrderSn());
        payCreateVO.setOrderAmount(depositAmount); // 定金金额

        log.info("定金支付单创建成功，订单ID: {}, 支付单号: {}, 定金金额: {}", orderId, paylog.getPaySn(), depositAmount);

        return payCreateVO;
    }
}
