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
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.yeepay.yop.sdk.auth.credentials.PKICredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.YopPKICredentials;
import com.yeepay.yop.sdk.config.enums.CertStoreType;
import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.base.security.cert.parser.YopCertParserFactory;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.security.cert.YopCertCategory;
import com.yeepay.yop.sdk.service.common.YopClient;
import com.yeepay.yop.sdk.service.common.YopClientBuilder;
import com.yeepay.yop.sdk.service.common.request.YopRequest;
import com.yeepay.yop.sdk.service.common.response.YopResponse;
import com.yeepay.yop.sdk.utils.DigitalEnvelopeUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.PrivateKey;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 易宝支付（YOP 开放平台）聚合支付服务实现（微信 yeepayWechat / 支付宝 yeepayAlipay + PC 收银台）。
 *
 * <p>加验签/加解密全部走易宝官方 {@code yop-java-sdk}（RSA2048-SHA256，国际软算法 crypto-inter）。
 * 凭证不落 classpath 的 {@code yop_sdk_config_default.json}，而是从后台 DB 配置按请求程序化注入
 * （{@code YopRequestConfig#setCredentials}）；响应/异步通知的平台验签由 SDK 自动拉取并缓存易宝平台证书完成。</p>
 *
 * <p>两个 payCode（yeepayWechat/yeepayAlipay）与回调父级码 yeepay 都路由到本 bean。金额单位为
 * <b>元（两位小数字符串，不 ×100）</b>。</p>
 *
 * <p><b>联调待校准（以易宝沙箱实际文档核对）：</b>聚合下单/收银台/退款/查询各接口的具体请求字段名
 * （payWay/channel/scene/userNo/appId 等）、prePayTn 前端使用方式、异步通知报文字段与应答格式、
 * 沙箱网关地址（{@code YEEPAY_SERVER_ROOT} 需通过自定义 {@code YopSdkConfigProvider} 生效）。</p>
 *
 * @author Tigshop团队
 * @since 2026/08/06
 */
@Service("YeepayService")
@Slf4j
public class YeepayServiceImpl extends PayBaseServiceImpl implements PayService {

    /** JSAPI 聚合统一下单（微信/支付宝 H5、小程序、公众号） */
    private static final String PATH_AGGPAY_PREPAY = "/rest/v1.0/aggpay/pre-pay";
    /** PC / H5 收银台统一下单（返回 cashierUrl） */
    private static final String PATH_CASHIER_UNIFIED_ORDER = "/rest/v1.0/cashier/unified/order";
    /** 申请退款 */
    private static final String PATH_TRADE_REFUND = "/rest/v1.0/trade/refund";
    /** 查询订单 */
    private static final String PATH_TRADE_ORDER_QUERY = "/rest/v1.0/trade/order/query";
    /** YOP 安全需求：国际 RSA2048-SHA256 */
    private static final String SECURITY_REQ_RSA = "YOP-RSA2048-SHA256";

    /** 线程安全，单例复用 */
    private volatile YopClient yopClient;

    @Resource
    private ConfigService configService;
    @Resource
    private PaylogService paylogService;

    // ============ 配置 ============

    private String cfg(SettingsEnum key) {
        return configService.getConfigVal(key);
    }

    private String appKey() {
        return cfg(SettingsEnum.YEEPAY_APP_KEY);
    }

    private String parentMerchantNo() {
        return cfg(SettingsEnum.YEEPAY_PARENT_MERCHANT_NO);
    }

    private String merchantNo() {
        String m = cfg(SettingsEnum.YEEPAY_MERCHANT_NO);
        // 未单独配置收款商户时，回退发起方商户（单商户场景 parentMerchantNo == merchantNo）
        return StrUtil.isNotBlank(m) ? m : parentMerchantNo();
    }

    private void requireConfigured() {
        if (StrUtil.isBlank(appKey())
                || StrUtil.isBlank(parentMerchantNo())
                || StrUtil.isBlank(cfg(SettingsEnum.YEEPAY_ISV_PRIVATE_KEY))) {
            throw new GlobalException("易宝支付未配置完整（AppKey/商户号/商户私钥）");
        }
    }

    private YopClient client() {
        if (yopClient == null) {
            synchronized (this) {
                if (yopClient == null) {
                    yopClient = YopClientBuilder.builder().build();
                }
            }
        }
        return yopClient;
    }

    /** 把 DB 里的 RSA 私钥字符串解析为 SDK 可用的 PrivateKey（STRING 存储、RSA2048）。 */
    private PrivateKey isvPrivateKey() {
        String priKey = normalizeKey(cfg(SettingsEnum.YEEPAY_ISV_PRIVATE_KEY));
        if (StrUtil.isBlank(priKey)) {
            throw new GlobalException("易宝商户私钥未配置");
        }
        try {
            YopCertConfig certConfig = new YopCertConfig();
            certConfig.setCertType(CertTypeEnum.RSA2048);
            certConfig.setValue(priKey);
            certConfig.setStoreType(CertStoreType.STRING);
            return (PrivateKey) YopCertParserFactory
                    .getCertParser(YopCertCategory.PRIVATE, CertTypeEnum.RSA2048)
                    .parse(certConfig);
        } catch (Exception e) {
            log.error("易宝商户私钥解析失败，疑似私钥格式或内容不正确：{}", e.getMessage());
            throw new GlobalException("易宝商户私钥不正确，请检查后台配置的商户RSA私钥（需 PKCS#8 Base64）");
        }
    }

    /** 剥离 PEM 头尾与空白，兼容运营直接粘贴 PEM 文件内容。 */
    private String normalizeKey(String raw) {
        if (StrUtil.isBlank(raw)) {
            return raw;
        }
        return raw.replaceAll("-----BEGIN[^-]*-----", "")
                .replaceAll("-----END[^-]*-----", "")
                .replaceAll("\\s", "");
    }

    // ============ 请求封装 ============

    /** 构造带凭证的 YopRequest（appKey + RSA2048 私钥从 DB 注入）。 */
    private YopRequest newRequest(String apiUri, String httpMethod) {
        YopRequest request = new YopRequest(apiUri, httpMethod);
        request.getRequestConfig().setAppKey(appKey());
        request.getRequestConfig().setSecurityReq(SECURITY_REQ_RSA);
        request.getRequestConfig().setCredentials(
                new YopPKICredentials(appKey(), new PKICredentialsItem(isvPrivateKey(), CertTypeEnum.RSA2048)));
        return request;
    }

    /** 执行请求，返回业务结果 JSON（已由 SDK 验签/解密）。 */
    private JSONObject execute(YopRequest request) {
        try {
            YopResponse response = client().request(request);
            String result = response == null ? null : response.getStringResult();
            if (StrUtil.isBlank(result)) {
                throw new GlobalException("易宝网关无响应");
            }
            return JSONUtil.parseObj(result);
        } catch (GlobalException e) {
            throw e;
        } catch (Exception e) {
            log.error("易宝请求异常 api={}", request.getApiUri(), e);
            throw new GlobalException("易宝网关通信失败：" + e.getMessage());
        }
    }

    /** 业务受理成功码：OPR00000（易宝受理成功，具体结果看 status）。 */
    private boolean isAccepted(JSONObject resp) {
        String code = resp.getStr("code");
        return "OPR00000".equals(code);
    }

    // ============ 钱包/单号 ============

    private String walletByPaySn(String paySn) {
        Paylog paylog = paylogService.getOne(new LambdaQueryWrapper<Paylog>().eq(Paylog::getPaySn, paySn));
        String payCode = paylog == null ? null : paylog.getPayCode();
        return payCode != null && payCode.toLowerCase().contains("alipay") ? "alipay" : "wechat";
    }

    /** WECHAT / ALIPAY 渠道码。 */
    private String channelOf(String wallet) {
        return "alipay".equals(wallet) ? "ALIPAY" : "WECHAT";
    }

    private String safeGoodsName(OrderVO orderVO) {
        String name = "Tigshop Order " + orderVO.getOrderSn();
        return name.length() > 85 ? name.substring(0, 85) : name;
    }

    /** reqSeqId 全局唯一，附 uuid 后缀防重复下单被拒；回调按 "-" 前段还原 paySn。 */
    private String buildReqSeqId(String paySn) {
        return paySn + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    // ============ PayService ============

    @Override
    public PayCreateVO.PayInfo pay(OrderVO orderVO, String openid, String clientType) {
        requireConfigured();
        String orderId = buildReqSeqId(orderVO.getPaySn());
        String orderAmount = orderVO.getUnpaidAmount().setScale(2, RoundingMode.HALF_UP).toPlainString();

        // PC 端：走收银台统一下单，返回收银台 URL 供跳转/扫码；移动端：走 JSAPI 聚合下单，返回 prePayTn 调起
        boolean pc = "pc".equals(clientType);
        JSONObject resp;
        if (pc) {
            YopRequest request = newRequest(PATH_CASHIER_UNIFIED_ORDER, "POST");
            request.addParameter("parentMerchantNo", parentMerchantNo());
            request.addParameter("merchantNo", merchantNo());
            request.addParameter("orderId", orderId);
            request.addParameter("orderAmount", orderAmount);
            request.addParameter("goodsName", safeGoodsName(orderVO));
            request.addParameter("notifyUrl", getPayNotifyUrl("yeepay"));
            request.addParameter("returnUrl", getReturnUrl(orderVO.getOrderId()));
            resp = execute(request);
        } else {
            String wallet = walletByPaySn(orderVO.getPaySn());
            YopRequest request = newRequest(PATH_AGGPAY_PREPAY, "POST");
            request.addParameter("parentMerchantNo", parentMerchantNo());
            request.addParameter("merchantNo", merchantNo());
            request.addParameter("orderId", orderId);
            request.addParameter("orderAmount", orderAmount);
            request.addParameter("goodsName", safeGoodsName(orderVO));
            request.addParameter("notifyUrl", getPayNotifyUrl("yeepay"));
            // 渠道 + 场景 + JSAPI 拉起所需 openid/appId（字段名以易宝沙箱文档核对）
            request.addParameter("channel", channelOf(wallet));
            request.addParameter("scene", "ONLINE");
            if (StrUtil.isNotBlank(openid)) {
                request.addParameter("userNo", openid);
                request.addParameter("userType", "USER_ID");
                String appId = "miniProgram".equals(clientType)
                        ? cfg(SettingsEnum.WECHAT_MINI_PROGRAM_APP_ID)
                        : cfg(SettingsEnum.WECHAT_APP_ID);
                if ("wechat".equals(wallet) && StrUtil.isNotBlank(appId)) {
                    request.addParameter("appId", appId);
                }
            }
            resp = execute(request);
        }

        if (!isAccepted(resp)) {
            log.error("易宝下单失败，pay_sn:{}, resp:{}", orderVO.getPaySn(), resp);
            throw new GlobalException("易宝下单失败：" + resp.getStr("message"));
        }

        String uniqueOrderNo = resp.getStr("uniqueOrderNo");
        // 暂存 reqSeqId(orderId) 到 transactionId、易宝订单号到 notifyData，供退款/查询定位原交易
        try {
            JSONObject meta = new JSONObject();
            if (StrUtil.isNotBlank(uniqueOrderNo)) {
                meta.set("uniqueOrderNo", uniqueOrderNo);
            }
            paylogService.lambdaUpdate()
                    .eq(Paylog::getPaySn, orderVO.getPaySn())
                    .set(Paylog::getTransactionId, orderId)
                    .set(Paylog::getNotifyData, meta.toString())
                    .update();
        } catch (Exception ex) {
            log.warn("易宝暂存 orderId/uniqueOrderNo 到 paylog 失败，pay_sn:{}", orderVO.getPaySn(), ex);
        }

        PayCreateVO.PayInfo payInfo = new PayCreateVO.PayInfo();
        if (pc) {
            // 收银台链接：前端跳转/渲染二维码
            String cashierUrl = resp.getStr("cashierUrl");
            payInfo.setUrl(cashierUrl);
            payInfo.setCodeUrl(cashierUrl);
            return payInfo;
        }
        // 移动端：prePayTn 为调起微信/支付宝的凭证串
        String prePayTn = resp.getStr("prePayTn");
        payInfo.setProvider("alipay".equals(walletByPaySn(orderVO.getPaySn())) ? "alipay" : "wxpay");
        payInfo.setOrderInfo(prePayTn);
        return payInfo;
    }

    @Override
    public PayNotifyVO payRefund(String outTradeNo, String refundNo, BigDecimal refundAmount,
                                 BigDecimal totalAmount, String clientType, String subMchId) {
        try {
            requireConfigured();
            Paylog paylog = paylogService.getOne(new LambdaQueryWrapper<Paylog>().eq(Paylog::getPaySn, outTradeNo));
            String origOrderId = paylog != null && StrUtil.isNotBlank(paylog.getTransactionId())
                    ? paylog.getTransactionId() : outTradeNo;
            String uniqueOrderNo = yeepayMeta(paylog).getStr("uniqueOrderNo");

            YopRequest request = newRequest(PATH_TRADE_REFUND, "POST");
            request.addParameter("parentMerchantNo", parentMerchantNo());
            request.addParameter("merchantNo", merchantNo());
            request.addParameter("orderId", origOrderId);
            if (StrUtil.isNotBlank(uniqueOrderNo)) {
                request.addParameter("uniqueOrderNo", uniqueOrderNo);
            }
            request.addParameter("refundRequestId", refundNo);
            request.addParameter("refundAmount", refundAmount.setScale(2, RoundingMode.HALF_UP).toPlainString());
            request.addParameter("notifyUrl", getRefundNotifyUrl("yeepay"));

            JSONObject resp = execute(request);
            if (!isAccepted(resp)) {
                log.error("易宝退款失败，refund_sn:{}, resp:{}", refundNo, resp);
                return new PayNotifyVO("FAIL", "退款失败：" + resp.getStr("message"));
            }
            // 受理成功后看 status：SUCCESS 立即成功；PROCESSING 等异步退款回调
            String status = resp.getStr("status");
            if ("SUCCESS".equalsIgnoreCase(status)) {
                return new PayNotifyVO("SUCCESS", "退款成功");
            }
            log.info("易宝退款受理处理中，refund_sn:{}, status:{}", refundNo, status);
            return new PayNotifyVO("PROCESSING", "退款处理中");
        } catch (Exception e) {
            log.error("易宝退款异常，refund_sn:{}", refundNo, e);
            return new PayNotifyVO("FAIL", "退款异常：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String payNotify(Map<String, Object> map) {
        JSONObject data = decryptNotify(map);
        if (data == null) {
            return "FAILURE";
        }
        try {
            String status = data.getStr("status");
            if (!"SUCCESS".equalsIgnoreCase(status)) {
                log.info("易宝支付通知非成功状态，data:{}", data);
                return "FAILURE";
            }
            String orderId = data.getStr("orderId");
            String uniqueOrderNo = data.getStr("uniqueOrderNo");
            String paySn = resolvePaySn(orderId);
            if (StrUtil.isBlank(paySn)) {
                log.error("易宝支付通知无法还原 pay_sn，orderId:{}, data:{}", orderId, data);
                return "FAILURE";
            }
            // 回写易宝订单号到 notifyData，供退款/查询定位；幂等
            if (StrUtil.isNotBlank(uniqueOrderNo)) {
                try {
                    Paylog cur = paylogService.getOne(new LambdaQueryWrapper<Paylog>().eq(Paylog::getPaySn, paySn));
                    JSONObject meta = yeepayMeta(cur);
                    meta.set("uniqueOrderNo", uniqueOrderNo);
                    paylogService.lambdaUpdate()
                            .eq(Paylog::getPaySn, paySn)
                            .set(Paylog::getNotifyData, meta.toString())
                            .update();
                } catch (Exception ex) {
                    log.warn("易宝回写 uniqueOrderNo 到 notifyData 失败，pay_sn:{}", paySn, ex);
                }
            }
            if (!checkOrderPayStatus(paySn)) {
                paySuccess(paySn, StrUtil.isNotBlank(uniqueOrderNo) ? uniqueOrderNo : orderId);
                log.info("易宝支付成功处理完成，pay_sn:{}, uniqueOrderNo:{}", paySn, uniqueOrderNo);
            } else {
                log.info("易宝订单已支付，跳过，pay_sn:{}", paySn);
            }
            return "SUCCESS";
        } catch (Exception e) {
            log.error("易宝支付通知处理异常", e);
            return "FAILURE";
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String refundNotify(Map<String, Object> map) {
        JSONObject data = decryptNotify(map);
        if (data == null) {
            return "FAILURE";
        }
        try {
            String status = data.getStr("status");
            if (!"SUCCESS".equalsIgnoreCase(status)) {
                log.info("易宝退款通知非成功状态，data:{}", data);
                return "FAILURE";
            }
            // 退款请求号回写：优先 refundRequestId
            String refundSn = firstNonBlank(data.getStr("refundRequestId"), data.getStr("orderId"));
            if (StrUtil.isBlank(refundSn)) {
                log.error("易宝退款通知缺失 refundRequestId");
                return "FAILURE";
            }
            refundSuccess(refundSn);
            log.info("易宝退款成功处理完成，refund_sn:{}", refundSn);
            return "SUCCESS";
        } catch (Exception e) {
            log.error("易宝退款通知处理异常", e);
            return "FAILURE";
        }
    }

    @Override
    public PayPlatformStateBO payPlatformStatus(String outTradeNo, String tradeNo, String clientType) {
        try {
            requireConfigured();
            Paylog paylog = paylogService.getOne(new LambdaQueryWrapper<Paylog>().eq(Paylog::getPaySn, outTradeNo));
            String origOrderId = paylog != null && StrUtil.isNotBlank(paylog.getTransactionId())
                    ? paylog.getTransactionId() : outTradeNo;
            String uniqueOrderNo = StrUtil.isNotBlank(tradeNo) ? tradeNo : yeepayMeta(paylog).getStr("uniqueOrderNo");

            YopRequest request = newRequest(PATH_TRADE_ORDER_QUERY, "GET");
            request.addParameter("parentMerchantNo", parentMerchantNo());
            request.addParameter("merchantNo", merchantNo());
            request.addParameter("orderId", origOrderId);
            if (StrUtil.isNotBlank(uniqueOrderNo)) {
                request.addParameter("uniqueOrderNo", uniqueOrderNo);
            }
            JSONObject resp = execute(request);
            PayPlatformStateBO bo = new PayPlatformStateBO();
            bo.setOutTradeNo(outTradeNo);
            bo.setTradeNo(Optional.ofNullable(resp.getStr("uniqueOrderNo")).orElse(uniqueOrderNo));
            bo.setTradeState(Optional.ofNullable(resp.getStr("status")).orElse("UNKNOWN"));
            return bo;
        } catch (Exception e) {
            log.error("易宝查询支付状态异常，outTradeNo:{}", outTradeNo, e);
            return null;
        }
    }

    // ============ helpers ============

    /**
     * 解密并验签易宝异步通知：通知以 form 参数 {@code response} 承载数字信封串
     * （{@code content$sign$AES$SHA256}），用商户私钥经 SDK {@link DigitalEnvelopeUtils} 解密+验签，
     * 返回业务明文 JSON。
     */
    private JSONObject decryptNotify(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        String envelope = firstNonBlank(
                map.get("response") == null ? null : String.valueOf(map.get("response")),
                map.get("requestBody") == null ? null : String.valueOf(map.get("requestBody")));
        if (StrUtil.isBlank(envelope)) {
            log.error("易宝回调缺失 response 数字信封");
            return null;
        }
        try {
            String plain = DigitalEnvelopeUtils.decrypt(envelope, isvPrivateKey());
            if (StrUtil.isBlank(plain)) {
                return null;
            }
            // 兜底：若返回 content$sign 形态，取第 1 段业务明文
            if (!JSONUtil.isTypeJSON(plain) && plain.contains("$")) {
                plain = plain.substring(0, plain.indexOf('$'));
            }
            return JSONUtil.parseObj(plain);
        } catch (Exception e) {
            log.error("易宝回调解密/验签失败", e);
            return null;
        }
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (StrUtil.isNotBlank(v)) {
                return v;
            }
        }
        return null;
    }

    /**
     * 从回调 orderId 还原业务 paySn，双通道兜底：
     * 1) 按 "-" 截断得到候选 paySn，命中 paylog 则返回；
     * 2) 未命中时用完整 orderId 反查 paylog.transactionId（下单时写入），取其 paySn。
     */
    private String resolvePaySn(String orderId) {
        if (StrUtil.isBlank(orderId)) {
            return null;
        }
        int idx = orderId.lastIndexOf('-');
        String candidate = idx <= 0 ? orderId : orderId.substring(0, idx);
        if (StrUtil.isNotBlank(candidate)
                && paylogService.getOne(new LambdaQueryWrapper<Paylog>().eq(Paylog::getPaySn, candidate)) != null) {
            return candidate;
        }
        Paylog byTxn = paylogService.getOne(new LambdaQueryWrapper<Paylog>().eq(Paylog::getTransactionId, orderId));
        if (byTxn != null && StrUtil.isNotBlank(byTxn.getPaySn())) {
            return byTxn.getPaySn();
        }
        return candidate;
    }

    /** 解析 paylog.notifyData 中存的易宝订单号；兼容历史旧单（空/非 JSON）返回空对象。 */
    private JSONObject yeepayMeta(Paylog paylog) {
        if (paylog == null || StrUtil.isBlank(paylog.getNotifyData())) {
            return new JSONObject();
        }
        try {
            return JSONUtil.parseObj(paylog.getNotifyData());
        } catch (Exception e) {
            return new JSONObject();
        }
    }
}
