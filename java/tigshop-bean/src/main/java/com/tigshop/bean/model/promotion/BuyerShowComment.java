package com.tigshop.bean.model.promotion;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 买家秀评论
 * &#064;TableName  buyer_show_comment
 *
 * @author wzh
 */
@TableName(value = "buyer_show_comment")
@Data
@Schema(description = "买家秀评论")
public class BuyerShowComment implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "买家秀评论ID")
    private Integer buyerShowCommentId;

    @Schema(description = "上级评论id")
    private Integer parentId;

    @Schema(description = "买家秀ID")
    private Integer buyerShowId;

    @Schema(description = "状态（0：待审核，1：通过，2：拒绝）")
    private Integer status;

    @Schema(description = "是否置顶（0：否，1：是）")
    private Integer isTop;

    @Schema(description = "是否显示（0：隐藏，1：公开）")
    private Integer isShow;

    @Schema(description = "用户id（非真实用户为0）")
    private Integer userId;

    @Schema(description = "用户名称")
    private String userName;

    @Schema(description = "用户头像")
    private String userPhoto;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "评论图片")
    private String commentPic;

    @Schema(description = "发布时间")
    private Long addTime;
}