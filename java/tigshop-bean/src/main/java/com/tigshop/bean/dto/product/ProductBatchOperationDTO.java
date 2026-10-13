package com.tigshop.bean.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量操作 入参
 *
 * @author kidd
 * @since 2025/3/24 17:24
 */
@Data
public class ProductBatchOperationDTO {

    @NotEmpty(message = "批量操作主键集合不能为空")
    @Schema(description = "批量操作主键集合")
    private List<Long> ids;

    @NotBlank(message = "批量操作类型不能为空")
    @Schema(description = "批量操作类型；up-上架，down-下架，recycle-回收， audit-审核")
    private String type;
}
