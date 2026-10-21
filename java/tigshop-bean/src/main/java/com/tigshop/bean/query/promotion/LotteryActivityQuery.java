package com.tigshop.bean.query.promotion;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 抽奖活动查询参数
 *
 * @author Tigshop团队
 * @create 2024年12月16日
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "抽奖活动查询参数")
public class LotteryActivityQuery extends BasePage {

    /**
     * 活动名称(模糊搜索)
     */
    @Schema(description = "活动名称(模糊搜索)")
    private String activityName;

    /**
     * 活动状态:0-未开始,1-进行中,2-已结束
     */
    @Schema(description = "活动状态:0-未开始,1-进行中,2-已结束")
    private Integer status;
}

