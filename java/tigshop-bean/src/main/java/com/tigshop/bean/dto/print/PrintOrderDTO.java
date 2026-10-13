// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.dto.print;

import com.tigshop.bean.model.print.Print;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 打印订单DTO
 *
 * @author Tigshop团队
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "打印订单DTO")
public class PrintOrderDTO {

    @Schema(description = "打印机")
    private Print print;

    @Schema(description = "订单ID")
    private Integer orderId;

    /**
     * 触发方式，向后兼容：未传时消费者默认按 AUTO(1) 处理。
     * 取值见 {@link com.tigshop.bean.enums.print.PrintTriggerTypeEnum}。
     */
    @Schema(description = "触发方式 1=自动 2=手动补打，默认自动")
    private Integer triggerType;

    @Schema(description = "收银台设备码")
    private String deviceCode;

}
