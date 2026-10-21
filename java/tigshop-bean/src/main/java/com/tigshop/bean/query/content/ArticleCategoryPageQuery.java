package com.tigshop.bean.query.content;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章分类
 *
 * @author kidd
 * @create 2025/7/4
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "文章分类")
public class ArticleCategoryPageQuery extends BasePage {

    @Schema(description = "父文章分类id")
    private Integer parentId;

}