package com.tigshop.bean.model.content;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 文章分类列表
 *
 * @author kidd
 * @create 2025/7/4
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文章分类列表")
@TableName(value ="article_category")
public class ArticleCategory implements Serializable {

    @TableId(type = IdType.AUTO)
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

}