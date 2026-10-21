// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.vo.vendor;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Tigshop团队
 * @create 2025/7/25 13:23
 */
@Data
public class ShopVendorSettingVO {
    @Schema(description = "供应商设价方式：(1按比例，2-按固定数值加价，3-默认售价)")
    private Integer vendorSetPriceType;

    @Schema(description = "智能设价（百分比或固定数值）")
    private String vendorSetPriceAutoValue;
}
