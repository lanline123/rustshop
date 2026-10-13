package com.tigshop.bean.query.promotion.coupon;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 优惠券手动发放会员查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "优惠券手动发放会员查询参数")
public class CouponIssueUserPageQuery extends BasePage {

    @Schema(description = "会员来源：0-全部，1-微信公众号，2-微信小程序，3-H5，4-PC，5-APP")
    private Integer fromTag = 0;
}
