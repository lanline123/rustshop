package com.tigshop.common.core.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页排序项")
public class SortItem {
    @Schema(description = "排序字段")
    private String field;

    @Schema(description = "排序方向：asc 或 desc")
    private String order;
}
