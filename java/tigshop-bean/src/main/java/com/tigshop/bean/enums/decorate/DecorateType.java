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

package com.tigshop.bean.enums.decorate;

import com.tigshop.common.exception.GlobalException;
import lombok.Getter;

import java.util.Arrays;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;
import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;

/**
 * 装修类型
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:27
 */
@Getter
public enum DecorateType {
    H5(1, "H5"),
    PC(2, "PC"),
    CUSTOM(3,"自定义页面");

    private final Integer code;
    private final String name;

    DecorateType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static boolean isValidCode(int code) {
        return Arrays.stream(values())
                .anyMatch(status -> status.code == code);
    }

    /**
     * 根据类型编码获取类型
     *
     * @param code 类型编码
     * @return 类型
     */
    public static DecorateType fromTypeCode(Integer code) {
        for (DecorateType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new GlobalException(DATA_TYPE_ERROR, SERVICE_DATA_ERROR);
    }
}
