package com.tigshop.bean.query.promotion.groupon;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 拼团活动订单分页列表查询条件
 *
 * @author kidd
 * @since 2025/8/29 09:58
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GrouponRecordOrderPageQuery extends BasePage {
    @Schema(description = "拼团活动ID")
    private Long grouponPromotionId;

}
