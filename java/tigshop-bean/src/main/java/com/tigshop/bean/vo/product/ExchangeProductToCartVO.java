// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.vo.product;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Tigshop团队
 * @create 2025/4/2 16:24
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "积分兑换购物车")
public class ExchangeProductToCartVO {
    @Schema(description = "是否成功")
    private boolean item;
    @Schema(description = "购物车类型")
    private Integer flowType;
}
