package com.tigshop.bean.dto.wholesale;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批发商品批量添加入参（admin 一次选多个普通商品转为批发商品）。
 *
 * <p>契约见 api-contract §4.3 {@code POST /adminapi/wholesale/product/batchAdd}。
 * 单次最多 50 个，由 {@link Size} 强制；幂等语义见 CD-023（已配置的商品进入 skippedIds，不报错）。
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Data
@Schema(description = "批发商品批量添加入参")
public class WholesaleProductBatchAddDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "商品 ID 列表不能为空")
    @Size(min = 1, max = 50, message = "批量数量必须在 1-50 之间")
    @Schema(description = "商品 ID 数组（1-50 个，元素必须为正整数）",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "[101,102,103]")
    private List<@Positive(message = "productId 必须为正数") Integer> productIds;
}
