package com.tigshop.bean.query.content;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author wzh
 */
@Data
public class ArticleCategoryQuery {

    @Schema(description = "父文章分类id（默认一级为0）")
    private Integer parentId;
}
