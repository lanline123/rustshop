package com.tigshop.bean.model.promotion;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 评论点赞表
 *
 * @author wzh
 */
@TableName(value = "comment_like")
@Data
@Schema(description = "评论点赞表")
public class CommentLike {
    @TableId(type = IdType.AUTO)
    @Schema(description = "自增ID")
    private Integer id;

    @Schema(description = "评论ID")
    private Integer commentId;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "发布时间")
    private Long addTime;
}