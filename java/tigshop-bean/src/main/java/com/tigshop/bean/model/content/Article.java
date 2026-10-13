package com.tigshop.bean.model.content;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 文章分类
 *
 * @author kidd
 * @create 2025/7/4
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("article")
public class Article implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "自增ID号")
    private Integer articleId;

    @JsonTranslate
    @Schema(description = "文章题目")
    private String articleTitle;

    @Schema(description = "该文章的分类,同article_cat的cat_id")
    private Integer articleCategoryId;

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
    private String content;

    @Schema(description = "文章描述")
    private String description;

    @Schema(description = "文章的关键字")
    private String keywords;

    @Schema(description = "是否显示;1显示;0不显示")
    private Integer isShow;

    @Schema(description = "文章添加时间")
    private Long addTime;

    @Schema(description = "是否热门文章:0,否;1,是")
    private Integer isHot;

    @Schema(description = "是否置顶")
    private Integer isTop;

    @Schema(description = "浏览次数")
    private Integer clickCount;

    @Schema(description = "相关链接")
    private String link;
}
