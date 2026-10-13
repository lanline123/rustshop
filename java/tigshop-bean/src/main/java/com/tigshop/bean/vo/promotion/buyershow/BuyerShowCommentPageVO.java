package com.tigshop.bean.vo.promotion.buyershow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author wzh
 */
@Data
@Schema(description = "买家秀评论列表信息")
public class BuyerShowCommentPageVO {
    @Schema(description = "买家秀ID")
    private Integer buyerShowId;

    @Schema(description = "买家秀评论ID")
    private Integer buyerShowCommentId;

    @Schema(description = "发布时间")
    private String addTime;

    @Schema(description = "发布人")
    private String userName;

    @Schema(description = "是否置顶（0：否，1：是）")
    private Integer isTop;

    @Schema(description = "状态（0：待审核，1：通过，2：拒绝）")
    private Integer status;

    @Schema(description = "是否显示（0：隐藏，1：公开）")
    private Integer isShow;

    @Schema(description = "当前状态（待审核，隐藏，公开）")
    private String statusText;

    @Schema(description = "评论内容")
    private String content;
}
