package com.tigshop.bean.param.content;

import com.tigshop.bean.model.content.ArticleCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章分类表
 *
 * @author kidd
 * @create 2025/7/4
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "文章分类")
public class ArticleCategoryEditParam extends ArticleCategorySaveParam {

    @NotNull(message = "id 不能为空")
    @Schema(description = "id")
    private Integer articleCategoryId;

    public ArticleCategory createArticleCategory() {
        ArticleCategory articleCategory = super.createArticleCategory();
        articleCategory.setArticleCategoryId(articleCategoryId);
        return articleCategory;
    }

}