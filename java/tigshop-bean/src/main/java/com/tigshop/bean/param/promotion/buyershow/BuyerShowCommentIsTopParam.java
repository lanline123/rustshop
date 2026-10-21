package com.tigshop.bean.param.promotion.buyershow;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author wzh
 */
@Data
@Schema(description = "是否置顶买家秀评论参数")
public class BuyerShowCommentIsTopParam {
    @NotNull(message = "买家秀评论ID不能为空")
    @Schema(description = "买家秀评论ID")
    private Integer buyerShowCommentId;

    @NotNull(message = "是否置顶不能为空")
    @Schema(description = "是否置顶（0：否，1：是）")
    private Integer isTop;

}
