package com.tigshop.bean.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 评价回复
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "评价回复")
public class CommentReplyDTO {
    @Schema(description = "评价回复ID")
    private Integer commentId;

    @Schema(description = "回复内容")
    private String content;
}
