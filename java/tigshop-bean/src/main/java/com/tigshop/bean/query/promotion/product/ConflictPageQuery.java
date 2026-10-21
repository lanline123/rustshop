package com.tigshop.bean.query.promotion.product;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 活动冲突列表查询条件
 *
 * @author kidd
 * @since 2025/4/30 09:21
 */
@Schema(description = "活动冲突列表查询条件")
@EqualsAndHashCode(callSuper = true)
@Data
public class ConflictPageQuery extends BasePage {

    private String startTime;

    private String endTime;

    private Integer range;

    private String rangeData;

    private Integer promotionType;
}
