package com.tigshop.bean.query.promotion.groupon;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 拼团活动商品分页列表
 *
 * @author kidd
 * @since 2025/8/28 18:07
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GrouponPromotionItemPageQuery extends BasePage {

    @Schema(description = "拼团活动 ID")
    private Long grouponPromotionId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品编号")
    private String productSn;

    @Schema(description = "商品分组 ID")
    private Integer productGroupId;

    @Schema(description = "品牌ID")
    private Integer brandId;

    @Schema(description = "分组商品 ID", hidden = true)
    private List<Integer> groupProductIds;

    @Schema(description = "商品名关键词")
    private String keyword;

}
