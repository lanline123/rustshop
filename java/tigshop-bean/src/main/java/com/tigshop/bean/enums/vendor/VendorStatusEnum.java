// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.enums.vendor;


import lombok.Getter;

/**
 * @author Tigshop团队
 * @create 2025/7/4 10:58
 */
@Getter
public enum VendorStatusEnum {
    OPEN(1, "开启"),

    CLOSE(2, "关闭"),
    ;

    private final int code;
    private final String description;

    VendorStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }
}
