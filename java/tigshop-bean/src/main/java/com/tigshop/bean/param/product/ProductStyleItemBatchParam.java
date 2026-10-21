package com.tigshop.bean.param.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "商品样式关联批量操作参数")
@Data
public class ProductStyleItemBatchParam {

    @NotNull(message = "样式ID不能为空")
    @Schema(description = "样式ID")
    private Integer styleId;

    @Schema(description = "商品ID列表")
    private List<Integer> productIds;

    @Schema(description = "分组ID列表")
    private List<Integer> groupIds;

    @Schema(description = "品牌ID列表")
    private List<Integer> brandIds;
}
