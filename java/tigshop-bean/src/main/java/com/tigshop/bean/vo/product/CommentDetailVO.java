package com.tigshop.bean.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 评价管理VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "评价管理参数")
public class CommentDetailVO {
    @Schema(description = "统计")
    private Integer total;

    @Schema(description = "不好的评价")
    private Integer badCount;

    @Schema(description = "好的评价")
    private Integer goodCount;

    @Schema(description = "中评")
    private Integer moderateCount;

    @Schema(description = "展示数量")
    private Integer showCount;
}
