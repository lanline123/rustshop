// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.decorate;

import com.tigshop.bean.model.product.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static com.tigshop.common.constant.decorate.PcCatFloorConstants.PC_CAT_FLOOR_ID_NOT_NULL;

/**
  * 首页分类栏更新参数
  * @author Tigshop团队
  * @create 2024年12月03日 17:17
  */
@Data
@Schema(description = "首页分类栏参数")
public class PcCatFloorUpdateDTO {
    @NotNull(message = PC_CAT_FLOOR_ID_NOT_NULL)
    @Schema(description = "分类抽屉ID")
    private Integer catFloorId;

    @Schema(description ="最终显示的分类名")
    private String catFloorName;

    @Schema(description ="分类ID，JSON格式")
    private Integer[] categoryIds;

    @Schema(description ="分类名称，JSON格式")
    private String[] categoryNames;

    @Schema(description ="分类数组详情")
    private CategoryList[] categoryList;

    @Schema(description ="分类ICO图片")
    private String floorIco;

    @Schema(description ="是否热门分类")
    private String hotCat;

    @Schema(description ="是否显示，1为显示，0为不显示")
    private Integer isShow;

    @Schema(description ="排序")
    private Integer sortOrder;

    @Schema(description ="分类ICO图标")
    private String floorIcoFont;

    @Schema(description ="推荐的品牌，JSON格式")
    private Integer[] brandIds;

    //CategoryList的属性
    @Data
    public static class CategoryList {
        @Schema(description ="分类ID")
        private Integer categoryId;
        @Schema(description ="分类名")
        private String categoryName;
    }
}
