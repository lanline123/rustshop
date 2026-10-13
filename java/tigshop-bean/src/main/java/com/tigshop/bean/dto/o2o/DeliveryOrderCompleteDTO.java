// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.o2o;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 完成配送/服务DTO
 *
 * @author Tigshop团队
 * @create 2024-12-16
 */
@Data
@Schema(description = "完成配送/服务参数")
public class DeliveryOrderCompleteDTO {

    @Schema(description = "派送订单ID")
    @NotNull(message = "派送订单ID不能为空")
    private Integer deliveryOrderId;

    @Schema(description = "完成照片/服务后照片（根据配置是否必填）")
    private List<String> photo;
}
