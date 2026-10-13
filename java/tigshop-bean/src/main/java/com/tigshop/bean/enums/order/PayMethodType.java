// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.enums.order;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Tigshop
 */
@Getter
@AllArgsConstructor
public enum PayMethodType {
    WECHAT("wechat", "微信"),
    ALIPAY("alipay", "支付宝"),
    LAKALA("lakala", "拉卡拉"),
    LAKALA_WECHAT("lakalaWechat", "拉卡拉微信"),
    LAKALA_ALIPAY("lakalaAlipay", "拉卡拉支付宝"),
    HUIFU_WECHAT("huifuWechat", "汇付微信"),
    HUIFU_ALIPAY("huifuAlipay", "汇付支付宝"),
    YEEPAY_WECHAT("yeepayWechat", "易宝微信"),
    YEEPAY_ALIPAY("yeepayAlipay", "易宝支付宝"),
    PAYPAL("paypal", "paypal"),
    APPLEPAY("applepay", "Apple Pay"),
    OFFLINE("offline", "线下支付"),
    BALANCE("balance", "余额"),
    CASH("cash", "现金"),
    OTHER("other", "其他"),
    ;

    private final String code;
    private final String description;

    public static String getTypeName(String code) {
        for (PayMethodType payMethodType : PayMethodType.values()) {
            if (payMethodType.code.equals(code)) {
                return payMethodType.description;
            }
        }
        return code;
    }

    public static PayMethodType getTypeDescription(String code) {
        for (PayMethodType payMethodType : PayMethodType.values()) {
            if (payMethodType.code.equals(code)) {
                return payMethodType;
            }
        }
        return null;
    }


}
