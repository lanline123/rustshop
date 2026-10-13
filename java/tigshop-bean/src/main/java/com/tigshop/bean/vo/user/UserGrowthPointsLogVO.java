package com.tigshop.bean.vo.user;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.bean.model.user.UserGrowthPointsLog;
import com.tigshop.common.config.TimestampToDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 会员成长积分日志
 *
 * @author kidd
 * @since 2025/4/10 10:25
 */
@Data
public class UserGrowthPointsLogVO {

    @TableId(value = "log_id")
    private Integer logId;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "增加或减少的成长值")
    private Integer points;

    @Schema(description = "该操作发生的时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String changeTime;

    @Schema(description = "该操作的备注")
    private String changeDesc;

    @Schema(description = "类型，1：增加，2：减少")
    private Integer changeType;

    @Schema(description = "类型，1：增加，2：减少")
    private String changeTypeName;

    public UserGrowthPointsLogVO(UserGrowthPointsLog log) {
        this.logId = log.getLogId();
        this.userId = log.getUserId();
        this.points = log.getPoints();
        this.changeTime = log.getChangeTime().toString();
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
