package com.tigshop.bean.param.content;

import com.tigshop.bean.model.content.ArticleCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static com.tigshop.common.constant.content.ArticleCategoryConstants.ARTICLE_CATEGORY_LENGTH_ERROR;
import static com.tigshop.common.constant.content.ArticleCategoryConstants.ARTICLE_CATEGORY_NAME_IS_NULL;

/**
 * 文章分类表
 *
 * @author kidd
 * @create 2025/7/4
 */
@Schema(description = "文章分类")
@Data
public class ArticleCategorySaveParam {

    @Size(min = 1, max = 30, message = ARTICLE_CATEGORY_LENGTH_ERROR)
    @NotNull(message = ARTICLE_CATEGORY_NAME_IS_NULL)
    @Schema(description = "文章分类名称")
    private String articleCategoryName;

    @Schema(description = "文章分类类型")
    private Integer categoryType;

    @Schema(description = "文章分类关键字")
    private String keywords;

    @Schema(description = "文章分类描述")
    private String description;

    @Schema(description = "文章分类显示顺序")
    private Integer sortOrder;

    @Schema(description = "文章分类父分类ID")
    private Integer parentId;

    public ArticleCategory createArticleCategory() {
        return ArticleCategory.builder()
                .articleCategoryName(articleCategoryName)
                .categoryType(categoryType)
                .keywords(keywords)
                .description(description)
                .sortOrder(sortOrder)
                .parentId(parentId)
                .build();
    }

}