// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.enums.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "退款通知类型")
public enum RefundNotifyType {
    @Schema(description = "退款成功通知")
    REFUND_SUCCESS("REFUND.SUCCESS"),

    @Schema(description = "退款异常通知")
    REFUND_ABNORMAL("REFUND.ABNORMAL"),

    @Schema(description = "退款关闭通知")
    REFUND_CLOSED("REFUND.CLOSED");

    private final String code;

    RefundNotifyType(String code) {
        this.code = code;
    }

    public static RefundNotifyType fromCode(String code) {
        for (RefundNotifyType type : RefundNotifyType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown refund notify type code: " + code);
    }
}
