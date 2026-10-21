package com.tigshop.bean.query.promotion.timediscount;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 限时折扣列表
 *
 * @author kidd
 * @create 2025/7/1
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "限时折扣列表参数")
public class TimeDiscountPageQuery extends BasePage {

    @Schema(description = "活动状态")
    private Integer activeState;
}
