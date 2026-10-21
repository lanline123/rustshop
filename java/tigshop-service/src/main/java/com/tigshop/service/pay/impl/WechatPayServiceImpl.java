package com.tigshop.service.pay.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.binarywang.wxpay.bean.notify.CombineNotifyResult;
import com.github.binarywang.wxpay.bean.notify.SignatureHeader;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyV3Response;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyV3Result;
import com.github.binarywang.wxpay.bean.notify.WxPayPartnerNotifyV3Result;
import com.github.binarywang.wxpay.bean.notify.WxPayPartnerRefundNotifyV3Result;
import com.github.binarywang.wxpay.bean.notify.WxPayRefundNotifyV3Result;
import com.github.binarywang.wxpay.bean.request.*;
import com.github.binarywang.wxpay.bean.result.*;
import com.github.binarywang.wxpay.bean.result.enums.TradeTypeEnum;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.v3.util.SignUtils;
import com.tigshop.bean.bo.PayPlatformStateBO;
import com.tigshop.bean.bo.pay.WechatPayContext;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.finance.Paylog;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.bean.vo.order.pay.PayCreateVO;
import com.tigshop.bean.vo.order.pay.PayNotifyVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.IpUtils;
import com.tigshop.config.PayServiceFactory;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.service.finance.PaylogService;
import com.tigshop.service.pay.PayService;
import com.tigshop.service.pay.WechatModePayService;
import com.tigshop.service.pay.WechatPayModeResolver;
import com.tigshop.service.setting.ConfigService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.tigshop.bean.enums.order.RefundNotifyType.REFUND_SUCCESS;

/**
 * 订单支付-微信
 *
 * @author Tigshop团队
 * @create 2024-09-30 15:16:06
 */
@Service("WechatPayService")
@Slf4j
public class WechatPayServiceImpl extends PayBaseServiceImpl implements PayService, WechatModePayService {

    /**
     * 合单与服务商各用独立回调地址，回调侧按地址分派解析器，不对 payload 做结构猜测；
     * 直连沿用既有的 {@code wechat}
     */
    private static final String NOTIFY_TYPE_PARTNER = "wechatPartner";
    private static final String NOTIFY_TYPE_COMBINE = "wechatCombine";

    private static final String CURRENCY_CNY = "CNY";
    private static final String DESCRIPTION = "商品购买";
    private static final BigDecimal FEN_PER_YUAN = new BigDecimal(100);
    private static final String PAY_SUCCESS_EVENT = "TRANSACTION.SUCCESS";
    private static final String PAY_CODE_WECHAT = "wechat";

    /**
     * 上溯父单时的深度上限。提交时拆单只一层，支付后拆分发货会再拆一层，正常不超过 2 层；
     * 设上限是为了兜住 parent_order_id 脏数据造成的父子环，避免死循环。
     */
    private static final int MAX_ORDER_ANCESTRY_DEPTH = 8;

    @Resource
    private ConfigService configService;
    @Resource
    private PayServiceFactory wxPayServiceFactory;
    @Resource
    private PaylogService paylogService;
    @Resource
    private WechatPayModeResolver wechatPayModeResolver;
    /**
     * 直接用 Mapper 而非 OrderService：支付实现被订单链路依赖，注入 OrderService 会形成循环依赖
     */
    @Resource
    private OrderMapper orderMapper;

    /**
     * 获得统一订单请求
     *
     * @param outTradeNo   订单编号
     * @param totalFee     订单金额
     * @param notifyUrl    回调地址
     * @param openid       openid
     * @param wxPayService 支付服务
     * @return WxPayUnifiedOrderV3Request
     */
    public WxPayUnifiedOrderV3Request getUnifiedOrderV3Request(String outTradeNo, BigDecimal totalFee, String notifyUrl, String openid, WxPayService wxPayService) {
        WxPayConfig wxPayConfig = wxPayService.getConfig();
        WxPayUnifiedOrderV3Request unifiedOrderV3Request = new WxPayUnifiedOrderV3Request();
        unifiedOrderV3Request.setAppid(wxPayConfig.getAppId());
        unifiedOrderV3Request.setMchid(wxPayConfig.getMchId());
        unifiedOrderV3Request.setDescription("商品购买");

        unifiedOrderV3Request.setOutTradeNo(outTradeNo);
        unifiedOrderV3Request.setNotifyUrl(notifyUrl);
        WxPayUnifiedOrderV3Request.Amount amount = new WxPayUnifiedOrderV3Request.Amount();
        // 分为单位
        amount.setTotal(totalFee.multiply(new BigDecimal(100))
                                .intValue());
        amount.setCurrency("CNY");
        unifiedOrderV3Request.setAmount(amount);
        String wechatPaySubMchId = configService.getConfigByCode(SettingsEnum.WECHAT_PAY_SUB_MCHID.getBizCode())
                                                .getBizVal();
        String wechatMchidType = configService.getConfigByCode(SettingsEnum.WECHAT_MCHID_TYPE.getBizCode())
                                              .getBizVal();
        if (Objects.equals(wechatMchidType, "2") && wechatPaySubMchId != null) {
            unifiedOrderV3Request.setAppid(wxPayConfig.getAppId());
        } else {
            unifiedOrderV3Request.setMchid(wxPayConfig.getMchId());
        }
        if (StrUtil.isNotEmpty(openid)) {
            WxPayUnifiedOrderV3Request.Payer payer = new WxPayUnifiedOrderV3Request.Payer();
            payer.setOpenid(openid);
            unifiedOrderV3Request.setPayer(payer);
        }
        // 直连形态一律不下发 settle_info：分账标识只在服务商 / 合单请求体里各自设置。
        // 开启线上分账后仍走直连的只有 POS、定金、充值与未进件降级，这些资金进平台主商户、
        // 没有分账对象，带标识会让资金冻结到微信自动解冻，且主商户无直连分账权限时会被拒单。
        return unifiedOrderV3Request;
    }

    /**
     * 获得统一订单请求
     *
     * @param outTradeNo   订单编号
     * @param totalFee     订单金额
     * @param authCode     付款码编码
     * @param wxPayService 支付服务
     * @return WxPayUnifiedOrderV3Request
     */
    public WxPayMicropayRequest getWxPayMicropayRequest(String outTradeNo, BigDecimal totalFee, @NotBlank String authCode, WxPayService wxPayService) {
        WxPayConfig wxPayConfig = wxPayService.getConfig();
        WxPayMicropayRequest request = new WxPayMicropayRequest();
        request.setAppid(wxPayConfig.getAppId());
        request.setOutTradeNo(outTradeNo);
        request.setAuthCode(authCode);
        request.setTotalFee(totalFee.multiply(new BigDecimal(100))
                                    .intValue());
        String wechatPaySubMchId = configService.getConfigByCode(SettingsEnum.WECHAT_PAY_SUB_MCHID.getBizCode())
                                                .getBizVal();
        String wechatMchidType = configService.getConfigByCode(SettingsEnum.WECHAT_MCHID_TYPE.getBizCode())
                                              .getBizVal();
        if (Objects.equals(wechatMchidType, "2") && wechatPaySubMchId != null) {
            request.setAppid(wxPayConfig.getAppId());
        } else {
            request.setMchId(wxPayConfig.getMchId());
        }
        request.setBody("支付");
        request.setFeeType("CNY");
        // 付款码支付恒走直连（平台收付通不支持付款码场景），直连商户带分账标识会被微信拒单，故不设 profit_sharing
//        request.setSpbillCreateIp("106.228.44.213");
        request.setSpbillCreateIp("127.0.0.1");
        return request;
    }


    @Override
    public PayCreateVO.PayInfo pay(OrderVO orderVO, String openid, String clientType) {
        return switch (clientType) {
            case "wechat" -> jsApiPay(orderVO, openid, clientType);
            case "miniProgram" -> miniPay(orderVO, openid, clientType);
            case "app" -> {
                PayCreateVO.PayInfo payInfo = appPay(orderVO, openid, clientType);
                payInfo.setProvider("wxpay");
                yield payInfo;
            }
            case "pc" -> new PayCreateVO.PayInfo(nativePay(orderVO, openid, clientType), "");
            case "h5" -> new PayCreateVO.PayInfo(htmlPay(orderVO, openid, clientType));
            case "bar-code" -> paymentCodePay(orderVO, clientType);
            default -> null;
        };
    }


    @Override
    public PayCreateVO.PayInfo payByMode(OrderVO orderVO, String openid, String clientType, WechatPayContext context) {
        TradeTypeEnum tradeType = resolveTradeType(clientType);
        if (context == null || context.isDirect() || tradeType == null) {
            // 付款码等不在收付通支持场景内的客户端类型落在此处，与改造前完全一致
            return pay(orderVO, openid, clientType);
        }
        return context.isCombine()
                ? combinePay(orderVO, openid, tradeType, clientType, context)
                : partnerPay(orderVO, openid, tradeType, clientType, context);
    }

    /**
     * 客户端类型到微信交易类型的映射，与直连分支的 {@link #pay} 逐项对应；
     * 返回 null 表示该客户端类型不支持收付通形态，调用方须降级直连。
     */
    private TradeTypeEnum resolveTradeType(String clientType) {
        if (clientType == null) {
            return null;
        }
        return switch (clientType) {
            case "wechat", "miniProgram" -> TradeTypeEnum.JSAPI;
            case "app" -> TradeTypeEnum.APP;
            case "pc" -> TradeTypeEnum.NATIVE;
            case "h5" -> TradeTypeEnum.H5;
            default -> null;
        };
    }

    /**
     * 合单支付：一笔支付覆盖多个二级商户的子单，每个子单独立商户单号与分账标识。
     */
    private PayCreateVO.PayInfo combinePay(OrderVO orderVO, String openid, TradeTypeEnum tradeType, String clientType, WechatPayContext context) {
        WxPayService wxPayService = wxPayServiceFactory.getWxPayV3Service(clientType);
        WxPayConfig wxPayConfig = wxPayService.getConfig();

        CombineTransactionsRequest request = new CombineTransactionsRequest();
        request.setCombineAppid(wxPayConfig.getAppId());
        request.setCombineMchid(wxPayConfig.getMchId());
        request.setCombineOutTradeNo(context.getCombineOutTradeNo());
        request.setNotifyUrl(getPayNotifyUrl(NOTIFY_TYPE_COMBINE));
        request.setSubOrders(buildCombineSubOrders(context, wxPayConfig.getMchId()));

        if (TradeTypeEnum.JSAPI == tradeType) {
            requireOpenid(openid, orderVO);
            CombineTransactionsRequest.CombinePayerInfo payerInfo = new CombineTransactionsRequest.CombinePayerInfo();
            // 支付者以平台 appId 授权，故用 openid 而非 subOpenid；子单也因此不下发 subAppid
            payerInfo.setOpenid(openid);
            request.setCombinePayerInfo(payerInfo);
        }
        if (TradeTypeEnum.H5 == tradeType) {
            CombineTransactionsRequest.SceneInfo sceneInfo = new CombineTransactionsRequest.SceneInfo();
            sceneInfo.setPayerClientIp(IpUtils.getIpAddr());
            CombineTransactionsRequest.H5Info h5Info = new CombineTransactionsRequest.H5Info();
            h5Info.setType("Wap");
            sceneInfo.setH5Info(h5Info);
            request.setSceneInfo(sceneInfo);
        }

        try {
            CombineTransactionsResult result = wxPayService.combine(tradeType, request);
            return buildCombinePayInfo(result, request, wxPayConfig, tradeType);
        } catch (WxPayException e) {
            throw new GlobalException(e.getMessage());
        }
    }

    private List<CombineTransactionsRequest.SubOrders> buildCombineSubOrders(WechatPayContext context, String spMchId) {
        List<CombineTransactionsRequest.SubOrders> subOrders = new ArrayList<>(context.getSubOrders()
                                                                                     .size());
        for (WechatPayContext.SubOrderCtx sub : context.getSubOrders()) {
            CombineTransactionsRequest.Amount amount = new CombineTransactionsRequest.Amount();
            amount.setTotalAmount(toFen(sub.getAmount()));
            amount.setCurrency(CURRENCY_CNY);

            CombineTransactionsRequest.SubOrders subOrder = new CombineTransactionsRequest.SubOrders();
            subOrder.setMchid(spMchId);
            subOrder.setSubMchid(sub.getSubMchId());
            subOrder.setOutTradeNo(sub.getPaySn());
            subOrder.setDescription(DESCRIPTION);
            subOrder.setAmount(amount);
            // 自营子单无分账对象，与服务商单同口径不下发标识，避免该笔资金被无谓冻结
            if (sub.isProfitSharing()) {
                CombineTransactionsRequest.SettleInfo settleInfo = new CombineTransactionsRequest.SettleInfo();
                settleInfo.setProfitSharing(true);
                subOrder.setSettleInfo(settleInfo);
            }
            subOrders.add(subOrder);
        }
        return subOrders;
    }

    private PayCreateVO.PayInfo buildCombinePayInfo(CombineTransactionsResult result, CombineTransactionsRequest request, WxPayConfig wxPayConfig, TradeTypeEnum tradeType) {
        return switch (tradeType) {
            case JSAPI -> {
                CombineTransactionsResult.JsapiResult jsapiResult = result.getPayInfo(TradeTypeEnum.JSAPI, request.getCombineAppid(), request.getCombineMchid(), wxPayConfig.getPrivateKey());
                PayCreateVO.PayInfo payInfo = new PayCreateVO.PayInfo();
                BeanUtils.copyProperties(jsapiResult, payInfo);
                yield payInfo;
            }
            case APP -> {
                CombineTransactionsResult.AppResult appResult = result.getPayInfo(TradeTypeEnum.APP, request.getCombineAppid(), request.getCombineMchid(), wxPayConfig.getPrivateKey());
                PayCreateVO.PayInfo payInfo = new PayCreateVO.PayInfo();
                payInfo.setAppId(appResult.getAppid());
                payInfo.setPartnerId(appResult.getPartnerid());
                payInfo.setPrepayId(appResult.getPrepayid());
                payInfo.setPackageValue(appResult.getPackageValue());
                payInfo.setNonceStr(appResult.getNoncestr());
                payInfo.setTimeStamp(appResult.getTimestamp());
                // SDK 的合单 AppResult 未生成 sign 字段（直连版有），按微信「APP 调起支付」文档的签名串补签
                payInfo.setSign(signAppPayInfo(appResult.getAppid(), appResult.getTimestamp(), appResult.getNoncestr(), appResult.getPrepayid(), wxPayConfig));
                payInfo.setProvider("wxpay");
                yield payInfo;
            }
            case NATIVE -> new PayCreateVO.PayInfo(result.getCodeUrl(), "");
            case H5 -> new PayCreateVO.PayInfo(result.getH5Url());
        };
    }

    /**
     * 普通服务商支付：整单资金进单一二级商户，整单打分账标识。
     */
    private PayCreateVO.PayInfo partnerPay(OrderVO orderVO, String openid, TradeTypeEnum tradeType, String clientType, WechatPayContext context) {
        WxPayService wxPayService = wxPayServiceFactory.getWxPayV3PartnerService(clientType, context.getSubMchId(), context.getSubAppId());
        WxPayConfig wxPayConfig = wxPayService.getConfig();

        WxPayPartnerUnifiedOrderV3Request request = new WxPayPartnerUnifiedOrderV3Request();
        request.setSpAppid(wxPayConfig.getAppId());
        request.setSpMchId(wxPayConfig.getMchId());
        request.setSubMchId(context.getSubMchId());
        // 不下发 subAppid：支付者的 openid 是在平台 appId 下授权的，一旦声明子 appId
        // 微信会要求改用 sub_openid，且 JSAPI 调起参数须用子 appId 签名，与现有授权链路不符
        request.setDescription(DESCRIPTION);
        request.setOutTradeNo(orderVO.getPaySn());
        request.setNotifyUrl(getPayNotifyUrl(NOTIFY_TYPE_PARTNER));

        WxPayPartnerUnifiedOrderV3Request.Amount amount = new WxPayPartnerUnifiedOrderV3Request.Amount();
        amount.setTotal(toFen(orderVO.getUnpaidAmount()));
        amount.setCurrency(CURRENCY_CNY);
        request.setAmount(amount);

        // 无分账对象时不下发：平台自营进件后也走本形态，带标识会让资金冻结等待一笔永不发起的分账
        if (context.isProfitSharing()) {
            WxPayPartnerUnifiedOrderV3Request.SettleInfo settleInfo = new WxPayPartnerUnifiedOrderV3Request.SettleInfo();
            settleInfo.setProfitSharing(true);
            request.setSettleInfo(settleInfo);
        }

        if (TradeTypeEnum.JSAPI == tradeType) {
            requireOpenid(openid, orderVO);
            WxPayPartnerUnifiedOrderV3Request.Payer payer = new WxPayPartnerUnifiedOrderV3Request.Payer();
            payer.setSpOpenid(openid);
            request.setPayer(payer);
        }
        if (TradeTypeEnum.H5 == tradeType) {
            WxPayPartnerUnifiedOrderV3Request.SceneInfo sceneInfo = new WxPayPartnerUnifiedOrderV3Request.SceneInfo();
            sceneInfo.setPayerClientIp(IpUtils.getIpAddr());
            WxPayPartnerUnifiedOrderV3Request.H5Info h5Info = new WxPayPartnerUnifiedOrderV3Request.H5Info();
            h5Info.setType("Wap");
            sceneInfo.setH5Info(h5Info);
            request.setSceneInfo(sceneInfo);
        }

        try {
            // 不走 createPartnerOrderV3：它按 subMchId 生成 APP 的 partnerid、并按 subAppid 生成 JSAPI 的 appId，
            // 而服务商模式下调起参数须用服务商商户号与服务商 appId，故自行组装
            WxPayUnifiedOrderV3Result result = wxPayService.unifiedPartnerOrderV3(tradeType, request);
            return buildPartnerPayInfo(result, wxPayConfig, tradeType);
        } catch (WxPayException e) {
            throw new GlobalException(e.getMessage());
        }
    }

    private PayCreateVO.PayInfo buildPartnerPayInfo(WxPayUnifiedOrderV3Result result, WxPayConfig wxPayConfig, TradeTypeEnum tradeType) {
        return switch (tradeType) {
            case JSAPI -> {
                WxPayUnifiedOrderV3Result.JsapiResult jsapiResult = result.getPayInfo(TradeTypeEnum.JSAPI, wxPayConfig.getAppId(), wxPayConfig.getMchId(), wxPayConfig.getPrivateKey());
                PayCreateVO.PayInfo payInfo = new PayCreateVO.PayInfo();
                BeanUtils.copyProperties(jsapiResult, payInfo);
                yield payInfo;
            }
            case APP -> {
                WxPayUnifiedOrderV3Result.AppResult appResult = result.getPayInfo(TradeTypeEnum.APP, wxPayConfig.getAppId(), wxPayConfig.getMchId(), wxPayConfig.getPrivateKey());
                PayCreateVO.PayInfo payInfo = new PayCreateVO.PayInfo();
                payInfo.setAppId(appResult.getAppid());
                payInfo.setPartnerId(appResult.getPartnerId());
                payInfo.setPrepayId(appResult.getPrepayId());
                payInfo.setPackageValue(appResult.getPackageValue());
                payInfo.setNonceStr(appResult.getNoncestr());
                payInfo.setTimeStamp(appResult.getTimestamp());
                payInfo.setSign(appResult.getSign());
                payInfo.setProvider("wxpay");
                yield payInfo;
            }
            case NATIVE -> new PayCreateVO.PayInfo(result.getCodeUrl(), "");
            case H5 -> new PayCreateVO.PayInfo(result.getH5Url());
        };
    }

    /**
     * 按微信「APP 调起支付」文档的签名串 {@code appid\ntimestamp\nnoncestr\nprepayid\n} 计算 paySign。
     * 合单结果的 AppResult 不带 sign（直连与服务商版由 SDK 生成），需在此补齐。
     */
    private String signAppPayInfo(String appId, String timestamp, String nonceStr, String prepayId, WxPayConfig wxPayConfig) {
        String signStr = String.format("%s\n%s\n%s\n%s\n", appId, timestamp, nonceStr, prepayId);
        return SignUtils.sign(signStr, wxPayConfig.getPrivateKey());
    }

    private void requireOpenid(String openid, OrderVO orderVO) {
        if (StrUtil.isBlank(openid)) {
            log.error("[微信支付形态] JSAPI 支付缺少 openid, orderId={}, paySn={}", orderVO.getOrderId(), orderVO.getPaySn());
            throw new GlobalException("未获取到微信用户信息，请重新进入支付页");
        }
    }

    private Integer toFen(BigDecimal yuan) {
        return Optional.ofNullable(yuan)
                       .orElse(BigDecimal.ZERO)
                       .multiply(FEN_PER_YUAN)
                       .intValue();
    }

    @Override
    public PayNotifyVO payRefund(String outTradeNo, String refundNo, BigDecimal refundAmount, BigDecimal totalAmount, String clientType, String subMchId) {
        WxPayService wxPayService;
        WxPayRefundV3Request wxPayRefundRequest = new WxPayRefundV3Request();
        try {
            wxPayService = wxPayServiceFactory.getWxPayV3Service(clientType);
            RefundTarget target = resolveRefundTarget(outTradeNo, totalAmount, subMchId, wxPayService);
            // 实例化退款金额
            WxPayRefundV3Request.Amount amount = new WxPayRefundV3Request.Amount();
            amount.setCurrency(CURRENCY_CNY);
            amount.setTotal(WxPayRefundRequest.yuanToFen(String.valueOf(target.total())));
            amount.setRefund(WxPayRefundRequest.yuanToFen(String.valueOf(refundAmount)));
            wxPayRefundRequest.setAmount(amount);
            // 其他退款参数
            wxPayRefundRequest.setOutRefundNo(refundNo);
            if (StrUtil.isNotBlank(target.transactionId())) {
                // 服务商拆单子单的 pay_sn 从未作为 out_trade_no 提交给微信，只能用交易号寻址
                wxPayRefundRequest.setTransactionId(target.transactionId());
            } else {
                wxPayRefundRequest.setOutTradeNo(target.outTradeNo());
            }
            // 直连时为 null；Gson 不序列化 null 字段，故请求体中不会出现 sub_mchid
            wxPayRefundRequest.setSubMchid(target.subMchId());
            wxPayRefundRequest.setNotifyUrl(getRefundNotifyUrl(target.isPartnerLike() ? NOTIFY_TYPE_PARTNER : PAY_CODE_WECHAT));
            wxPayRefundRequest.setReason("退款");
        } catch (Exception e) {
            throw new RefundNotRequestedException(e.getMessage());
        }
        try {
            WxPayRefundV3Result wxPayRefundV3Result = wxPayService.refundV3(wxPayRefundRequest);
            if (StrUtil.equals(wxPayRefundV3Result.getStatus(), "SUCCESS")) {
                return new PayNotifyVO(wxPayRefundV3Result.getStatus(), "退款成功");
            }
            return new PayNotifyVO(wxPayRefundV3Result.getStatus(), "退款失败");
        } catch (WxPayException e) {
            // 微信已返回明确的“商户余额不足”时，退款请求尚未被受理，本地占额可以释放；
            // 网络/超时等 errCode 为空的异常仍必须保留占额，等待查单确认，不能自动重放。
            if (isRefundRejectedBeforeAcceptance(e)) {
                throw new RefundNotRequestedException(e.getMessage());
            }
            throw new GlobalException(e.getMessage());
        }
    }

    static boolean isRefundRejectedBeforeAcceptance(WxPayException e) {
        String errCode = StrUtil.blankToDefault(e.getErrCode(), "");
        return "NOTENOUGH".equalsIgnoreCase(errCode) || "NOT_ENOUGH".equalsIgnoreCase(errCode);
    }

    /** 明确未调用 refundV3，或微信明确拒绝且未受理，可安全释放本地退款占额。 */
    public static class RefundNotRequestedException extends GlobalException {
        public RefundNotRequestedException(String message) {
            super(message);
        }
    }

    /**
     * 一笔微信退款的寻址结果。
     * <p>{@code transactionId} 非空表示用交易号寻址，否则用 {@code outTradeNo}；
     * {@code subMchId} 非空即服务商形态（含合单），需在请求体下发 sub_mchid 并走服务商退款通知地址。
     *
     * @param total 必须等于**原交易总额**（微信硬约束），不是本次退款金额
     */
    private record RefundTarget(String outTradeNo, String transactionId, BigDecimal total, String subMchId) {

        boolean isPartnerLike() {
            return StrUtil.isNotBlank(subMchId);
        }

        static RefundTarget direct(String outTradeNo, BigDecimal total) {
            return new RefundTarget(outTradeNo, null, total, null);
        }
    }

    /**
     * 判定退款寻址方式（DESIGN D5 / CR-20260730-1310 ①）。
     * <p>形态无法从本地数据可靠推断：不在 paylog 上留形态痕迹（§6 ②），且店铺可能在支付后换绑子商户，
     * 故先向微信查单确认。查单能力不对称——{@code queryCombine} 无需预先知道 sub_mchid 且回传
     * {@code sub_orders[].mchid}，可权威发现归属；{@code queryPartnerOrderV3} 必须先知道 sub_mchid，
     * 只能验证候选值。因此顺序固定为「先判合单，再验服务商，最后回退直连」。
     *
     * @param outTradeNo        退款所针对的那条 paylog 的 pay_sn
     * @param callerTotal       调用方算出的总金额，直连形态原样使用
     * @param candidateSubMchId 按店铺现查的候选二级商户号，可能为 null（未开分账 / 未进件 / 已解绑）
     */
    private RefundTarget resolveRefundTarget(String outTradeNo, BigDecimal callerTotal, String candidateSubMchId, WxPayService wxPayService) {
        if (!wechatPayModeResolver.isOnlineProfitSharing()) {
            return RefundTarget.direct(outTradeNo, callerTotal);
        }
        Paylog paylog = findPaylogByPaySn(outTradeNo);
        if (paylog == null) {
            log.warn("[微信退款] 商户单号[{}]无对应支付日志，按直连退款处理", outTradeNo);
            return RefundTarget.direct(outTradeNo, callerTotal);
        }
        // 以根单为锚而非直接父单：支付后的拆分发货会在子单之下再拆一层，只上溯一级会把孙单判错形态
        Paylog root = findRootPaylogOf(paylog);
        boolean ownNumber = root == null || Objects.equals(paylog.getPaySn(), root.getPaySn());

        // 一、合单：根单商户单号即合单单号，查得到就以微信回传为准，天然免疫子商户换绑
        if (root != null) {
            RefundTarget combineTarget = tryResolveAsCombine(root.getPaySn(), outTradeNo, wxPayService);
            if (combineTarget != null) {
                return combineTarget;
            }
        }

        // 二、服务商：候选号缺失时无从查单也无从退款，只能按直连处理
        if (StrUtil.isBlank(candidateSubMchId)) {
            return RefundTarget.direct(outTradeNo, callerTotal);
        }
        // ownNumber 表示该 pay_sn 确实作为 out_trade_no 提交过微信：未拆单的单，
        // 以及历史拆单里复制了根单号的行（createSplitOrder）。其余是本地新生成的子单号，微信不认。
        String transactionId = ownNumber ? null : StrUtil.trimToNull(paylog.getTransactionId());
        if (!ownNumber && transactionId == null) {
            // 交易号只能来自支付回调，缺号说明回调未达；用根单交易号硬退会退到错误的原交易上
            throw new GlobalException("该子单尚未收到微信支付通知，暂不能发起退款，请稍后重试");
        }
        // 服务商形态整笔只提交过一次，原交易总额在根单行上
        BigDecimal total = ownNumber ? callerTotal : requireRootTotal(root, outTradeNo);
        if (!verifyPartnerOrder(transactionId, ownNumber ? outTradeNo : null, candidateSubMchId, wxPayService)) {
            return RefundTarget.direct(outTradeNo, callerTotal);
        }
        return new RefundTarget(outTradeNo, transactionId, total, candidateSubMchId);
    }

    /**
     * 用合单查单确认归属：命中则返回该子单的权威 sub_mchid 与原交易金额。
     *
     * @return null 表示这不是合单订单（业务态查不到），调用方继续判服务商形态
     */
    private RefundTarget tryResolveAsCombine(String combineOutTradeNo, String childPaySn, WxPayService wxPayService) {
        if (StrUtil.isBlank(combineOutTradeNo)) {
            return null;
        }
        CombineQueryResult result;
        try {
            result = wxPayService.queryCombine(combineOutTradeNo);
        } catch (WxPayException e) {
            if (isOrderNotExists(e)) {
                return null;
            }
            // 网络 / 超时 / 签名等非业务失败不得静默回退直连：那会把服务商单当直连退，必然被微信拒
            throw new GlobalException("微信合单查询失败，暂不能发起退款：" + e.getMessage());
        }
        List<CombineQueryResult.SubOrders> subOrders = Optional.ofNullable(result.getSubOrders())
                                                              .orElse(List.of());
        for (CombineQueryResult.SubOrders subOrder : subOrders) {
            if (!Objects.equals(childPaySn, subOrder.getOutTradeNo())) {
                continue;
            }
            BigDecimal total = fenToYuan(subOrder.getAmount() == null ? null : subOrder.getAmount()
                                                                                      .getTotalAmount());
            if (total == null) {
                throw new GlobalException("微信合单查询未返回该子单金额，暂不能发起退款");
            }
            // 合单子单的 out_trade_no 在微信侧存在，用它寻址即可，无需交易号
            return new RefundTarget(childPaySn, null, total, subOrder.getMchid());
        }
        log.error("[微信退款] 合单单号[{}]的查单结果中没有子单商户单号[{}]，号段与微信侧不一致", combineOutTradeNo, childPaySn);
        throw new GlobalException("微信合单查询结果与本地单号不一致，暂不能发起退款");
    }

    /**
     * 验证候选二级商户号下确有这笔交易。
     *
     * @return false 表示业务态查不到（换绑或本就是直连单），调用方回退直连
     */
    private boolean verifyPartnerOrder(String transactionId, String outTradeNo, String subMchId, WxPayService wxPayService) {
        WxPayPartnerOrderQueryV3Request request = new WxPayPartnerOrderQueryV3Request();
        request.setSubMchId(subMchId);
        request.setTransactionId(transactionId);
        request.setOutTradeNo(outTradeNo);
        try {
            wxPayService.queryPartnerOrderV3(request);
            return true;
        } catch (WxPayException e) {
            if (isOrderNotExists(e)) {
                log.warn("[微信退款] 二级商户[{}]下查不到交易（transactionId={}, outTradeNo={}），按直连退款处理",
                        subMchId, transactionId, outTradeNo);
                return false;
            }
            throw new GlobalException("微信服务商查单失败，暂不能发起退款：" + e.getMessage());
        }
    }

    /**
     * 微信「订单不存在」是业务态而非通信失败，只有它才允许回退直连。
     * <p>判定只看 {@code errCode}：SDK 仅在拿到微信应答体时才把 {@code code} 填进 errCode
     * （见 {@code WxPayServiceApacheHttpImpl.convertException}），网络与超时失败的 errCode 恒为空，
     * 因而天然不会被误判成「订单不存在」。
     */
    private boolean isOrderNotExists(WxPayException e) {
        String errCode = e.getErrCode();
        return "ORDER_NOT_EXIST".equals(errCode) || "RESOURCE_NOT_EXISTS".equals(errCode);
    }

    /**
     * 服务商形态整笔只向微信提交过一次，原交易总额在根单行上；缺失则宁可退款失败也不用子单金额顶替
     */
    private BigDecimal requireRootTotal(Paylog root, String childPaySn) {
        BigDecimal total = root == null ? null : root.getPayAmount();
        if (total == null || total.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("[微信退款] 子单[{}]的根单支付金额缺失，无法确定原交易总额", childPaySn);
            throw new GlobalException("原交易金额缺失，暂不能发起退款");
        }
        return total;
    }

    /**
     * 按 pay_sn 取支付日志。历史拆单数据存在「父子同 pay_sn」的行（{@code createSplitOrder} 复制父号），
     * 故不能用 getOne 唯一查询，取最新一行。
     */
    private Paylog findPaylogByPaySn(String paySn) {
        if (StrUtil.isBlank(paySn)) {
            return null;
        }
        return paylogService.getOne(new LambdaQueryWrapper<Paylog>().eq(Paylog::getPaySn, paySn)
                                                                   .orderByDesc(Paylog::getPaylogId)
                                                                   .last("LIMIT 1"));
    }

    /**
     * 上溯到根单的 paylog；该 paylog 本身就属于根单（非拆单子单）时返回 null。
     * <p>提交时拆单只有一层，但支付后的拆分发货会在子单之下再拆，故必须一路上溯到根。
     * 设深度上限兜住脏数据造成的父子环。
     */
    private Paylog findRootPaylogOf(Paylog paylog) {
        Integer orderId = paylog.getOrderId();
        Integer rootOrderId = null;
        for (int depth = 0; depth < MAX_ORDER_ANCESTRY_DEPTH && orderId != null && orderId > 0; depth++) {
            Order order = orderMapper.selectById(orderId);
            Integer parentOrderId = order == null ? null : order.getParentOrderId();
            if (parentOrderId == null || parentOrderId <= 0) {
                break;
            }
            rootOrderId = parentOrderId;
            orderId = parentOrderId;
        }
        if (rootOrderId == null) {
            return null;
        }
        return paylogService.getOne(new LambdaQueryWrapper<Paylog>().eq(Paylog::getOrderId, rootOrderId)
                                                                   // 商品订单：orderType=0，避免撞充值单号
                                                                   .eq(Paylog::getOrderType, 0)
                                                                   .eq(Paylog::getPayCode, paylog.getPayCode())
                                                                   .orderByDesc(Paylog::getPaylogId)
                                                                   .last("LIMIT 1"));
    }

    private BigDecimal fenToYuan(Integer fen) {
        return fen == null ? null : new BigDecimal(fen).divide(FEN_PER_YUAN);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String payNotify(Map<String, Object> map) {
        log.info("微信支付回调：{}", JSONUtil.toJsonStr(map));
        SignatureHeader headers = new SignatureHeader(HeaderUtils.getHeaderValue("Wechatpay-Timestamp"), HeaderUtils.getHeaderValue("Wechatpay-Nonce"), HeaderUtils.getHeaderValue("Wechatpay-Signature"), HeaderUtils.getHeaderValue("Wechatpay-Serial"));
        WxPayService wxPayService = wxPayServiceFactory.getWxPayV3Service(HeaderUtils.getClientType());
        try {
            final WxPayNotifyV3Result wxPayNotifyV3Result = wxPayService.parseOrderNotifyV3Result(JSONUtil.toJsonStr(map), headers);
            // 支付成功类型
            final String eventType = "TRANSACTION.SUCCESS";
            if (wxPayNotifyV3Result.getRawData()
                                   .getEventType()
                                   .equals(eventType)) {
                // 3. 提取订单信息
                WxPayNotifyV3Result.DecryptNotifyResult result = wxPayNotifyV3Result.getResult();
                String outTradeNo = result.getOutTradeNo();
                String transactionId = result.getTransactionId();
                if (!checkOrderPayStatus(outTradeNo)) {
                    //修改订单状态
                    paySuccess(outTradeNo, transactionId);
                }
                return WxPayNotifyV3Response.success("成功");
            }
            return WxPayNotifyV3Response.fail("失败");
        } catch (WxPayException e) {
            throw new GlobalException(e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String combineNotify(Map<String, Object> map) {
        log.info("微信合单支付回调：{}", JSONUtil.toJsonStr(map));
        WxPayService wxPayService = wxPayServiceFactory.getWxPayV3Service(HeaderUtils.getClientType());
        try {
            CombineNotifyResult notifyResult = wxPayService.parseCombineNotifyResult(JSONUtil.toJsonStr(map), buildSignatureHeader());
            if (!PAY_SUCCESS_EVENT.equals(notifyResult.getRawData()
                                                      .getEventType())) {
                return WxPayNotifyV3Response.fail("失败");
            }
            CombineNotifyResult.DecryptNotifyResult result = notifyResult.getResult();
            String combineOutTradeNo = result.getCombineOutTradeNo();
            Map<String, String> transactionIdByPaySn = new HashMap<>();
            for (CombineNotifyResult.SubOrders subOrder : Optional.ofNullable(result.getSubOrders())
                                                                 .orElse(List.of())) {
                transactionIdByPaySn.put(subOrder.getOutTradeNo(), subOrder.getTransactionId());
            }
            findParentPaylog("微信合单回调", combineOutTradeNo);
            // 先补子单交易号：分账按子单 orderId 取号，父单 paySuccess 之后的定时任务立即依赖它
            int missing = paylogService.fillCombineChildTransactionIds(combineOutTradeNo, PAY_CODE_WECHAT, transactionIdByPaySn);
            if (missing > 0) {
                log.error("[微信合单回调] 合单单号[{}]有 {} 个子单未拿到交易号，这些子单无法发起分账，回调子单数={}",
                        combineOutTradeNo, missing, transactionIdByPaySn.size());
            }
            if (!checkOrderPayStatus(combineOutTradeNo)) {
                // 合单没有单一平台交易号，父单不写号；资金归属与分账一律以子单交易号为准
                paySuccess(combineOutTradeNo, null);
            }
            return WxPayNotifyV3Response.success("成功");
        } catch (WxPayException e) {
            throw new GlobalException(e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String partnerNotify(Map<String, Object> map) {
        log.info("微信服务商支付回调：{}", JSONUtil.toJsonStr(map));
        WxPayService wxPayService = wxPayServiceFactory.getWxPayV3Service(HeaderUtils.getClientType());
        try {
            WxPayPartnerNotifyV3Result notifyResult = wxPayService.parsePartnerOrderNotifyV3Result(JSONUtil.toJsonStr(map), buildSignatureHeader());
            if (!PAY_SUCCESS_EVENT.equals(notifyResult.getRawData()
                                                     .getEventType())) {
                return WxPayNotifyV3Response.fail("失败");
            }
            WxPayPartnerNotifyV3Result.DecryptNotifyResult result = notifyResult.getResult();
            String outTradeNo = result.getOutTradeNo();
            String transactionId = result.getTransactionId();
            Paylog parent = findParentPaylog("微信服务商回调", outTradeNo);
            if (parent != null) {
                // 资金去向以微信回传为准，本地绑定变更只告警不阻断
                wechatPayModeResolver.warnIfSubMchIdChanged(parent.getOrderId(), result.getSubMchid());
            }
            // 单店多供应商拆单也走本形态，子单需共用这一个交易号才能各自发起分账
            int missing = paylogService.fillPartnerChildTransactionIds(outTradeNo, PAY_CODE_WECHAT, transactionId);
            if (missing > 0) {
                log.error("[微信服务商回调] 商户单号[{}]有 {} 个子单未拿到交易号，这些子单无法发起分账", outTradeNo, missing);
            }
            if (!checkOrderPayStatus(outTradeNo)) {
                paySuccess(outTradeNo, transactionId);
            }
            return WxPayNotifyV3Response.success("成功");
        } catch (WxPayException e) {
            throw new GlobalException(e.getMessage());
        }
    }

    /**
     * 取回调对应的父单支付日志。缺行意味着钱已收到但本地无处落库，而 {@code checkOrderPayStatus} 会把这种
     * 情况当成「已处理」静默放过，故必须在此显式报错。
     */
    private Paylog findParentPaylog(String scene, String outTradeNo) {
        Paylog paylog = paylogService.getOne(new LambdaQueryWrapper<Paylog>().eq(Paylog::getPaySn, outTradeNo));
        if (paylog == null) {
            log.error("[{}] 商户单号[{}]无对应支付日志，本次回调未落库任何数据", scene, outTradeNo);
        }
        return paylog;
    }

    public WxPayOrderQueryV3Result queryOrderPay(String transactionId, String paySn) {
        WxPayService wxPayService = wxPayServiceFactory.getWxPayV3Service(HeaderUtils.getClientType());

        String wechatPayMchId = configService.getConfigByCode(SettingsEnum.WECHAT_PAY_MCHID.getBizCode())
                                             .getBizVal();

        WxPayOrderQueryV3Request wxPayOrderQueryV3Request = new WxPayOrderQueryV3Request();
        wxPayOrderQueryV3Request.setTransactionId(transactionId)
                                .setOutTradeNo(paySn)
                                .setMchid(wechatPayMchId);
        try {
            return wxPayService.queryOrderV3(wxPayOrderQueryV3Request);
        } catch (WxPayException e) {
            throw new GlobalException(e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String refundNotify(Map<String, Object> map) {
        log.info("微信退款回调：{}", JSONUtil.toJsonStr(map));
        SignatureHeader headers = new SignatureHeader(HeaderUtils.getHeaderValue("Wechatpay-Timestamp"), HeaderUtils.getHeaderValue("Wechatpay-Nonce"), HeaderUtils.getHeaderValue("Wechatpay-Signature"), HeaderUtils.getHeaderValue("Wechatpay-Serial"));
        WxPayService wxPayService = wxPayServiceFactory.getWxPayV3Service(HeaderUtils.getClientType());
        try {
            final WxPayRefundNotifyV3Result wxPayRefundNotifyV3Result = wxPayService.parseRefundNotifyV3Result(JSONUtil.toJsonStr(map), headers);
            if (wxPayRefundNotifyV3Result.getRawData()
                                         .getEventType()
                                         .equals(REFUND_SUCCESS.getCode())) {
                refundSuccess(wxPayRefundNotifyV3Result.getResult()
                                                       .getOutRefundNo());
                return WxPayNotifyV3Response.success("成功");
            }
            return WxPayNotifyV3Response.fail("失败");
        } catch (WxPayException e) {
            throw new GlobalException(e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String partnerRefundNotify(Map<String, Object> map) {
        log.info("微信服务商退款回调：{}", JSONUtil.toJsonStr(map));
        WxPayService wxPayService = wxPayServiceFactory.getWxPayV3Service(HeaderUtils.getClientType());
        try {
            WxPayPartnerRefundNotifyV3Result notifyResult = wxPayService.parsePartnerRefundNotifyV3Result(JSONUtil.toJsonStr(map), buildSignatureHeader());
            if (!REFUND_SUCCESS.getCode()
                               .equals(notifyResult.getRawData()
                                                   .getEventType())) {
                return WxPayNotifyV3Response.fail("失败");
            }
            refundSuccess(notifyResult.getResult()
                                      .getOutRefundNo());
            return WxPayNotifyV3Response.success("成功");
        } catch (WxPayException e) {
            throw new GlobalException(e.getMessage());
        }
    }

    /**
     * 从当前请求头取微信 V3 验签所需的四个头部值。
     */
    private SignatureHeader buildSignatureHeader() {
        return new SignatureHeader(HeaderUtils.getHeaderValue("Wechatpay-Timestamp"),
                HeaderUtils.getHeaderValue("Wechatpay-Nonce"),
                HeaderUtils.getHeaderValue("Wechatpay-Signature"),
                HeaderUtils.getHeaderValue("Wechatpay-Serial"));
    }

    @Override
    public PayPlatformStateBO payPlatformStatus(String outTradeNo, String tradeNo, String clientType) {
        WxPayService wxPayService = wxPayServiceFactory.getWxPayV3Service(clientType);
        //参数1: 交易号
        //参数2: 外部订单号
        try {
            WxPayOrderQueryV3Result wxPayOrderQueryV3Result = wxPayService.queryOrderV3(tradeNo, outTradeNo);
            PayPlatformStateBO payPlatformStateBO = new PayPlatformStateBO();
            payPlatformStateBO.setTradeState(wxPayOrderQueryV3Result.getTradeState());
            payPlatformStateBO.setTradeNo(wxPayOrderQueryV3Result.getTransactionId());
            payPlatformStateBO.setOutTradeNo(wxPayOrderQueryV3Result.getOutTradeNo());
            return payPlatformStateBO;
        } catch (WxPayException e) {
            throw new GlobalException(e.getReturnMsg());
        }
    }

    /**
     * H5支付
     *
     * @param orderVO    订单信息
     * @param openid     openid
     * @param clientType 支付类型
     * @return Object
     */
    private String htmlPay(OrderVO orderVO, String openid, String clientType) {
        try {
            // 根据请求头中的 clientType 获取对应的 WxPayService
            WxPayService wxPayService = wxPayServiceFactory.getWxPayV3Service(clientType);
            WxPayUnifiedOrderV3Request wechat = getUnifiedOrderV3Request(orderVO.getPaySn(), orderVO.getUnpaidAmount(), getPayNotifyUrl("wechat"), openid, wxPayService);
            WxPayUnifiedOrderV3Request.SceneInfo sceneInfo = new WxPayUnifiedOrderV3Request.SceneInfo();
            sceneInfo.setPayerClientIp(IpUtils.getIpAddr());
            WxPayUnifiedOrderV3Request.H5Info h5Info = new WxPayUnifiedOrderV3Request.H5Info();
            h5Info.setType("Wap");
            sceneInfo.setH5Info(h5Info);
            wechat.setSceneInfo(sceneInfo);
            return wxPayService.createOrderV3(TradeTypeEnum.H5, wechat);
        } catch (WxPayException e) {
            throw new GlobalException(e.getMessage());
        }
    }

    /**
     * App支付
     *
     * @param orderVO    订单信息
     * @param openid     openid
     * @param clientType 支付类型
     * @return Object
     */
    private PayCreateVO.PayInfo appPay(OrderVO orderVO, String openid, String clientType) {
        try {
            // 根据请求头中的 ClientType 获取对应的 WxPayService
            WxPayService wxPayService = wxPayServiceFactory.getWxPayV3Service(clientType);
            WxPayUnifiedOrderV3Result.AppResult appResult = wxPayService.createOrderV3(TradeTypeEnum.APP, getUnifiedOrderV3Request(orderVO.getPaySn(), orderVO.getUnpaidAmount(), getPayNotifyUrl("wechat"), openid, wxPayService));
            PayCreateVO.PayInfo payInfo = new PayCreateVO.PayInfo();
            payInfo.setAppId(appResult.getAppid());
            payInfo.setPartnerId(appResult.getPartnerId());
            payInfo.setPrepayId(appResult.getPrepayId());
            payInfo.setPackageValue(appResult.getPackageValue());
            payInfo.setNonceStr(appResult.getNoncestr());
            payInfo.setTimeStamp(appResult.getTimestamp());
            payInfo.setSign(appResult.getSign());

            return payInfo;
        } catch (WxPayException e) {
            throw new GlobalException(e.getMessage());
        }
    }

    /**
     * 小程序支付
     *
     * @param orderVO    订单信息
     * @param openid     openid
     * @param clientType 支付类型
     * @return Object
     */
    private PayCreateVO.PayInfo miniPay(OrderVO orderVO, String openid, String clientType) {
        try {
            WxPayService wxPayService = wxPayServiceFactory.getWxPayV3Service(clientType);
            WxPayUnifiedOrderV3Result.JsapiResult result = wxPayService.createOrderV3(TradeTypeEnum.JSAPI, getUnifiedOrderV3Request(orderVO.getPaySn(), orderVO.getUnpaidAmount(), getPayNotifyUrl("wechat"), openid, wxPayService));
            PayCreateVO.PayInfo payInfo = new PayCreateVO.PayInfo();
            BeanUtils.copyProperties(result, payInfo);
            return payInfo;
        } catch (WxPayException e) {
            throw new GlobalException(e.getMessage());
        }
    }

    /**
     * native支付
     *
     * @param orderVO    订单信息
     * @param openid     openid
     * @param clientType 支付类型
     * @return Object
     */
    private String nativePay(OrderVO orderVO, String openid, String clientType) {
        try {
            WxPayService wxPayService = wxPayServiceFactory.getWxPayV3Service(clientType);
            return wxPayService.createOrderV3(TradeTypeEnum.NATIVE, getUnifiedOrderV3Request(orderVO.getPaySn(), orderVO.getUnpaidAmount(), getPayNotifyUrl("wechat"), openid, wxPayService));
        } catch (WxPayException e) {
            throw new GlobalException(e.getMessage());
        }
    }

    /**
     * JSAPI支付
     *
     * @param orderVO    订单信息
     * @param openid     openid
     * @param clientType 支付类型
     * @return Object
     */
    private PayCreateVO.PayInfo jsApiPay(OrderVO orderVO, String openid, String clientType) {
        try {
            WxPayService wxPayService = wxPayServiceFactory.getWxPayV3Service(clientType);
            WxPayUnifiedOrderV3Result.JsapiResult result = wxPayService.createOrderV3(TradeTypeEnum.JSAPI, getUnifiedOrderV3Request(orderVO.getPaySn(), orderVO.getUnpaidAmount(), getPayNotifyUrl("wechat"), openid, wxPayService));
            PayCreateVO.PayInfo payInfo = new PayCreateVO.PayInfo();
            BeanUtils.copyProperties(result, payInfo);
            return payInfo;
        } catch (WxPayException e) {
            throw new GlobalException(e.getMessage());
        }
    }


    private PayCreateVO.PayInfo paymentCodePay(OrderVO orderVO, String clientType) {
        try {
            WxPayService wxPayService = wxPayServiceFactory.getWxPayV3Service(clientType);
            WxPayMicropayRequest codePayRequest = getWxPayMicropayRequest(orderVO.getPaySn(), orderVO.getUnpaidAmount(), orderVO.getAuthCode(), wxPayService);
            WxPayMicropayResult codePayResult = wxPayService.micropay(codePayRequest);
            // TODO 微信付款码支付没有异步通知逻辑处理, 需要后端任务调度或者通过前端轮训特殊处理(后端提供一个查询支付号接口)
            //https://pay.weixin.qq.com/doc/v2/merchant/4011936672
            //https://pay.weixin.qq.com/doc/v2/merchant/4011937125
            PayCreateVO.PayInfo payInfo = new PayCreateVO.PayInfo();
            payInfo.setCodePaySuccess(false);
            BeanUtils.copyProperties(codePayResult, payInfo);
            return payInfo;
        } catch (WxPayException e) {
            String errCode = e.getErrCode();
            if ("USERPAYING".equals(errCode)) {
                //用户支付中
                PayCreateVO.PayInfo payInfo = new PayCreateVO.PayInfo();
                payInfo.setCodePaySuccess(false);
                payInfo.setUserPaying(true);
                return payInfo;
            }
            var errorMeg = Optional.ofNullable(e.getErrCodeDes())
                                   .orElse(Optional.ofNullable(e.getReturnMsg())
                                                   .orElse(e.getCustomErrorMsg()));
            if (errorMeg.indexOf("auth_code") >= 0) {
                throw new GlobalException("请输入正确的付款码");
            }
            throw new GlobalException(errorMeg);
        }
    }
}
