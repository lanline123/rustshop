// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.param.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 尾款支付参数
 *
 * @author Tigshop团队
 * @create 2024年11月24日
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "尾款支付参数")
public class FinalPaymentParam implements Serializable {

    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "支付方式ID")
    private Integer payTypeId;
}

