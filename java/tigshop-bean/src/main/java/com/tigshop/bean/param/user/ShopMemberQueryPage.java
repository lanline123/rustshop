package com.tigshop.bean.param.user;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 店铺会员列表查询参数。
 *
 * <p>shopId：平台端显式传入指定店铺；店铺/门店端不传，服务层取 {@code HeaderUtils.getShopId()}。
 */
@Setter
@Getter
@Schema(description = "店铺会员列表查询参数")
public class ShopMemberQueryPage extends BasePage {

    @Schema(description = "店铺ID（平台端指定；店铺端忽略，取当前登录店铺）")
    private Integer shopId;

    @Schema(description = "客户ID")
    private Integer userId;

    @Schema(description = "用户类型；0-会员，1-游客，未传默认会员")
    private Integer userType;

    @Schema(description = "关键字（模糊匹配昵称/手机号/邮箱）")
    private String keyword;

    @Schema(description = "消费金额下限")
    private BigDecimal orderAmountMin;

    @Schema(description = "消费金额上限")
    private BigDecimal orderAmountMax;

    @Schema(description = "入会时间起（Unix秒）")
    private Long joinTimeStart;

    @Schema(description = "入会时间止（Unix秒）")
    private Long joinTimeEnd;

    @Schema(description = "最近消费时间起（Unix秒）")
    private Long lastConsumeTimeStart;

    @Schema(description = "最近消费时间止（Unix秒）")
    private Long lastConsumeTimeEnd;
}
