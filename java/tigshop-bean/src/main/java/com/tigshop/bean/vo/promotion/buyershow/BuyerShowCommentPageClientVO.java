package com.tigshop.bean.vo.promotion.buyershow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author wzh
 */
@Data
@Schema(description = "买家秀评论分页信息-客户端")
public class BuyerShowCommentPageClientVO {

    @Schema(description = "买家秀评论ID")
    private Integer buyerShowCommentId;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "评论图片")
    private String commentPic;

    @Schema(description = "评论配图URL数组（由 commentPic 解析，无则为空数组）")
    private List<String> images;

    @Schema(description = "用户名称")
    private String userName;

    @Schema(description = "用户头像")
    private String userPhoto;

    @Schema(description = "用户id（非真实用户为0）")
    private Integer userId;

    @Schema(description = "点赞数")
    private Long likeCount;

    @Schema(description = "当前用户是否点赞（0：否，1：是）")
    private Integer isLike;

    @Schema(description = "是否是作者评论（0：否，1：是）")
    private Integer isAuthor;

    @Schema(description = "是否是自己评论（0：否，1：是）")
    private Integer isMe;

    @Schema(description = "回复数")
    private Long replyCount;

    @Schema(description = "发布时间")
    private String addTime;



}
