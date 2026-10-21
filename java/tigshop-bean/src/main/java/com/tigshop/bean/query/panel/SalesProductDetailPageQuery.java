// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.query.panel;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.tigshop.bean.enums.order.OrderStatusEnum;
import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 销售商品明细
 *
 * @author Tigshop团队
 * @create 2024年12月31日 14:38
 */
@Data
@Schema(description = "销售商品明细")
public class SalesProductDetailPageQuery extends BasePage {

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "是否导出")
    private Integer isExport;

    // *** Other ***

    @Schema(description = "开始时间时间戳", hidden = true)
    private Long startTimeStamp;

    @Schema(description = "结束时间时间戳", hidden = true)
    private Long endTimeStamp;

    @Schema(description = "订单状态", hidden = true)
    private String orderStatues;

    public void init() {
        this.startTimeStamp = this.transStartTime();
        this.endTimeStamp = this.transEndTime();
        this.orderStatues = OrderStatusEnum.CONFIRMED.getCode() + StrUtil.COMMA + OrderStatusEnum.PROCESSING.getCode() + StrUtil.COMMA + OrderStatusEnum.COMPLETED.getCode();
    }



    public Long transStartTime() {
        return DateUtil.beginOfDay(DateUtil.parse(this.startTime)).getTime() / 1000;
    }

    public Long transEndTime() {
        return DateUtil.endOfDay(DateUtil.parse(this.endTime)).getTime() / 1000;
    }

}
