package com.tigshop.bean.vo.promotion;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.common.config.TimestampToDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 抽奖活动列表VO
 *
 * @author Tigshop团队
 * @create 2024年12月16日
 */
@Data
@Schema(description = "抽奖活动列表VO")
public class LotteryActivityListVO {

    /**
     * 活动ID
     */
    @Schema(description = "活动ID")
    private Integer activityId;

    /**
     * 活动名称
     */
    @Schema(description = "活动名称")
    private String activityName;

    /**
     * 活动状态:0-未开始,1-进行中,2-已结束
     */
    @Schema(description = "活动状态:0-未开始,1-进行中,2-已结束")
    private Integer status;

    /**
     * 活动状态文本
     */
    @Schema(description = "活动状态文本")
    private String statusText;

    /**
     * 参与人数
     */
    @Schema(description = "参与人数")
    private Integer participantCount;

    /**
     * 创建时间(格式:yyyy-MM-dd HH:mm:ss)
     */
    @Schema(description = "创建时间(格式:yyyy-MM-dd HH:mm:ss)")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String createTime;

    /**
     * 开始时间(格式:yyyy-MM-dd HH:mm:ss)
     */
    @Schema(description = "开始时间(格式:yyyy-MM-dd HH:mm:ss)")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String startTime;

    /**
     * 结束时间(格式:yyyy-MM-dd HH:mm:ss)
     */
    @Schema(description = "结束时间(格式:yyyy-MM-dd HH:mm:ss)")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String endTime;

    /**
     * 是否可以编辑
     */
    @Schema(description = "是否可以编辑")
    private Boolean canEdit;

    /**
     * 是否可以删除
     */
    @Schema(description = "是否可以删除")
    private Boolean canDelete;
}

