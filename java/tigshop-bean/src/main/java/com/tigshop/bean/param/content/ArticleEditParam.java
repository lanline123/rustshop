package com.tigshop.bean.param.content;

import com.tigshop.bean.model.content.Article;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章表数据传输对象
 *
 * @author kidd
 * @create 2025/7/4
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "文章")
public class ArticleEditParam extends ArticleSaveParam {

    // *** Article ***

    @NotNull(message = "文章 ID 不能为空")
    @Schema(description = "自增ID号")
    private Integer articleId;

    public Article createArticle() {
        Article article = super.createArticle();
        article.setArticleId(this.articleId);
        return article;
    }

}
