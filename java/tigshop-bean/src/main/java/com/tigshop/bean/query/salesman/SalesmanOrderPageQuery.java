// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.query.salesman;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 分销业绩订单结算列表
 *
 * @author kidd
 * @create 2025/6/23
 */
@Setter
@Getter
@Schema(description = "分销业绩订单结算列表参数")
public class SalesmanOrderPageQuery extends BasePage {

    @Schema(description = "订单编号")
    private String orderSn;

    @Schema(description = "分销业绩订单状态")
    private Integer status;

    @Schema(description = "筛选时间类型")
    private Integer timeType;

    @Schema(description = "筛选时间开始")
    private String orderTimeStart;

    @Schema(description = "筛选时间结束")
    private String orderTimeEnd;

    @Schema(description = "是否导出")
    private Integer isExport;

    @Schema(description = "导出维度")
    private Integer range;

    @Schema(description = "分销员id")
    private Integer salesmanId;
}
