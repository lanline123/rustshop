package com.tigshop.bean.vo.content;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.bean.model.content.Article;
import com.tigshop.bean.model.content.ArticleCategory;
import com.tigshop.bean.model.product.ProductArticle;
import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.config.TimestampToDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

import static com.tigshop.common.constant.content.ArticleConstants.ARTICLE_TYPE_MAP;

/**
 * 文章表数据传输对象
 *
 * @author kidd
 * @create 2025/7/4
 */
@Schema(description = "文章")
@Data
public class ArticleDetailVO {

    // *** Article ***

    @Schema(description = "自增ID号")
    private Integer articleId;

    @JsonTranslate
    @Schema(description = "文章题目")
    private String articleTitle;

    @Schema(description = "文章编码")
    private String articleSn;

    @Schema(description = "缩略图")
    private String articleThumb;

    @Schema(description = "文章标签")
    private String articleTag;

    @Schema(description = "文章作者")
    private String articleAuthor;

    @Schema(description = "---")
    private Integer articleType;

    @Schema(description = "文章内容")
    @JsonTranslate(dataType = 12)
    private String content;

    @Schema(description = "文章描述")
    private String description;

    @Schema(description = "文章的关键字")
    private String keywords;

    @Schema(description = "是否显示;1显示;0不显示")
    private Integer isShow;

    @Schema(description = "是否热门文章:0,否;1,是")
    private Integer isHot;

    @Schema(description = "是否置顶")
    private Integer isTop;

    @Schema(description = "浏览次数")
    private Integer clickCount;

    @Schema(description = "相关链接")
    private String link;

    // *** ArticleCategory ***

    @Schema(description = "文章分类名称")
    private String articleCategoryName;

    // *** Other ***

    @JsonSerialize(using = TimestampToDateSerializer.class)
    @Schema(description = "文章添加时间")
    private String addTime;

    @Schema(description = "文章类型名称")
    private String articleTypeText;

    @Schema(description = "关联产品ID")
    private List<Integer> productIds;

    @Schema(description = "文章分类ID")
    private List<Integer> articleCategoryId;

    public ArticleDetailVO(Article article, ArticleCategory articleCategory, List<ProductArticle> productArticles) {
        this.articleId = article.getArticleId();
        this.articleTitle = article.getArticleTitle();
        this.articleSn = article.getArticleSn();
        this.articleThumb = article.getArticleThumb();
        this.articleTag = article.getArticleTag();
        this.articleAuthor = article.getArticleAuthor();
        this.articleType = article.getArticleType();
        this.content = article.getContent();
        this.description = article.getDescription();
        this.keywords = article.getKeywords();
        this.isShow = article.getIsShow();
        this.isHot = article.getIsHot();
        this.isTop = article.getIsTop();
        this.clickCount = article.getClickCount();
        this.link = article.getLink();

        if (articleCategory != null) {
            this.articleCategoryName = articleCategory.getArticleCategoryName();
        }

        this.addTime = article.getAddTime().toString();
        this.articleTypeText = ARTICLE_TYPE_MAP.get(this.articleType);
        this.productIds = productArticles.stream().map(ProductArticle::getGoodsId).toList();

    }
}
