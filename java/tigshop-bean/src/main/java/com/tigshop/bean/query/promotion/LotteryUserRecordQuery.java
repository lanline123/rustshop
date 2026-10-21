package com.tigshop.bean.query.promotion;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 抽奖用户中奖详情查询参数
 *
 * @author Tigshop团队
 * @create 2025年01月04日
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "抽奖用户中奖详情查询参数")
public class LotteryUserRecordQuery extends BasePage {

    /**
     * 活动ID
     */
    @NotNull(message = "活动ID不能为空")
    @Schema(description = "活动ID")
    private Integer activityId;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID")
    private Integer userId;
}

