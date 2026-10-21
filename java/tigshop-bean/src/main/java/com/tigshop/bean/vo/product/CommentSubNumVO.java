package com.tigshop.bean.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 评论晒单数量VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "评论晒单数量VO")
public class CommentSubNumVO {
    @Schema(description = "待评价")
    private Long awaitComment;

    @Schema(description = "待晒单")
    private Long showPics;

    @Schema(description = "已评价")
    private Long commented;
}
