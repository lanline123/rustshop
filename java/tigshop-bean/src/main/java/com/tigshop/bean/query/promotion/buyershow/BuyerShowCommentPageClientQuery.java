package com.tigshop.bean.query.promotion.buyershow;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author wzh
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "买家秀评论查询参数-客户端")
public class BuyerShowCommentPageClientQuery extends BasePage {

    @Schema(description = "上级评论id（一级回复传0）")
    private Long parentId;

    @Schema(description = "买家秀ID")
    private Long buyerShowId;
}
