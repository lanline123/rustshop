// **---------------------------------------------------------------------+
// ** 文件 -- ${DESCRIPTION}
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.dto.order;

import com.tigshop.bean.dto.order.deliver.BatchDeliverData;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

import static com.tigshop.common.constant.ResultTextConstants.*;

/**
 * 批量操作
 *
 * @author Jayce
 * @create 2024年10月22日 13:50
 */
@Data
@Schema(description = "批量操作")
public class OrderBatchDTO {
    @Schema(description = "ids")
    @NotEmpty(message = UNSELECTED_ITEM)
    private List<Integer> ids;

    @NotNull(message = OPERATION_TYPE_IS_NULL)
    @Schema(description = "操作类型")
    private String type;

    @Schema(description = "发货数据")
    private BatchDeliverData data;

}
