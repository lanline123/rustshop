package com.tigshop.bean.query.promotion;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 抽奖活动用户管理查询参数
 *
 * @author Tigshop团队
 * @create 2025年01月04日
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "抽奖活动用户管理查询参数")
public class LotteryUserManageQuery extends BasePage {

    /**
     * 活动ID
     */
    @NotNull(message = "活动ID不能为空")
    @Schema(description = "活动ID")
    private Integer activityId;

    /**
     * 用户名称(模糊搜索)
     */
    @Schema(description = "用户名称(模糊搜索)")
    private String userName;

    /**
     * 最后参与时间-开始时间(格式:yyyy-MM-dd HH:mm:ss)
     */
    @Schema(description = "最后参与时间-开始时间(格式:yyyy-MM-dd HH:mm:ss)")
    private String startTime;

    /**
     * 最后参与时间-结束时间(格式:yyyy-MM-dd HH:mm:ss)
     */
    @Schema(description = "最后参与时间-结束时间(格式:yyyy-MM-dd HH:mm:ss)")
    private String endTime;
}

