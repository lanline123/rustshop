package com.tigshop.bean.model.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 店铺商品分类表model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("shop_product_category")
@Schema(description = "店铺商品分类表")
public class ShopProductCategory {
    @Schema(description = "店铺商品分类表ID")
    @TableId(value = "category_id", type = IdType.AUTO)
    private Integer categoryId;

    @Schema(description = "分类名称")
    @JsonTranslate
    private String categoryName;

    @Schema(description = "所属店铺")
    private Integer shopId;

    @Schema(description = "父级分类")
    private Integer parentId;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "是否显示")
    private Integer isShow;

    @Schema(description = "添加时间")
    private Long addTime;

    @Schema(description = "图片")
    @TableField(value = "category_pic")
    private String categoryPic;
}
