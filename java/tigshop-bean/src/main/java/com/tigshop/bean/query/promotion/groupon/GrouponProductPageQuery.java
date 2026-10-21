package com.tigshop.bean.query.promotion.groupon;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 拼团活动商品分页列表
 *
 * @author kidd
 * @since 2025/8/28 18:07
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GrouponProductPageQuery extends BasePage {

    @Schema(description = "拼团活动 ID")
    private Long grouponPromotionId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "品牌ID")
    private Integer brandId;
}
