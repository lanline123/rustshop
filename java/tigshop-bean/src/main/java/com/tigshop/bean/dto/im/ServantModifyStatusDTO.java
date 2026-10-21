package com.tigshop.bean.dto.im;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 修改状态
 *
 * @author Jayce
 * @create 2024年10月25日 14:15
 */
@Data
@Schema(description = "修改状态")
public class ServantModifyStatusDTO {
    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "客服id")
    private Integer servantId;

    @Schema(description = "店铺id")
    private Integer shopId;
}
