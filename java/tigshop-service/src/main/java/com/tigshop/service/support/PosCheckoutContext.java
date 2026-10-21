package com.tigshop.service.support;

import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.param.o2o.BaseChangePriceParam;
import com.tigshop.common.utils.HeaderUtils;

/**
 * POS 业务场景识别。
 *
 * <p>当前收银台发送真实 {@code shop/admin} 登录身份，业务场景由 POS 专属控制器显式标记；
 * 同时保留遗留 {@code adminType=pos} 请求兼容。</p>
 */
public final class PosCheckoutContext {

    private PosCheckoutContext() {
    }

    public static boolean isPosCheckout(BaseChangePriceParam param) {
        if (param != null && param.isPosCheckout()) {
            return true;
        }
        return isPosCheckout(param, HeaderUtils.getAdminType());
    }

    static boolean isPosCheckout(BaseChangePriceParam param, String adminType) {
        return (param != null && param.isPosCheckout())
                || AdminTypeEnum.POS.getCode().equals(adminType);
    }
}
