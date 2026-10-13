package com.tigshop.bean.vo.user;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.bean.model.user.UserPointsLog;
import com.tigshop.common.config.TimestampToDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * @author kidd
 * @since 2025/3/27 17:07
 */
@Getter
@Setter
public class UserPointsLogVO {

    @Schema(description = "积分id")
    private Integer logId;

    @Schema(description = "用户id")
    private Integer userId;

    @Schema(description = "增加或减少的消费积分")
    private Integer points;

    @Schema(description = "该操作发生的时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String changeTime;

    @Schema(description = "该操作的备注")
    private String changeDesc;

    @Schema(description = "增加还是减少")
    private Integer changeType;

    @Schema(description = "类型，1：增加，2：减少")
    private String changeTypeName;

    public UserPointsLogVO(UserPointsLog log) {
        this.logId = log.getLogId();
        this.userId = log.getUserId();
        this.points = log.getPoints();
        this.changeTime = String.valueOf(log.getChangeTime());
        this.changeDesc = log.getChangeDesc();
        this.changeType = log.getChangeType();

        switch (log.getChangeType()) {
            case 1:
                this.changeTypeName = "增加";
                break;
            case 2:
                this.changeTypeName = "减少";
                break;
            case 99:
                this.changeTypeName = "其他";
                break;
            default:
                this.changeTypeName = "未知";
                break;
        }
    }

}
