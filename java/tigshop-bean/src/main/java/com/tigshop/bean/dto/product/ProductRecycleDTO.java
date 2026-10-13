package com.tigshop.bean.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 产品回收 入参
 *
 * @author kidd
 * @since 2025/3/25 09:16
 */
@Data
public class ProductRecycleDTO {

    @NotNull(message = "产品主键不能为空")
    @Schema(description = "产品主键")
    private Long id;
}
