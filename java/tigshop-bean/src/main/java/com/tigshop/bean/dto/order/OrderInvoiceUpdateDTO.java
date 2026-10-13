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
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static com.tigshop.common.constant.order.OrderInvoiceConstants.ORDER_INVOICE_ID_NOT_NULL;

/**
 * 订单发票更新参数
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:17
 */
@Data
@Schema(description = "订单发票参数")
public class OrderInvoiceUpdateDTO {
    @Schema(description = "订单发票ID")
    @NotNull(message = ORDER_INVOICE_ID_NOT_NULL)
    private Integer orderInvoiceId;

    @Schema(description = "订单发票名称")
    private String orderInvoiceName;

    @Schema(description = "订单发票图片")
    private String orderInvoicePic;

    @Schema(description = "订单发票排序")
    private Integer sortOrder;

    @Schema(description = "是否展示")
    private Integer isShow;

    @Schema(description = "状态")
    private Integer status;
}
