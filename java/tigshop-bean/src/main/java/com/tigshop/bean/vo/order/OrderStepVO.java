// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.order;

import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "进度步骤")
public class OrderStepVO {
    @Schema(description = "当前步骤")
    private Integer current;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "订单步骤列表")
    private List<OrderStepDetailVO> steps;

    @Data
    public static class OrderStepDetailVO {
        @Schema(description = "步骤标题")
        @JsonTranslate
        private String title;

        @Schema(description = "步骤描述")
        private String description;

    }
}