// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.param.account;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Tigshop团队
 * @create 2025/7/4 10:19
 */
@Data
public class ShopOrVendorBindParam {

    @Schema(description = "店铺id 或者 供应商id")
    private Integer id;

    @Schema(description = "类型：shop-店铺，vendor-供应商")
    @NotNull(message = "类型不能为空")
    private String type;

    @Schema(description = "管理员信息")
    private ShopOrVendorBindParam.AdminInfo adminData;

    /**
     * 管理员信息
     */
    @Data
    @Schema(description = "管理员信息")
    public static class AdminInfo {
        @Schema(description = "类型")
        private Integer type;

        @Schema(description = "用户ID")
        private Integer userId;

        @Schema(description = "管理员ID")
        private Integer adminId;
    }


}
