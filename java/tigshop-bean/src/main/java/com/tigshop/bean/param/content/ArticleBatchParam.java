package com.tigshop.bean.param.content;

import com.tigshop.bean.dto.common.BatchDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 批量操作
 *
 * @author kidd
 * @create 2025/7/7
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "批量操作")
public class ArticleBatchParam extends BatchDTO {

    @Schema(description = "目标分类")
    private Integer[] targetCat;
}
