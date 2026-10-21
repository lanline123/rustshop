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
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lkl.laop.sdk.Config2;
import com.lkl.laop.sdk.LKLSDK;
import com.lkl.laop.sdk.request.V3CcssCounterOrderSpecialCreateRequest;
import com.lkl.laop.sdk.request.V3LabsQueryTradequeryRequest;
import com.lkl.laop.sdk.request.V3LabsRelationRefundRequest;
import com.lkl.laop.sdk.request.V3LabsTransMicropayRequest;
import com.lkl.laop.sdk.request.V3LabsTransPreorderRequest;
import com.lkl.laop.sdk.request.model.V3LabsTradeLocationInfo;
import com.lkl.laop.sdk.request.model.V3LabsTradePreorderAlipayBus;
import com.lkl.laop.sdk.request.model.V3LabsTradePreorderWechatBus;
import com.tigshop.bean.bo.PayPlatformStateBO;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.finance.Paylog;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.bean.vo.order.pay.PayCreateVO;
import com.tigshop.bean.vo.order.pay.PayNotifyVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.finance.PaylogService;
import com.tigshop.service.pay.PayService;
import com.tigshop.service.setting.ConfigService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * 拉卡拉支付服务实现类
 *
 * <p>
 * 一期定位：平台统一收款 + 退款，不涉及分账 / 多商户结算。基于拉卡拉开放平台官方 Java SDK（1.0.8）。
 * </p>
 *
 * <p>覆盖终端（收银台平铺后按 payCode 拆钱包：lakalaWechat / lakalaAlipay）：</p>
 * <ul>
 *   <li>微信 · 公众号 JSAPI / 小程序 / APP：主扫预下单（V3LabsTransPreorder，accountType=WECHAT）</li>
 *   <li>微信 · PC / H5：无直连主扫（41 NATIVE 不支持微信），走聚合收银台（V3CcssCounterOrderSpecialCreate）返回 counter_url</li>
 *   <li>支付宝 · PC / H5 / APP：直连预下单（V3LabsTransPreorder，accountType=ALIPAY，PC=41 主扫 / H5=81）</li>
 *   <li>被扫付款码：V3LabsTransMicropay（同步返回结果）</li>
 * </ul>
 *
 * <p>
 * SDK 采用全局静态初始化（LKLSDK.init）。由于本系统仅拉卡拉使用该 SDK，
 * 因此在配置变化时重新 init，实现 Admin 改配置立即生效（热更新）。
 * </p>
 *
 * @author Tigshop团队
 * @since 2026/07
 * @see <a href="https://o.lakala.com/#/home/document">拉卡拉开放平台文档</a>
 */
@Service("LakalaService")
@Slf4j
public class LakalaServiceImpl extends PayBaseServiceImpl implements PayService {

    /** 拉卡拉生产网关 */
    private static final String DEFAULT_GATEWAY = "https://s2.lakala.com";
    /** 拉卡拉 v3 成功返回码 */
    private static final String SUCCESS_CODE = "000000";
    /** 微信账户类型 */
    private static final String ACCOUNT_WECHAT = "WECHAT";
    /** 微信交易类型（公众号 / 小程序 / APP 主扫预下单） */
    private static final String TRANS_TYPE_WECHAT = "51";
    /** 支付宝账户类型 */
    private static final String ACCOUNT_ALIPAY = "ALIPAY";
    /** 支付宝交易类型：41 NATIVE 主扫（PC 出二维码，支付宝支持） */
    private static final String TRANS_TYPE_ALIPAY_NATIVE = "41";
    /** 支付宝交易类型：81 支付宝 H5（仅 labs 接口支持） */
    private static final String TRANS_TYPE_ALIPAY_H5 = "81";

    private final ConfigService configService;
    private final PaylogService paylogService;
    private final HttpServletRequest httpServletRequest;

    /** 当前生效的配置指纹，用于判定是否需要重新 init（热更新） */
    private volatile String currentConfigFingerprint;

    @Autowired
    public LakalaServiceImpl(ConfigService configService,
                             PaylogService paylogService,
                             HttpServletRequest httpServletRequest) {
        this.configService = configService;
        this.paylogService = paylogService;
        this.httpServletRequest = httpServletRequest;
    }

    /**
     * 懒加载 / 热更新 SDK 初始化。配置齐全时用字符串证书 init；配置缺失返回 false 由业务方法抛错。
     */
    private boolean ensureInit() {
        String appId = configService.getConfigVal(SettingsEnum.LAKALA_APP_ID);
        String serialNo = configService.getConfigVal(SettingsEnum.LAKALA_SERIAL_NO);
        String priKey = configService.getConfigVal(SettingsEnum.LAKALA_MERCHANT_PRIVATE_KEY);
        String lklCer = configService.getConfigVal(SettingsEnum.LAKALA_PLATFORM_CERT);
        String sm4Key = configService.getConfigVal(SettingsEnum.LAKALA_SM4_KEY);
        String gatewayUrl = configService.getConfigVal(SettingsEnum.LAKALA_GATEWAY_URL);
        if (StrUtil.isBlank(gatewayUrl)) {
            gatewayUrl = DEFAULT_GATEWAY;
        }
        if (StrUtil.isBlank(appId) || StrUtil.isBlank(serialNo)
                || StrUtil.isBlank(priKey) || StrUtil.isBlank(lklCer)) {
            return false;
        }
        String fingerprint = String.join("|", appId, serialNo,
                String.valueOf(priKey.hashCode()), String.valueOf(lklCer.hashCode()),
                String.valueOf(sm4Key == null ? "" : sm4Key.hashCode()), gatewayUrl);
        if (fingerprint.equals(currentConfigFingerprint)) {
            return true;
        }
        synchronized (this) {
            if (fingerprint.equals(currentConfigFingerprint)) {
                return true;
            }
            Config2 config = new Config2();
            config.setAppId(appId);
            config.setSerialNo(serialNo);
            config.setPriKey(priKey);
            config.setLklCer(lklCer);
            config.setLklNotifyCer(lklCer);
            config.setServerUrl(gatewayUrl);
            if (StrUtil.isNotBlank(sm4Key)) {
                config.setSm4Key(sm4Key);
            }
            LKLSDK.init(config);
            currentConfigFingerprint = fingerprint;
            log.info("拉卡拉 SDK 初始化完成，gateway={}, appId={}", gatewayUrl, mask(appId));
        }
        return true;
    }

    @Override
    public PayCreateVO.PayInfo pay(OrderVO orderVO, String openid, String clientType) {
        if (!ensureInit()) {
            throw new GlobalException("拉卡拉支付未配置或配置不完整");
        }
        // 收银台平铺后按钱包拆码：先按 paylog.payCode 判定钱包（lakalaAlipay -> 支付宝，其余 -> 微信）
        String wallet = walletByPaySn(orderVO.getPaySn());
        if ("alipay".equals(wallet)) {
            return alipayPreorder(orderVO, clientType);
        }
        // 微信钱包：拉卡拉微信无 PC/H5 直连主扫（41 NATIVE 不支持微信），pc/h5 仍走聚合收银台
        return switch (clientType == null ? "" : clientType) {
            case "wechat" -> wechatPreorder(orderVO, openid,
                    configService.getConfigVal(SettingsEnum.LAKALA_WX_SUB_APPID));
            case "miniProgram" -> wechatPreorder(orderVO, openid,
                    configService.getConfigVal(SettingsEnum.LAKALA_WX_MINI_APPID));
            case "app" -> wechatPreorder(orderVO, openid, null);
            case "bar-code" -> micropay(orderVO);
            default -> counterPay(orderVO, clientType);
        };
    }

    /** 根据 paySn 读取 paylog.payCode 判定钱包（lakalaAlipay -> alipay，其余 -> wechat） */
    private String walletByPaySn(String paySn) {
        Paylog paylog = paylogService.getOne(new LambdaQueryWrapper<Paylog>().eq(Paylog::getPaySn, paySn));
        String payCode = paylog == null ? null : paylog.getPayCode();
        return payCode != null && payCode.toLowerCase().contains("alipay") ? "alipay" : "wechat";
    }

    /**
     * PC / H5：聚合收银台下单，返回 counter_url 供前端跳转。
     */
    private PayCreateVO.PayInfo counterPay(OrderVO orderVO, String clientType) {
        V3CcssCounterOrderSpecialCreateRequest req = new V3CcssCounterOrderSpecialCreateRequest();
        req.setOutOrderNo(orderVO.getPaySn());
        req.setMerchantNo(configService.getConfigVal(SettingsEnum.LAKALA_MERCHANT_NO));
        req.setTermNo(configService.getConfigVal(SettingsEnum.LAKALA_TERM_NO));
        req.setTotalAmount(toFen(orderVO.getUnpaidAmount()));
        req.setNotifyUrl(getPayNotifyUrl("lakala"));
        req.setSupportRefund(1);
        req.setSupportRepeatPay(0);
        req.setOrderInfo("Tigshop Order " + orderVO.getOrderSn());

        JSONObject respData = execute(req);
        String counterUrl = respData.getString("counter_url");
        if (StrUtil.isBlank(counterUrl)) {
            throw new GlobalException("拉卡拉收银台创建失败：counter_url 为空");
        }
        storeTransactionId(orderVO.getPaySn(), respData.getString("pay_order_no"));
        PayCreateVO.PayInfo payInfo = new PayCreateVO.PayInfo();
        payInfo.setUrl(counterUrl);
        return payInfo;
    }

    /**
     * 微信主扫预下单（公众号 JSAPI / 小程序 / APP）。
     */
    private PayCreateVO.PayInfo wechatPreorder(OrderVO orderVO, String openid, String subAppid) {
        V3LabsTransPreorderRequest req = new V3LabsTransPreorderRequest();
        req.setMerchantNo(configService.getConfigVal(SettingsEnum.LAKALA_MERCHANT_NO));
        req.setTermNo(configService.getConfigVal(SettingsEnum.LAKALA_TERM_NO));
        req.setOutTradeNo(orderVO.getPaySn());
        req.setAccountType(ACCOUNT_WECHAT);
        req.setTransType(TRANS_TYPE_WECHAT);
        req.setTotalAmount(String.valueOf(toFen(orderVO.getUnpaidAmount())));
        req.setNotifyUrl(getPayNotifyUrl("lakala"));
        req.setSubject("Tigshop Order " + orderVO.getOrderSn());
        req.setLocationInfo(new V3LabsTradeLocationInfo(clientIp()));

        V3LabsTradePreorderWechatBus wechatBus = new V3LabsTradePreorderWechatBus();
        if (StrUtil.isNotBlank(subAppid)) {
            wechatBus.setSubAppid(subAppid);
        }
        if (StrUtil.isNotBlank(openid)) {
            wechatBus.setUserId(openid);
        }
        req.setAccBusiFields(wechatBus);

        JSONObject respData = execute(req);
        storeTransactionId(orderVO.getPaySn(), firstNonBlank(respData.getString("trade_no"),
                respData.getString("log_no")));

        // acc_resp_fields 内含微信调起参数（pay_info）。不同终端结构略有差异，
        // 这里原样透传给前端，并尽力抽取常见的微信 JSAPI 字段，最终以联调为准。
        PayCreateVO.PayInfo payInfo = new PayCreateVO.PayInfo();
        String accRespFields = respData.getString("acc_resp_fields");
        payInfo.setOrderInfo(StrUtil.isNotBlank(accRespFields) ? accRespFields : respData.toJSONString());
        fillWechatInvokeParams(payInfo, accRespFields);
        return payInfo;
    }

    /**
     * 支付宝直连预下单（拉卡拉统一预下单，accountType=ALIPAY）。
     * PC 用 41 NATIVE 主扫出二维码；H5 用 81 支付宝 H5；其余默认 41。
     */
    private PayCreateVO.PayInfo alipayPreorder(OrderVO orderVO, String clientType) {
        V3LabsTransPreorderRequest req = new V3LabsTransPreorderRequest();
        req.setMerchantNo(configService.getConfigVal(SettingsEnum.LAKALA_MERCHANT_NO));
        req.setTermNo(configService.getConfigVal(SettingsEnum.LAKALA_TERM_NO));
        req.setOutTradeNo(orderVO.getPaySn());
        req.setAccountType(ACCOUNT_ALIPAY);
        req.setTransType("h5".equals(clientType) ? TRANS_TYPE_ALIPAY_H5 : TRANS_TYPE_ALIPAY_NATIVE);
        req.setTotalAmount(String.valueOf(toFen(orderVO.getUnpaidAmount())));
        req.setNotifyUrl(getPayNotifyUrl("lakala"));
        req.setSubject("Tigshop Order " + orderVO.getOrderSn());
        req.setLocationInfo(new V3LabsTradeLocationInfo(clientIp()));
        req.setAccBusiFields(new V3LabsTradePreorderAlipayBus());

        JSONObject respData = execute(req);
        storeTransactionId(orderVO.getPaySn(), firstNonBlank(respData.getString("trade_no"),
                respData.getString("log_no")));

        // acc_resp_fields 内含支付宝调起信息（主扫二维码串 / H5 跳转串），结构因终端而异，
        // 原样透传给前端，并尽力抽取常见字段，最终以联调为准。
        PayCreateVO.PayInfo payInfo = new PayCreateVO.PayInfo();
        payInfo.setProvider("alipay");
        String accRespFields = respData.getString("acc_resp_fields");
        payInfo.setOrderInfo(StrUtil.isNotBlank(accRespFields) ? accRespFields : respData.toJSONString());
        fillAlipayInvokeParams(payInfo, accRespFields);
        return payInfo;
    }

    /**
     * 尽力从 acc_resp_fields 抽取支付宝调起信息（二维码 / 跳转串）填入 PayInfo（最终以联调为准）。
     */
    private void fillAlipayInvokeParams(PayCreateVO.PayInfo payInfo, String accRespFields) {
        if (StrUtil.isBlank(accRespFields)) {
            return;
        }
        try {
            JSONObject acc = JSON.parseObject(accRespFields);
            JSONObject payInfoJson = acc.getJSONObject("pay_info");
            JSONObject src = payInfoJson != null ? payInfoJson : acc;
            String qr = firstNonBlank(src.getString("qr_code"), src.getString("code_url"));
            if (StrUtil.isNotBlank(qr)) {
                payInfo.setCodeUrl(qr);
            }
            String url = firstNonBlank(src.getString("pay_url"), src.getString("h5_url"));
            if (StrUtil.isNotBlank(url)) {
                payInfo.setUrl(url);
            }
        } catch (Exception e) {
            log.warn("拉卡拉支付宝 acc_resp_fields 解析失败，将原样透传给前端", e);
        }
    }

    /**
     * 被扫付款码支付（同步返回结果）。
     */
    private PayCreateVO.PayInfo micropay(OrderVO orderVO) {
        String authCode = orderVO.getAuthCode();
        if (StrUtil.isBlank(authCode)) {
            throw new GlobalException("请输入正确的付款码");
        }
        V3LabsTransMicropayRequest req = new V3LabsTransMicropayRequest();
        req.setMerchantNo(configService.getConfigVal(SettingsEnum.LAKALA_MERCHANT_NO));
        req.setTermNo(configService.getConfigVal(SettingsEnum.LAKALA_TERM_NO));
        req.setOutTradeNo(orderVO.getPaySn());
        req.setAuthCode(authCode);
        req.setTotalAmount(String.valueOf(toFen(orderVO.getUnpaidAmount())));
        req.setNotifyUrl(getPayNotifyUrl("lakala"));
        req.setSubject("Tigshop Order " + orderVO.getOrderSn());
        req.setLocationInfo(new V3LabsTradeLocationInfo(clientIp()));

        JSONObject respData = execute(req);
        String tradeState = respData.getString("trade_state");
        PayCreateVO.PayInfo payInfo = new PayCreateVO.PayInfo();
        if ("SUCCESS".equalsIgnoreCase(tradeState)) {
            String tradeNo = firstNonBlank(respData.getString("trade_no"), respData.getString("log_no"));
            if (!checkOrderPayStatus(orderVO.getPaySn())) {
                paySuccess(orderVO.getPaySn(), tradeNo);
            }
            payInfo.setCodePaySuccess(true);
        } else {
            // 付款码待用户确认 / 处理中，前端需轮询订单支付状态
            payInfo.setCodePaySuccess(false);
            payInfo.setUserPaying(true);
        }
        return payInfo;
    }

    @Override
    public PayNotifyVO payRefund(String outTradeNo, String refundNo, BigDecimal refundAmount,
                                 BigDecimal totalAmount, String clientType, String subMchId) {
        if (!ensureInit()) {
            return new PayNotifyVO("fail", "拉卡拉支付未配置");
        }
        try {
            Paylog paylog = paylogService.getOne(new LambdaQueryWrapper<Paylog>()
                    .eq(Paylog::getPaySn, outTradeNo));
            V3LabsRelationRefundRequest req = new V3LabsRelationRefundRequest();
            req.setMerchantNo(configService.getConfigVal(SettingsEnum.LAKALA_MERCHANT_NO));
            req.setTermNo(configService.getConfigVal(SettingsEnum.LAKALA_TERM_NO));
            req.setOutTradeNo(refundNo);
            req.setOriginOutTradeNo(outTradeNo);
            if (paylog != null && StrUtil.isNotBlank(paylog.getTransactionId())) {
                req.setOriginTradeNo(paylog.getTransactionId());
            }
            req.setRefundAmount(String.valueOf(toFen(refundAmount)));
            req.setRefundReason("Tigshop refund " + refundNo);
            req.setLocationInfo(new V3LabsTradeLocationInfo(clientIp()));

            String response = LKLSDK.httpPost(req);
            log.info("拉卡拉退款响应，refund_sn: {}, response: {}", refundNo, response);
            JSONObject json = JSON.parseObject(response);
            if (json != null && SUCCESS_CODE.equals(json.getString("code"))) {
                // 拉卡拉退款为同步受理，成功后直接回写退款成功
                refundSuccess(refundNo);
                return new PayNotifyVO("SUCCESS", "退款成功");
            }
            String msg = json == null ? "unknown" : json.getString("msg");
            log.warn("拉卡拉退款失败，refund_sn: {}, msg: {}", refundNo, msg);
            return new PayNotifyVO("fail", "退款失败：" + msg);
        } catch (Exception e) {
            log.error("拉卡拉退款异常，refund_sn: {}", refundNo, e);
            return new PayNotifyVO("fail", "退款异常：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public String payNotify(Map<String, Object> map) {
        String body = verifyNotify(map);
        if (body == null) {
            return "FAIL";
        }
        try {
            JSONObject json = JSON.parseObject(body);
            JSONObject data = json.getJSONObject("resp_data");
            if (data == null) {
                data = json;
            }
            // 退款通知（拉卡拉退款以同步为主，此处兼容异步退款推送）
            if (StrUtil.isNotBlank(data.getString("refund_amount"))
                    || StrUtil.isNotBlank(data.getString("origin_out_trade_no"))) {
                return refundNotify(map);
            }
            String tradeState = data.getString("trade_state");
            if (tradeState != null && !"SUCCESS".equalsIgnoreCase(tradeState)) {
                log.info("拉卡拉支付通知非成功状态，body: {}", body);
                return "FAIL";
            }
            String paySn = firstNonBlank(data.getString("out_trade_no"), data.getString("out_order_no"));
            if (StrUtil.isBlank(paySn)) {
                log.error("拉卡拉支付通知缺失 out_trade_no，body: {}", body);
                return "FAIL";
            }
            String tradeNo = firstNonBlank(data.getString("trade_no"), data.getString("log_no"));
            if (!checkOrderPayStatus(paySn)) {
                paySuccess(paySn, tradeNo);
                log.info("拉卡拉支付成功处理完成，pay_sn: {}, trade_no: {}", paySn, tradeNo);
            } else {
                log.info("拉卡拉订单已支付，跳过，pay_sn: {}", paySn);
            }
            return "SUCCESS";
        } catch (Exception e) {
            log.error("拉卡拉支付通知处理异常", e);
            return "FAIL";
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String refundNotify(Map<String, Object> map) {
        String body = verifyNotify(map);
        if (body == null) {
            return "FAIL";
        }
        try {
            JSONObject json = JSON.parseObject(body);
            JSONObject data = json.getJSONObject("resp_data");
            if (data == null) {
                data = json;
            }
            String refundState = firstNonBlank(data.getString("refund_state"), data.getString("trade_state"));
            if (refundState != null && !"SUCCESS".equalsIgnoreCase(refundState)) {
                log.info("拉卡拉退款通知非成功状态，body: {}", body);
                return "FAIL";
            }
            String refundSn = data.getString("out_trade_no");
            if (StrUtil.isBlank(refundSn)) {
                log.error("拉卡拉退款通知缺失 out_trade_no，body: {}", body);
                return "FAIL";
            }
            refundSuccess(refundSn);
            log.info("拉卡拉退款成功处理完成，refund_sn: {}", refundSn);
            return "SUCCESS";
        } catch (Exception e) {
            log.error("拉卡拉退款通知处理异常", e);
            return "FAIL";
        }
    }

    @Override
    public PayPlatformStateBO payPlatformStatus(String outTradeNo, String tradeNo, String clientType) {
        if (!ensureInit()) {
            return null;
        }
        try {
            V3LabsQueryTradequeryRequest req = new V3LabsQueryTradequeryRequest();
            req.setMerchantNo(configService.getConfigVal(SettingsEnum.LAKALA_MERCHANT_NO));
            req.setTermNo(configService.getConfigVal(SettingsEnum.LAKALA_TERM_NO));
            req.setOutTradeNo(outTradeNo);
            if (StrUtil.isNotBlank(tradeNo)) {
                req.setTradeNo(tradeNo);
            }
            String response = LKLSDK.httpPost(req);
            JSONObject json = JSON.parseObject(response);
            if (json == null || !SUCCESS_CODE.equals(json.getString("code"))) {
                return null;
            }
            JSONObject data = json.getJSONObject("resp_data");
            if (data == null) {
                return null;
            }
            PayPlatformStateBO bo = new PayPlatformStateBO();
            bo.setOutTradeNo(outTradeNo);
            bo.setTradeNo(firstNonBlank(data.getString("trade_no"), data.getString("log_no")));
            bo.setTradeState(StrUtil.isBlank(data.getString("trade_state")) ? "UNKNOWN" : data.getString("trade_state"));
            return bo;
        } catch (Exception e) {
            log.error("拉卡拉查询支付状态异常，outTradeNo: {}", outTradeNo, e);
            return null;
        }
    }

    // ============ private helpers ============

    /**
     * 发起请求并校验返回码，返回 resp_data。
     */
    private JSONObject execute(com.lkl.laop.sdk.request.LklRequest req) {
        try {
            String response = LKLSDK.httpPost(req);
            log.info("拉卡拉请求响应：{}", response);
            JSONObject json = JSON.parseObject(response);
            if (json == null || !SUCCESS_CODE.equals(json.getString("code"))) {
                String msg = json == null ? "空响应" : json.getString("msg");
                throw new GlobalException("拉卡拉请求失败：" + msg);
            }
            JSONObject data = json.getJSONObject("resp_data");
            return data == null ? new JSONObject() : data;
        } catch (GlobalException e) {
            throw e;
        } catch (Exception e) {
            log.error("拉卡拉请求异常", e);
            throw new GlobalException("拉卡拉请求异常：" + e.getMessage());
        }
    }

    /**
     * 校验异步通知签名，返回验签通过的原始 body；失败返回 null。
     */
    private String verifyNotify(Map<String, Object> map) {
        if (map == null || map.isEmpty() || map.get("requestBody") == null) {
            return null;
        }
        if (!ensureInit()) {
            log.error("拉卡拉未配置，无法验签通知");
            return null;
        }
        String body = String.valueOf(map.get("requestBody"));
        String authorization = map.get("authorization") == null ? null : String.valueOf(map.get("authorization"));
        try {
            // SpringBoot3 使用 body + Authorization 验签（void，失败抛异常）
            LKLSDK.notificationHandle(body, authorization);
            return body;
        } catch (Exception e) {
            log.error("拉卡拉通知验签失败，body: {}", body, e);
            return null;
        }
    }

    /**
     * 尽力从 acc_resp_fields 抽取微信调起参数填入 PayInfo（最终以联调为准）。
     */
    private void fillWechatInvokeParams(PayCreateVO.PayInfo payInfo, String accRespFields) {
        if (StrUtil.isBlank(accRespFields)) {
            return;
        }
        try {
            JSONObject acc = JSON.parseObject(accRespFields);
            // 微信 JSAPI 调起参数常见嵌套在 pay_info 中
            JSONObject payInfoJson = acc.getJSONObject("pay_info");
            JSONObject src = payInfoJson != null ? payInfoJson : acc;
            payInfo.setAppId(firstNonBlank(src.getString("appId"), src.getString("app_id")));
            payInfo.setTimeStamp(firstNonBlank(src.getString("timeStamp"), src.getString("timestamp")));
            payInfo.setNonceStr(firstNonBlank(src.getString("nonceStr"), src.getString("nonce_str")));
            payInfo.setPackageValue(firstNonBlank(src.getString("package"), src.getString("packageValue")));
            payInfo.setSignType(firstNonBlank(src.getString("signType"), src.getString("sign_type")));
            payInfo.setPaySign(firstNonBlank(src.getString("paySign"), src.getString("pay_sign")));
            payInfo.setPrepayId(firstNonBlank(src.getString("prepayId"), src.getString("prepay_id")));
        } catch (Exception e) {
            log.warn("拉卡拉 acc_resp_fields 解析失败，将原样透传给前端", e);
        }
    }

    private void storeTransactionId(String paySn, String transactionId) {
        if (StrUtil.isBlank(transactionId)) {
            return;
        }
        try {
            paylogService.lambdaUpdate()
                    .eq(Paylog::getPaySn, paySn)
                    .set(Paylog::getTransactionId, transactionId)
                    .update();
        } catch (Exception ex) {
            log.warn("拉卡拉暂存 transactionId 到 paylog 失败，pay_sn: {}", paySn, ex);
        }
    }

    /** 元转分 */
    private long toFen(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
    }

    private String clientIp() {
        try {
            String ip = httpServletRequest.getRemoteAddr();
            return StrUtil.isBlank(ip) ? "127.0.0.1" : ip;
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }

    private String firstNonBlank(String a, String b) {
        return StrUtil.isNotBlank(a) ? a : b;
    }

    private String mask(String s) {
        if (s == null || s.length() <= 4) {
            return "****";
        }
        return s.substring(0, 4) + "****";
    }
}
