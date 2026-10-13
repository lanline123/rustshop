package com.tigshop.bean.param.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 抽奖活动ID参数
 *
 * @author Tigshop团队
 * @create 2024年12月22日
 */
@Data
@Schema(description = "抽奖活动ID参数")
public class LotteryActivityIdParam {

    @NotNull(message = "活动ID不能为空")
    @Schema(description = "活动ID")
    private Integer activityId;
}


