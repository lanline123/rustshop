// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static com.tigshop.common.constant.product.CategoryConstants.CATEGORY_NAME_NOT_NULL;
import static com.tigshop.common.constant.product.CategoryConstants.CATEGORY_NAME_OVERLENGTH;

/**
 * 商品分类
 *
 * @author Jayce
 * @create 2024年10月31日 16:17
 */
@Data
@Schema(description = "商品分类")
public class CategoryDTO {
    @Schema(description = "自增id")
    private Integer categoryId;

    @Schema(description = "分类名称")
    @NotNull(message = CATEGORY_NAME_NOT_NULL)
    @Size(max = 30, min = 1, message = CATEGORY_NAME_OVERLENGTH)
    private String categoryName;

    @Schema(description = "SEO标识")
    private String seoIdentification;

    @Schema(description = "关键字")
    private String keywords;

    @Schema(description = "描述")
    private String categoryDesc;

    @Schema(description = "父级id")
    private Integer parentId;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "计量单位")
    private String measureUnit;

    @Schema(description = "是否显示")
    private Integer isShow;

    @Schema(description = "seo标题")
    private String seoTitle;

    @Schema(description = "短名称")
    private String shortName;

    @Schema(description = "图片")
    private String categoryPic;

    @Schema(description = "图标")
    private String categoryIco;

    @Schema(description = "是否热门")
    private Integer isHot;

    @Schema(description = "搜索关键字")
    private String searchKeywords;

    @Schema(description = "是否有子类")
    private Long hasChildren;
}
