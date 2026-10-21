package com.tigshop.bean.query.promotion.coupon;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 优惠券列表
 *
 * @author kidd
 * @create 2025/7/4
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "优惠券列表参数")
public class CouponPageQuery extends BasePage {

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "是否展示")
    private Integer isShow;

    @Schema(description = "有效日期")
    private Integer validDate;

    @Schema(description = "收到")
    private Integer receiveDate;

    @Schema(description = "领取排序")
    private Integer receiveFlag;

    @Schema(description = "状态筛选：1=进行中 2=待生效 3=已失效；不传/空表示不限")
    private Integer status;
}
