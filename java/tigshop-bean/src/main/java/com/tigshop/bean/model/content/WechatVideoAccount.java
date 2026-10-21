package com.tigshop.bean.model.content;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 视频号账号
 */
@Data
@TableName("wechat_video_account")
@Schema(description = "视频号账号")
public class WechatVideoAccount {
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Integer id;

    @TableField("finder_id")
    @Schema(description = "视频号ID")
    private String finderId;

    @TableField("add_time")
    @Schema(description = "关联时间")
    private Long addTime;

    @TableField("update_time")
    @Schema(description = "更新时间")
    private Long updateTime;

    @TableField("shop_id")
    @Schema(description = "店铺ID")
    private Integer shopId;
}
