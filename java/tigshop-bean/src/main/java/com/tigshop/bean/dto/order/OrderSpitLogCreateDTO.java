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
import jakarta.validation.constraints.Size;
import lombok.Data;

import static com.tigshop.common.constant.order.OrderSpitLogConstants.*;

/**
 * 订单拆分创建数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "订单拆分参数")
public class OrderSpitLogCreateDTO {
    @Schema(description = "订单拆分名称")
    @NotNull(message = ORDER_SPLIT_LOG_NOT_NULL)
    @Size(max = 50, message = ORDER_SPLIT_LOG_OVER_LENGTH)
    private String orderSpitLogName;

    @Schema(description = "订单拆分图片")
    private String orderSpitLogPic;

    @Schema(description = "订单拆分排序")
    private Integer sortOrder;

    @Schema(description = "是否展示")
    private Integer isShow;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "订单拆分描述")
    private String orderSpitLogDesc;
}
