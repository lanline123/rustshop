package com.tigshop.bean.dto.user;

import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
/**
 * 用户积分日志 DTO
 * @author Tigshop团队
 */
@Data
@Schema(description = "用户积分日志 DTO")
public class UserPointsLogDTO {

    @Schema(description = "自增 ID 号")
    private Integer logId;

    @Schema(description = "用户id")
    private Integer userId;

    @Schema(description = "增加或减少的消费积分")
    private Integer points;

    @Schema(description = "该操作发生的时间")
    private String changeTime;

    @Schema(description = "该操作的备注")
    @JsonTranslate
    private String changeDesc;

    @Schema(description = "增加还是减少，1：增加，2：减少")
    private Integer changeType;

    @Schema(description = "变更类型名称")
    @JsonTranslate
    private String changeTypeName;

    @Schema(description = "用户名")
    private String username;
}
