package com.tigshop.bean.param.content;

import cn.hutool.core.collection.CollUtil;
import com.tigshop.bean.model.content.Article;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

import static com.tigshop.common.constant.content.ArticleConstants.ARTICLE_LENGTH_ERROR;
import static com.tigshop.common.constant.content.ArticleConstants.ARTICLE_TITLE_IS_NULL;

/**
 * 文章表数据传输对象
 *
 * @author kidd
 * @create 2025/7/4
 */
@Data
@Schema(description = "文章")
public class ArticleSaveParam {

    // *** Article ***

    @Size(min = 1, max = 150, message = ARTICLE_LENGTH_ERROR) // 根据表中定义的长度
    @NotBlank(message = ARTICLE_TITLE_IS_NULL)
    @Schema(description = "文章名称")
    private String articleTitle;

    @Schema(description = "文章编码")
    private String articleSn;

    @Schema(description = "缩略图")
    private String articleThumb;

    @Schema(description = "文章标签")
    private String articleTag;

    @Schema(description = "文章作者")
    private String articleAuthor;

    @Schema(description = "文章类型")
    private Integer articleType;

    @NotBlank(message = "文章内容不能为空")
    @Schema(description = "文章内容")
    private String content;

    @Schema(description = "文章描述")
    private String description;

    @Schema(description = "文章的关键字")
    private String keywords;

    @Schema(description = "是否显示;1显示;0不显示")
    private Integer isShow;

    @Schema(description = "文章添加时间")
    private String addTime;

    @Schema(description = "是否热门文章:0,否;1,是")
    private Integer isHot;

    @Schema(description = "是否置顶")
    private Integer isTop;

    @Schema(description = "浏览次数")
    private Integer clickCount;

    @Schema(description = "相关链接")
    private String link;

    // *** Other ***

    @NotEmpty(message = "文章分类ID不能为空")
    @Schema(description = "文章分类ID")
    private List<Integer> articleCategoryId;

    @Schema(description = "关联产品ID")
    private List<Integer> productIds;

    public Article createArticle() {
        Integer articleCategoryId =  CollUtil.isNotEmpty(this.articleCategoryId) ? this.articleCategoryId.getLast() : null;
        return Article.builder()
                .articleTitle(this.articleTitle)
                .articleCategoryId(articleCategoryId)
                .articleSn(this.articleSn)
                .articleThumb(this.articleThumb)
                .articleTag(this.articleTag)
                .articleAuthor(this.articleAuthor)
                .articleType(this.articleType)
                .content(this.content)
                .description(this.description)
                .keywords(this.keywords)
                .isShow(this.isShow)
                .addTime(StringUtils.getCurrentTime())
                .isHot(this.isHot)
                .isTop(this.isTop)
                .clickCount(this.clickCount)
                .link(this.link)
                .build();
    }

}
