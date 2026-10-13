package com.tigshop.bean.query.content;

import cn.hutool.core.util.StrUtil;
import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 文章
 *
 * @author kidd
 * @create 2025/7/4
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "文章")
public class ArticlePageQuery extends BasePage {

    @Schema(description = "分类id")
    private String articleCategoryId;

    @Schema(description = "是否显示")
    private Integer isShow;

    @Schema(description = "是否热门")
    private Integer isHot;

    @Schema(description = "文章ids")
    private String articleIds;

    @Schema(description = "分类sn")
    private String categorySn;

    @Schema(description = "文章ids", hidden = true)
    private List<String> articleIdList;

    @Schema(description = "文章分类ids", hidden = true)
    private List<String> articleCategoryIds;

    public void init() {
        if (StrUtil.isNotBlank(this.articleCategoryId)) {
            this.articleCategoryIds = StrUtil.split(this.articleCategoryId, StrUtil.COMMA);
        }

        if (StrUtil.isNotBlank(this.articleIds)) {
            this.articleIdList = StrUtil.split(this.articleIds, StrUtil.COMMA);
        }
    }
}