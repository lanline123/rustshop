package com.tigshop.bean.model.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 成长值日志表
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "成长值日志表")
@TableName(value = "rank_growth_log")
public class RankGrowthLog implements Serializable {
    @Schema(description = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "会员ID")
    private Integer userId;

    @Schema(description = "成长类型 1 完成下单 2 退款")
    private Integer type;

    @Schema(description = "成长值")
    private Integer growthPoints;

    @Schema(description = "变化类型：1 增加 2 减少")
    private Integer changeType;

    @Schema(description = "创建时间")
    private Long createTime;
}
