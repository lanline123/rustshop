package com.tigshop.bean.model.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户成长积分表
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户成长积分表")
@TableName(value = "user_growth_points_log")
public class UserGrowthPointsLog {

    @TableId(type = IdType.AUTO)
    @Schema(description = "ID")
    private Integer logId;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "增加或减少的成长值")
    private Integer points;

    @Schema(description = "该操作发生的时间")
    private Long changeTime;

    @Schema(description = "该操作的备注")
    private String changeDesc;

    @Schema(description = "类型，1：增加，2：减少")
    private Integer changeType;
}
