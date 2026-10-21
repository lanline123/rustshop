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

package com.tigshop.service.pay.impl;

import cn.hutool.json.JSONUtil;
import com.alipay.v3.ApiClient;
import com.alipay.v3.ApiException;
import com.alipay.v3.Configuration;
import com.alipay.v3.api.AlipayTradeApi;
import com.alipay.v3.model.AlipayTradePayModel;
import com.alipay.v3.model.ExtendParams;
import com.alipay.v3.model.OpenApiRoyaltyDetailInfoPojo;
import com.alipay.v3.model.AlipayTradeRefundModel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.dto.finance.profitsharing.RefundSplitReversal;
import com.tigshop.bean.model.finance.Paylog;
import com.tigshop.service.finance.PaylogService;
import com.tigshop.service.finance.ProfitSharingOrderService;
import com.alipay.v3.model.AlipayTradeRefundResponseModel;
import com.alipay.v3.util.AlipaySignature;
import com.alipay.v3.util.GenericExecuteApi;
import com.alipay.v3.util.model.AlipayConfig;
import com.tigshop.bean.bo.PayPlatformStateBO;
import com.tigshop.bean.bo.pay.ProfitSharingPaymentContext;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.bean.vo.order.pay.PayCreateVO;
import com.tigshop.bean.vo.order.pay.PayNotifyVO;
import com.tigshop.common.constant.finance.PayConstants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.service.pay.ProfitSharingAwarePayService;
import com.tigshop.service.setting.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 支付宝
 *
 * @author Tigshop团队
 * @create 2025年02月21日 11:21
 */
@Service("AliPayService")
@Slf4j
public class AliPayServiceImpl extends PayBaseServiceImpl implements ProfitSharingAwarePayService {
    private final ConfigService configService;


    public AliPayServiceImpl(ConfigService configService) {
        this.configService = configService;
    }

    @Override
    public PayCreateVO.PayInfo pay(OrderVO orderVO, String openid, String clientType) {
        return pay(orderVO, openid, clientType, ProfitSharingPaymentContext.none());
    }

    @Override
    public PayCreateVO.PayInfo pay(OrderVO orderVO, String openid, String clientType,
                                   ProfitSharingPaymentContext context) {
        return pay(orderVO, clientType, context);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayNotifyVO payRefund(String outTradeNo, String refundNo, BigDecimal refundAmount, BigDecimal totalAmount, String payType, String subMchId) {
        // 构造请求参数以调用接口
        getAliPayConfig();
        AlipayTradeApi api = new AlipayTradeApi();
        AlipayTradeRefundModel data = new AlipayTradeRefundModel();
        // 设置商户订单号
        data.setOutTradeNo(outTradeNo);
        data.setOutRequestNo(refundNo);
        // 设置退款金额
        data.setRefundAmount(refundAmount.toString());
        // 设置退款原因说明
        data.setRefundReason("退款");
        try {
            AlipayTradeRefundResponseModel response = api.refund(data);
            if ("Y".equals(response.getFundChange())) {
                refundSuccess(refundNo);
                return new PayNotifyVO("SUCCESS", "退款成功");
            } else if ("N".equals(response.getFundChange()) && response.getRefundFee() != null) {
                // 说明退款已经处理过，视为成功
                log.info("退款已处理过，refundFee: {}", response.getRefundFee());
                refundSuccess(refundNo);
                return new PayNotifyVO("SUCCESS", "退款已处理");
            }
            return new PayNotifyVO("FAIL", "退款失败");
        } catch (ApiException e) {
            log.error("支付宝退款", e);
            throw new GlobalException(JSONUtil.parseObj(e.getResponseBody())
                                              .getStr("message"));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String payNotify(Map<String, Object> resMap) {
        log.info("支付宝回调{}", JSONUtil.toJsonStr(resMap));
        String signVerified = buildNotify(resMap);
        if ("success".equals(signVerified)) {
            String paySn = (String) resMap.get("out_trade_no");
            if (!checkOrderPayStatus(paySn) && "TRADE_SUCCESS".equals(resMap.get("trade_status"))) {
                //修改订单状态
                paySuccess(paySn, (String) resMap.get("trade_no"));
            }
        }
        return signVerified;
    }

    /**
     * 构建支付宝回调数据
     *
     * @param resMap 回调数据
     * @return String
     */
    private String buildNotify(Map<String, Object> resMap) {
        Map<String, String> param = resMap.entrySet()
                                          .stream()
                                          .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() == null ? "" : e.getValue()
                                                                                                                         .toString()));
        try {
            boolean signVerified = AlipaySignature.rsaCheckV1(param, configService.getConfigByCode(SettingsEnum.ALIPAY_RSA_PUBLIC_KEY.getBizCode())
                                                                                  .getBizVal(), "UTF-8", "RSA2");
            if (!signVerified) {
                return "fail";
            }
            // 验签通过后，处理业务逻辑
            return "success";
        } catch (ApiException e) {
            return "fail";
        }
    }

    @Override
    public String refundNotify(Map<String, Object> resMap) {
        // 支付包没有退款回调
        return buildNotify(resMap);
    }

    @Override
    public PayPlatformStateBO payPlatformStatus(String outTradeNo, String tradeNo, String clientType) {
        throw new GlobalException("alipay not support");
    }

    /**
     * 支付宝支付
     *
     * @param orderVO    订单信息
     * @param clientType 支付客户端类型
     * @return String
     */
    public PayCreateVO.PayInfo pay(OrderVO orderVO, String clientType) {
        return pay(orderVO, clientType, ProfitSharingPaymentContext.none());
    }

    private PayCreateVO.PayInfo pay(OrderVO orderVO, String clientType,
                                    ProfitSharingPaymentContext context) {
        // 获取支付宝支付服务
        getAliPayConfig();
        //实例化客户端
        GenericExecuteApi api = new GenericExecuteApi();
        //设置业务参数
        Map<String, Object> bizParams = new HashMap<>();
        // 设置支付信息
        setPayInfo(bizParams, orderVO, context, clientType);
        // 默认使用电脑端的支付
        // 判断支付方式
        try {
            switch (clientType) {
                case "h5" -> {
                    String method = "alipay.trade.wap.pay";
                    String res = api.pageExecute(method, "POST", bizParams);
                    System.out.println(res);
                    return new PayCreateVO.PayInfo("", res.replace("\"", "'"));
                }
                case "app", "android", "ios" -> {
                    String method = "alipay.trade.app.pay";
                    String res = api.sdkExecute(method, bizParams);
                    System.out.println(res);
                    return new PayCreateVO.PayInfo("alipay", "", res.replace("\"", "'"));
                }
                case PayConstants.CODE_PAY_CODE -> {
                    codePay(orderVO, context);
                    return new PayCreateVO.PayInfo("alipay", "", "");
                }
                default -> {
                    String method = "alipay.trade.page.pay";
                    String res = api.pageExecute(method, "POST", bizParams);
                    System.out.println(res);
                    return new PayCreateVO.PayInfo("", res.replace("\"", "'"));
                }
            }
        } catch (ApiException e) {
            throw new GlobalException(e.getMessage());
        }
    }

    private void codePay(OrderVO orderVO, ProfitSharingPaymentContext context) {
        String authCode = orderVO.getAuthCode();
        if (StringUtils.isBlank(authCode)) {
            throw new GlobalException("请输入付款码");
        }
        AlipayTradeApi tradeApi = new AlipayTradeApi();
        AlipayTradePayModel alipayTradePayModel = new AlipayTradePayModel();
        alipayTradePayModel.setSubject(orderVO.getOrderSn());
        alipayTradePayModel.setOutTradeNo(orderVO.getPaySn());
        alipayTradePayModel.setTotalAmount(orderVO.getUnpaidAmount()
                                                  .toString());
        //商家和支付宝签约的产品码。
        //当面付场景下，如果签约的是当面付快捷版，则传 OFFLINE_PAYMENT;
        //其它支付宝当面付产品传 FACE_TO_FACE_PAYMENT；
        //不传则默认使用FACE_TO_FACE_PAYMENT。
        alipayTradePayModel.setProductCode("FACE_TO_FACE_PAYMENT");
        alipayTradePayModel.setTimeoutExpress("60m");
        alipayTradePayModel.setNotifyUrl(getPayNotifyUrl("ali"));
        alipayTradePayModel.setAuthCode(authCode);
        if (authCode.startsWith("fp")) {
            //刷脸
            alipayTradePayModel.setScene("security_code");
        } else {
            //用户展示条形码
            alipayTradePayModel.setScene("bar_code");
        }
        try {
            tradeApi.pay(alipayTradePayModel);
        } catch (ApiException apiException) {
            String message = apiException.getMessage();
            if (message.indexOf("ACQ.PAYMENT_AUTH_CODE_INVALID") >= 0) {
                throw new GlobalException("请输入正确的付款码");
            }
            throw new GlobalException(message);
        }
    }

    /**
     * 设置支付信息
     *
     * @param bizParams 设置业务参数
     * @param orderVO   订单信息
     */
    public void setPayInfo(Map<String, Object> bizParams, OrderVO orderVO) {
        setPayInfo(bizParams, orderVO, ProfitSharingPaymentContext.none());
    }

    public void setPayInfo(Map<String, Object> bizParams, OrderVO orderVO,
                           ProfitSharingPaymentContext context) {
        setPayInfo(bizParams, orderVO, context, null);
    }

    void setPayInfo(Map<String, Object> bizParams, OrderVO orderVO,
                    ProfitSharingPaymentContext context, String clientType) {
        //设置bizContent
        Map<String, Object> otherParams = new HashMap<>();
        otherParams.put("subject", orderVO.getOrderSn());
        otherParams.put("out_trade_no", orderVO.getPaySn());
        otherParams.put("total_amount", orderVO.getUnpaidAmount());
        otherParams.put("product_code", "FAST_INSTANT_TRADE_PAY");
        otherParams.put("timeout_express", "60m");
        String returnUrl = getReturnUrl(orderVO.getOrderId());
        if ("h5".equals(clientType) && !Integer.valueOf(1).equals(orderVO.getOrderType())) {
            String h5Domain = configService.getConfigByCode(SettingsEnum.H5_DOMAIN.getBizCode()).getBizVal();
            returnUrl = getH5CashierUrl(h5Domain, orderVO.getOrderId(), "alipay");
        }
        otherParams.put("request_from_url", returnUrl);
        // 设置业务参数
        bizParams.put("biz_content", otherParams);
        bizParams.put("notify_url", getPayNotifyUrl("ali"));
        bizParams.put("return_url", returnUrl);
        log.info("bizParams:{}", JSONUtil.toJsonStr(bizParams));
    }

    private boolean isAlipayProfitSharing(ProfitSharingPaymentContext context) {
        return context != null && "ALIPAY".equals(context.getChannel());
    }

    private String cfgVal(String bizCode) {
        ConfigPO po = configService.getConfigByCode(bizCode);
        return po == null ? null : po.getBizVal();
    }


    // 获取支付宝支付服务
    public void getAliPayConfig() {
        ConfigPO alipayAppidConfig = configService.getConfigByCode(SettingsEnum.ALIPAY_APPID.getBizCode());
        ConfigPO alipayRsaPrivateKeyConfig = configService.getConfigByCode(SettingsEnum.ALIPAY_RSA_PRIVATE_KEY.getBizCode());
        ConfigPO alipayRsaPublicKeyConfig = configService.getConfigByCode(SettingsEnum.ALIPAY_RSA_PUBLIC_KEY.getBizCode());
        ApiClient apiClient = Configuration.getDefaultApiClient();
        // 设置网关地址
        apiClient.setBasePath("https://openapi.alipay.com");
        // 设置alipayConfig参数（全局设置一次）
        AlipayConfig alipayConfig = new AlipayConfig();
        // 设置应用ID
        alipayConfig.setAppId(alipayAppidConfig.getBizVal());
        // APP 支付使用移动端应用ID，PC/H5 不依赖该配置
        String clientType = HeaderUtils.getClientType();
        if ("app".equals(clientType)) {
            ConfigPO alipayMobileAppidConfig = configService.getConfigByCode(SettingsEnum.ALIPAY_MOBILE_APPID.getBizCode());
            alipayConfig.setAppId(alipayMobileAppidConfig.getBizVal());
        }
        // 设置应用私钥
        alipayConfig.setPrivateKey(alipayRsaPrivateKeyConfig.getBizVal());
        // 设置支付宝公钥
        alipayConfig.setAlipayPublicKey(alipayRsaPublicKeyConfig.getBizVal());
        try {
            apiClient.setAlipayConfig(alipayConfig);
        } catch (ApiException e) {
            throw new GlobalException(e.getMessage());
        }
    }
}
