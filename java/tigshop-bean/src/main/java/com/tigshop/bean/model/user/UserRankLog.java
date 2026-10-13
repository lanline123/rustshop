package com.tigshop.bean.model.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 会员等级变更日志表
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "会员等级变更日志表")
@TableName(value = "user_rank_log")
public class UserRankLog implements Serializable {
    @Schema(description = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "会员ID")
    private Integer userId;

    @Schema(description = "会员等级ID")
    private Integer rankId;

    @Schema(description = "会员等级变更类型")
    private Integer rankType;

    @Schema(description = "会员等级变更名称")
    private String rankName;

    @Schema(description = "会员等级变更时间")
    private Long changeTime;

    @Schema(description = "会员等级变更备注")
    private String remark;
}
