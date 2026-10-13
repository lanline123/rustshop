// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 订单金额修改数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月04日 10:00
 */
@Data
@Schema(description = "订单金额修改参数")
public class OrderModifyShippingDTO {

    @Schema(description = "被操作的订单id")
    private Integer id;

    @Schema(description = "配送方式")
    private Integer shippingMethod;

    @Schema(description = "物流公司ID")
    private Integer logisticsId;

    @Schema(description = "物流跟踪号")
    private String trackingNo;
}
