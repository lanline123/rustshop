package com.tigshop.bean.vo.content;

//**---------------------------------------------------------------------+
//** 实体类文件 -- 文章分类
//**---------------------------------------------------------------------+
//** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
//**---------------------------------------------------------------------+
//** 作者：Tigshop团队，yq@tigshop.com
//**---------------------------------------------------------------------+
//** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
//**---------------------------------------------------------------------+

import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 文章分类表
 * @author Tigshop团队
 * @create 2024-09-30 15:16:06
 */
@Schema(description = "文章分类")
@Data
public class ChildrenArticleWithCategoryVO {
    /**
     * id
     */
    @Schema(description = "id")
    private Integer articleCategoryId;

    /**
     * 文章分类名称
     */
    @Schema(description = "文章分类名称")
    @JsonTranslate
    private String articleCategoryName;

    /**
     * 文章分类英文名称
     */
    @Schema(description = "文章分类编号")
    private String categorySn;

    /**
     * 文章分类类型
     */
    @Schema(description = "文章分类类型")
    private String categoryType;

    @Schema(description = "文章分类父分类ID")
    private Integer parentId;

    /**
     * 文章列表
     */
    @Schema(description = "文章列表")
    private List<ArticleVO> articles;

}