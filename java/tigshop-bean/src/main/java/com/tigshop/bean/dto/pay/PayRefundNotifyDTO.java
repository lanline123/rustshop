// **---------------------------------------------------------------------+
// ** 文件 -- ${DESCRIPTION}
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.dto.pay;

import com.tigshop.bean.dto.order.deliver.BatchDeliverData;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 退款回调
 *
 * @author Jayce
 * @create 2024年10月22日 13:50
 */
@Data
@Schema(description = "退款回调操作")
public class PayRefundNotifyDTO {

    @Schema(description = "ids")
    private List<Integer> ids;

    @Schema(description = "操作类型")
    private String type;

    @Schema(description = "发货数据")
    private BatchDeliverData data;

}
