// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.param.vendor;


import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Tigshop团队
 * @create 2025/7/4 15:17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorShopBindPageParam extends BasePage {
    @Schema(description = "店铺名称")
    private String shopName;

    @Schema(description = "店铺id")
    private String shopId;

    @Schema(description = "商户id")
    private String merchantId;

    @Schema(description = "绑定开始时间")
    private String startTime;

    @Schema(description = "绑定结束时间")
    private String endTime;
}
