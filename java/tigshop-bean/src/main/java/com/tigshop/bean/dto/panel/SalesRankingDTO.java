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
 * 销售排行
 *
 * @author Tigshop团队
 * @create 2024年12月31日 14:38
 */
@Data
@Schema(description = "销售排行")
public class SalesRankingDTO {

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "关键词")
    private String keyword;

    @Schema(description = "是否导出")
    private Integer isExport;

    @Schema(description = "当前页")
    private Integer page;

    @Schema(description = "每页大小")
    private Integer size;

    @Schema(description = "排序字段")
    private String sortField;

    @Schema(description = "排序顺序")
    private String sortOrder;

}
