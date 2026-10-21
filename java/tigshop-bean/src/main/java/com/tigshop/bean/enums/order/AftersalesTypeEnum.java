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

import com.tigshop.common.exception.GlobalException;
import lombok.Getter;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;

/**
 * @author ：Tigshop
 */
@Getter
public enum AftersalesTypeEnum {
    /**
     * 退货/退款
     */
    RETURN(1, "退货/退款"),
    /**
     * 仅退款
     */
    PAYRETURN(2, "仅退款");

    private final int code;
    private final String description;

    AftersalesTypeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 判断一个值是否为有效的 code
     *
     * @param code 状态码
     * @return 如果 code 有效则返回 true；否则返回 false
     */
    public static boolean isValidCode(int code) {
        for (AftersalesTypeEnum type : values()) {
            if (type.code == code) {
                return true;
            }
        }
        return false;
    }

    public static String getTypeName(Integer type) {
        for (AftersalesTypeEnum aftersalesTypeEnum : values()) {
            if (aftersalesTypeEnum.code == type) {
                return aftersalesTypeEnum.getDescription();
            }
        }
        return "";
    }

    public static AftersalesTypeEnum getType(Integer type) {
        for (AftersalesTypeEnum aftersalesTypeEnum : values()) {
            if (aftersalesTypeEnum.code == type) {
                return aftersalesTypeEnum;
            }
        }
        throw new GlobalException(DATA_TYPE_ERROR);
    }
}
