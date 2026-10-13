// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.panel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 销售明细看板
 *
 * @author Tigshop团队
 * @create 2024年12月31日 14:38
 */
@Data
@Schema(description = "销售明细看板")
public class SalesDataDTO {

    @Schema(description = "开始时间")
    private String startEndTime;

    @Schema(description = "统计类型")
    private Integer statisticType;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "是否导出")
    private Integer isExport;

    @Schema(description = "数据类型")
    private Integer dateType;

}
