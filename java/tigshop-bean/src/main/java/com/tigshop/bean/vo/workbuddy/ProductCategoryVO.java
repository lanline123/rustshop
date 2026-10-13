package com.tigshop.bean.vo.workbuddy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 供调用方定位 categoryId 的分类条目。
 *
 * <p>导入要求传数字 categoryId，而调用方只知道分类名称，
 * 故需要一个可按名称检索、可逐级下钻的查询结果。
 */
@Data
@Schema(description = "WorkBuddy商品分类")
public class ProductCategoryVO {

    private Integer categoryId;

    private String categoryName;

    /** 从顶级到本级的完整路径，用于消解同名分类的歧义。 */
    private String path;

    private Integer parentId;

    /** 是否还有下级：为真时应继续下钻，商品通常挂在末级分类。 */
    private boolean hasChildren;
}
