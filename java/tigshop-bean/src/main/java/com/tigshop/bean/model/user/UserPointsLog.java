package com.tigshop.bean.model.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_points_log")
@Schema(description = "用户积分表")
public class UserPointsLog implements Serializable {

    @TableId(value = "log_id", type = IdType.AUTO)
    @Schema(description = "积分id")
    private Integer logId;

    @TableField("user_id")
    @Schema(description = "用户id")
    private Integer userId;

    @TableField("points")
    @Schema(description = "增加或减少的消费积分")
    private Integer points;

    @TableField("change_time")
    @Schema(description = "该操作发生的时间")
    private Long changeTime;

    @TableField("change_desc")
    @Schema(description = "该操作的备注")
    private String changeDesc;

    @TableField("change_type")
    @Schema(description = "增加还是减少")
    private Integer changeType;

    @Schema(description = "关联类型; 1-订单")
    private Integer relationType;

    @Schema(description = "关联ID")
    private Integer relationId;
}
