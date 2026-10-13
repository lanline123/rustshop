package com.tigshop.bean.param.promotion.buyershow;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author wzh
 */
@Data
@Schema(description = "买家秀评价参数-客户端")
public class BuyerShowCommentClientParam {

    @Schema(description = "上级评论id（一级回复传0）")
    private Integer parentId;

    @NotNull(message = "买家秀信息错误")
    @Schema(description = "买家秀ID")
    private Integer buyerShowId;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "评论图片")
    private String commentPic;

}
