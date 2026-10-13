package com.tigshop.bean.query.wholesale;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 批发商品分页查询入参
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Data
@Schema(description = "批发商品分页查询入参")
public class WholesaleProductPageQuery {

    @Schema(description = "页码，默认1", example = "1")
    private Integer page;

    @Schema(description = "每页条数，默认20，1-100", example = "20")
    private Integer size;

    @Schema(description = "商品名/货号 模糊")
    private String keyword;

    @Schema(description = "起批维度筛选：1=SPU 2=SKU")
    private Integer moqDimension;
}
