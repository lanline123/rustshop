package com.tigshop.bean.vo.content;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文章分类表
 *
 * @author kidd
 * @create 2025/7/4
 */
@Schema(description = "文章分类")
@Data
public class ArticleCategoryVO {

    // *** ArticleCategory ***

    @Schema(description = "主键")
    private Integer articleCategoryId;

    @Schema(description = "文章分类名称")
    private String articleCategoryName;

    @Schema(description = "文章分类编号")
    private String categorySn;

    @Schema(description = "文章分类类型")
    private Integer categoryType;

    @Schema(description = "文章分类关键字")
    private String keywords;

    @Schema(description = "文章分类描述")
    private String description;

    @Schema(description = "文章分类在前台页面的显示顺序,数字越大越靠后")
    private Integer sortOrder;

    @Schema(description = "父节点id，取值于该表article_category_id字段")
    private Integer parentId;

    // *** Other ***

    @Schema(description = "是否有子节点")
    private Integer hasChildren;

}