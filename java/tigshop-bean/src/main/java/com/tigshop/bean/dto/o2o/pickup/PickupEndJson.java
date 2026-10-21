// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.dto.o2o.pickup;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Tigshop团队
 * @create 2025/8/23 16:13
 */
@Data
@Schema(description = "提货有效期")
public class PickupEndJson {

    @Schema(description = "1 停止提货时间")
    private Integer type;
    @Schema(description = "天数")
    private Integer day;
    @Schema(description = "小时")
    private Integer hour;
}
