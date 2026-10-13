package com.tigshop.bean.vo.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 抽奖活动用户管理VO
 *
 * @author Tigshop团队
 * @create 2025年01月04日
 */
@Data
@Schema(description = "抽奖活动用户管理VO")
public class LotteryUserManageVO {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Integer userId;

    /**
     * 用户名称
     */
    @Schema(description = "用户名称")
    private String userName;

    /**
     * 头像URL
     */
    @Schema(description = "头像URL")
    private String avatar;

    /**
     * 来源标签名称
     */
    @Schema(description = "来源标签名称")
    private String fromTagName;

    /**
     * 参与次数
     */
    @Schema(description = "参与次数")
    private Integer totalCount;

    /**
     * 最后参与时间
     */
    @Schema(description = "最后参与时间")
    private String lastLotteryTime;
}

