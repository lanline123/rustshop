package com.tigshop.bean.query.promotion.groupon;

import com.tigshop.common.core.entity.BasePage;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 拼团分页列表查询条件
 *
 * @author kidd
 * @since 2025/8/28 15:05
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GrouponPromotionPageQuery extends BasePage {

    @Schema(description = "活动名称")
    private String grouponName;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品编号")
    private String productSn;

    @Schema(description = "所属店铺")
    private Integer shopId;

    @Schema(description = "活动状态；1-活动进行中，2-活动已结束，3-活动未开始")
    private Integer status;

    @Schema(description = "搜索状态时间", hidden = true)
    private Long searchStatusTime;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "拼团类型；1-普通团，2-阶梯团")
    private Integer groupType;

    public void queryData() {
        if (this.status != null) {
            this.searchStatusTime = StringUtils.getCurrentTime();
        }
        if (this.startTime != null) {
            this.startTime = StringUtils.dateToTimestampExample(this.startTime).toString();
        }
        if (this.endTime != null) {
            this.endTime = StringUtils.dateToTimestampExample(this.endTime).toString();
        }
        if (this.shopId == null) {
            this.shopId = HeaderUtils.getShopId();
        }
    }

}
