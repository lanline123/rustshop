package com.tigshop.bean.query.promotion.groupon;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 拼团活动开团记录分页列表查询条件
 *
 * @author kidd
 * @since 2025/8/29 09:58
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GrouponRecordOpenJonPageQuery extends BasePage {

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "拼团状态；1-待成团，2-拼团成功，3-拼团失败")
    private Integer joinStatus;

    @Schema(description = "活动id")
    @NotNull
    private Long grouponPromotionId;
}
