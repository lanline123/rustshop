package com.tigshop.service.pay.impl;

import cn.hutool.core.util.StrUtil;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.finance.PaylogRefund;
import com.tigshop.bean.model.finance.RefundApply;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.mapper.finance.RefundApplyMapper;
import com.tigshop.service.finance.PaylogRefundService;
import com.tigshop.service.finance.RefundApplyService;
import com.tigshop.service.pay.PaymentService;
import com.tigshop.service.setting.ConfigService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 订单支付
 *
 * @author Tigshop团队
 * @create 2024-09-30 15:16:06
 */
@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {

    @Resource
    private ConfigService configService;

    @Resource
    private ObjectProvider<RefundApplyService> refundApplyService;

    private final PaylogRefundService paylogRefundService;

    private final RefundApplyMapper refundApplyMapper;


    @Override
    public List<String> getAvailablePayment(String type) {

        String clientType = HeaderUtils.getClientType();

        List<String> paymentList = new ArrayList<>();

        // 容错说明：所有支付开关使用 getConfigVal（缺失返回空串）替代 getConfigByCode（缺失抛 CONFIG_NOT_EXIST），
        // 避免任一开关 config 记录缺失（例如老库未执行新增开关的 update.sql）导致整个支付页面无法打开。
        String useWechatVal = configService.getConfigVal(SettingsEnum.USE_WECHAT);
        if (StrUtil.isNotBlank(useWechatVal) && Integer.parseInt(useWechatVal) == 1) {
            paymentList.add("wechat");
        }

        String useAlipayVal = configService.getConfigVal(SettingsEnum.USE_ALIPAY);
        if (StrUtil.isNotBlank(useAlipayVal) && Integer.parseInt(useAlipayVal) == 1) {
            if (!"miniProgram".equals(clientType) && !"wechat".equals(clientType)) {
                paymentList.add("alipay");
            }
        }

        String usePaypalVal = configService.getConfigVal(SettingsEnum.USE_PAYPAL);
        if (StrUtil.isNotBlank(usePaypalVal) && Integer.parseInt(usePaypalVal) == 1) {
            paymentList.add("paypal");
        }


        // Apple Pay（全版本可见）：开关开启 + 配置齐全 + clientType∈{h5,pc}（BR-02，第一层过滤；第二层设备检测在前端）
        String useApplePayVal = configService.getConfigVal(SettingsEnum.USE_APPLE_PAY);
        if (StrUtil.isNotBlank(useApplePayVal) && Integer.parseInt(useApplePayVal) == 1
                && ("h5".equals(clientType) || "pc".equals(clientType))
                && isApplePayConfigComplete()) {
            paymentList.add("applepay");
        }

        // 拉卡拉聚合收单：单一开关控制，按钱包拆码平铺（微信始终；支付宝在微信内/小程序无法拉起，排除）
        String useLakalaVal = configService.getConfigVal(SettingsEnum.USE_LAKALA);
        if (StrUtil.isNotBlank(useLakalaVal) && Integer.parseInt(useLakalaVal) == 1) {
            paymentList.add("lakalaWechat");
            if (!"miniProgram".equals(clientType) && !"wechat".equals(clientType)) {
                paymentList.add("lakalaAlipay");
            }
        }


        String useYabanpayVal = configService.getConfigVal(SettingsEnum.USE_YABANPAY);
        if (StrUtil.isNotBlank(useYabanpayVal) && Integer.parseInt(useYabanpayVal) == 1) {
            //检测是否开启微信/支付宝支付
            String useYabanpayWechatVal = configService.getConfigVal(SettingsEnum.USE_YABANPAY_WECHAT);
            if (StrUtil.isNotBlank(useYabanpayWechatVal) && Integer.parseInt(useYabanpayWechatVal) == 1) {
                paymentList.add("yabanpayWechat");
            }
            String useYabanpayAlipayVal = configService.getConfigVal(SettingsEnum.USE_YABANPAY_ALIPAY);
            if (StrUtil.isNotBlank(useYabanpayAlipayVal) && Integer.parseInt(useYabanpayAlipayVal) == 1 && !"miniProgram".equals(clientType) && !"wechat".equals(clientType)) {
                paymentList.add("yabanpayAlipay");
            }
        }

        String useYunpayVal = configService.getConfigVal(SettingsEnum.USE_YUNPAY);
        if (StrUtil.isNotBlank(useYunpayVal) && Integer.parseInt(useYunpayVal) == 1) {
            paymentList.add("yunpayWechat");
            if (!"miniProgram".equals(clientType) && !"wechat".equals(clientType)) {
                paymentList.add("yunpayAlipay");
            }
//            paymentList.add("yunpay_yunshanfu");
        }

        // 汇付斗拱聚合收单（境内，单一总开关统一控制；开启后微信+支付宝均可用）
        String useHuifuVal = configService.getConfigVal(SettingsEnum.USE_HUIFU);
        if (StrUtil.isNotBlank(useHuifuVal) && Integer.parseInt(useHuifuVal) == 1) {
            paymentList.add("huifuWechat");
            // 支付宝在微信内/小程序环境无法拉起，排除展示
            if (!"miniProgram".equals(clientType) && !"wechat".equals(clientType)) {
                paymentList.add("huifuAlipay");
            }
        }

        // 易宝支付聚合收单（境内，单一总开关统一控制；开启后微信+支付宝均可用）
        String useYeepayVal = configService.getConfigVal(SettingsEnum.USE_YEEPAY);
        if (StrUtil.isNotBlank(useYeepayVal) && Integer.parseInt(useYeepayVal) == 1) {
            paymentList.add("yeepayWechat");
            // 支付宝在微信内/小程序环境无法拉起，排除展示
            if (!"miniProgram".equals(clientType) && !"wechat".equals(clientType)) {
                paymentList.add("yeepayAlipay");
            }
        }

        String useOfflineVal = configService.getConfigVal(SettingsEnum.USE_OFFLINE);
        if (StrUtil.isNotBlank(useOfflineVal) && Integer.parseInt(useOfflineVal) == 1 && "order".equals(type)) {
            paymentList.add("offline");
        }

        // 按照配置的支付顺序排序
        try {
            String paymentOrderVal = configService.getConfigVal(SettingsEnum.PAYMENT_ORDER);
            if (StrUtil.isNotBlank(paymentOrderVal)) {
                List<String> orderList = Arrays.asList(paymentOrderVal.split(","));
                paymentList.sort((a, b) -> {
                    int indexA = getPaymentOrderIndex(orderList, a);
                    int indexB = getPaymentOrderIndex(orderList, b);
                    return Integer.compare(indexA, indexB);
                });
            }
        } catch (Exception e) {

        }

        return paymentList;
    }

    @Transactional
    @Override
    public void refundSuccess(String refundSn) {
        PaylogRefund paylogRefund = paylogRefundService.lambdaQuery()
                .eq(PaylogRefund::getRefundSn, refundSn)
                .one();
        if (paylogRefund == null) {
            return;
        }

        if (paylogRefund.getStatus() == 1) {
            return;
        }

        if (paylogRefund.getOrderId() == null) {
            return;
        }

        try {
            paylogRefund.setStatus(1);
            paylogRefund.setNotifyTime(System.currentTimeMillis()/1000);
            paylogRefundService.updateById(paylogRefund);

            RefundApply refundApply = refundApplyService.getIfAvailable().getByPaylogRefundId(paylogRefund.getPaylogRefundId());
//            RefundApply refundApply = refundApplyMapper.selectOne(
//                    Wrappers.lambdaQuery(RefundApply.class)
//                            .eq(RefundApply::getPaylogRefundId, paylogRefund.getPaylogRefundId())
//            );
            if (refundApply == null) {
                return;
            }

            refundApply.setIsOnline(2);

            if (refundApply.getIsOffline() != 1 && refundApply.getIsOnline() != 1 && refundApply.getIsReceive() != 1) {
                refundApply.setRefundStatus(2);
            }
            refundApplyMapper.updateById(refundApply);
        } catch (Exception e) {
            throw new GlobalException(e.getMessage());
        }
    }

    /**
     * Apple Pay 配置完整性校验（BR-02/BR-08 双保险）：
     * Merchant ID + 商户身份证书 + 收单参数齐全才对外可见。
     * 支付处理证书私钥仅 generic（商户自解密）必需，adyen/ums/stripe 由收单方解密，
     * 与保存校验 {@code ApplePayConfigServiceImpl#saveApplePaySettings} 保持同一规则。
     */
    private boolean isApplePayConfigComplete() {
        String provider = StrUtil.blankToDefault(
                configService.getConfigVal(SettingsEnum.APPLE_PAY_ACQUIRER_PROVIDER), "generic");
        boolean processingKeyReady = !"generic".equalsIgnoreCase(provider)
                || "1".equals(configService.getConfigVal(SettingsEnum.APPLE_PAY_PROCESSING_PRIVATE_KEY));
        return StrUtil.isNotBlank(configService.getConfigVal(SettingsEnum.APPLE_PAY_MERCHANT_ID))
                && StrUtil.isNotBlank(configService.getConfigVal(SettingsEnum.APPLE_PAY_DISPLAY_NAME))
                && "1".equals(configService.getConfigVal(SettingsEnum.APPLE_PAY_MERCHANT_IDENTITY_CERT))
                && "1".equals(configService.getConfigVal(SettingsEnum.APPLE_PAY_MERCHANT_IDENTITY_KEY))
                && processingKeyReady
                && StrUtil.isNotBlank(configService.getConfigVal(SettingsEnum.APPLE_PAY_ACQUIRER_GATEWAY))
                && StrUtil.isNotBlank(configService.getConfigVal(SettingsEnum.APPLE_PAY_ACQUIRER_MERCHANT_NO))
                && StrUtil.isNotBlank(configService.getConfigVal(SettingsEnum.APPLE_PAY_ACQUIRER_KEY));
    }

    /**
     * 获取支付方式在配置顺序中的索引
     * 处理 yabanpayWechat/yabanpayAlipay -> yabanpay，yunpayWechat/yunpayAlipay -> yunpay 的映射
     */
    private int getPaymentOrderIndex(List<String> orderList, String paymentMethod) {
        // 直接匹配
        int index = orderList.indexOf(paymentMethod);
        if (index != -1) {
            return index;
        }
        // 映射匹配：yabanpayWechat/yabanpayAlipay -> yabanpay
        if (paymentMethod.startsWith("yabanpay")) {
            index = orderList.indexOf("yabanpay");
            if (index != -1) {
                // 同一父级下，Wechat排在Alipay前面
                return paymentMethod.contains("Wechat") ? index : index + 1;
            }
        }
        // 映射匹配：yunpayWechat/yunpayAlipay -> yunpay
        if (paymentMethod.startsWith("yunpay")) {
            index = orderList.indexOf("yunpay");
            if (index != -1) {
                return paymentMethod.contains("Wechat") ? index : index + 1;
            }
        }
        // 映射匹配：huifuWechat/huifuAlipay -> huifu
        if (paymentMethod.startsWith("huifu")) {
            index = orderList.indexOf("huifu");
            if (index != -1) {
                return paymentMethod.contains("Wechat") ? index : index + 1;
            }
        }
        // 映射匹配：lakalaWechat/lakalaAlipay -> lakala
        if (paymentMethod.startsWith("lakala")) {
            index = orderList.indexOf("lakala");
            if (index != -1) {
                return paymentMethod.contains("Wechat") ? index : index + 1;
            }
        }
        // 映射匹配：yeepayWechat/yeepayAlipay -> yeepay
        if (paymentMethod.startsWith("yeepay")) {
            index = orderList.indexOf("yeepay");
            if (index != -1) {
                return paymentMethod.contains("Wechat") ? index : index + 1;
            }
        }
        // 找不到放到最后
        return Integer.MAX_VALUE;
    }
}