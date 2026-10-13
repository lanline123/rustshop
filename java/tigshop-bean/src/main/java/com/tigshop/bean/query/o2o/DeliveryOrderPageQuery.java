// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.query.o2o;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 派送订单查询参数
 *
 * @author Tigshop团队
 * @create 2024-12-16
 */
@Schema(description = "派送订单查询参数")
@Setter
@Getter
public class DeliveryOrderPageQuery extends BasePage {

    @Schema(description = "门店ID（平台管理员可传）")
    private Integer shopId;

    @Schema(description = "类型：1-配送订单，2-服务订单")
    private Integer type;

    @Schema(description = "状态：10-待派发，20-待取货/待服务，30-配送中/服务中，40-已完成，50-已取消")
    private Integer status;

    @Schema(description = "(不匹配)状态：10-待派发，20-待取货/待服务，30-配送中/服务中，40-已完成，50-已取消")
    private Integer notIncludeStatus;

    @Schema(description = "派单模式：1-抢单模式，2-手动派单")
    private Integer dispatchMode;

    @Schema(description = "配送员/服务员ID")
    private Integer staffId;

    @Schema(description = "运力类型：1-自建配送，2-美团配送（预留多运力）；不传默认只查自建(1)，兼容历史调用")
    private Integer carrierType;

    @Schema(description = "开始时间 yyyy-MM-dd")
    private String searchStartTime;

    @Schema(description = "结束时间 yyyy-MM-dd")
    private String searchEndTime;

    @Schema(description = "开始创建时间", hidden = true)
    private Long startCreateTime;

    @Schema(description = "结束创建时间", hidden = true)
    private Long endCreateTime;

    @Schema(description = "快捷筛选：pending(待取货/待服务)，inProgress(进行中)，completed(已完成)，all(全部)")
    private String tab;

    @Schema(description = "配送员当前经度（用于计算距离）")
    private BigDecimal staffLongitude;

    @Schema(description = "配送员当前纬度（用于计算距离）")
    private BigDecimal staffLatitude;
}
