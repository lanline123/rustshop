package com.tigshop.bean.query.promotion.groupon;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 拼团商品
 *
 * @author kidd
 * @since 2025/8/29 14:58
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GrouponItemPageQuery extends BasePage {

    @Schema(description = "拼团活动 ID")
    private Long grouponPromotionId;
}
