package com.tigshop.bean.vo.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提示对象
 *
 * @author kidd
 * @since 2025/6/19 13:39
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TipsVO {

    @Schema(description = "提示编码")
    private String code;

    @Schema(description = "提示状态")
    private Boolean status;

}
