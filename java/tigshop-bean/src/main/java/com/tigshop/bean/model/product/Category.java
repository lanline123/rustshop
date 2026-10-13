// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.model.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
 * 商品分类
 *
 * @author Jayce
 * @create 2024年10月31日 16:17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品分类")
@TableName("category")
public class Category implements Serializable {
    @Schema(description = "自增id")
    @TableId(value = "category_id", type = IdType.AUTO)
    private Integer categoryId;

    @Schema(description = "分类名称")
    @TableField(value = "category_name")
    @JsonTranslate
    private String categoryName;

    @Schema(description = "SEO标识")
    @TableField(value = "seo_identification")
    private String seoIdentification;

    @Schema(description = "关键字")
    @TableField(value = "keywords")
    private String keywords;

    @Schema(description = "描述")
    @TableField(value = "category_desc")
    private String categoryDesc;

    @Schema(description = "父级id")
    @TableField(value = "parent_id")
    private Integer parentId;

    @Schema(description = "排序")
    @TableField(value = "sort_order")
    private Integer sortOrder;

    @Schema(description = "计量单位")
    @TableField(value = "measure_unit")
    private String measureUnit;

    @Schema(description = "是否显示")
    @TableField(value = "is_show")
    private Integer isShow;

    @Schema(description = "seo标题")
    @TableField(value = "seo_title")
    private String seoTitle;

    @Schema(description = "短名称")
    @TableField(value = "short_name")
    private String shortName;

    @Schema(description = "图片")
    @TableField(value = "category_pic")
    private String categoryPic;

    @Schema(description = "图标")
    @TableField(value = "category_ico")
    private String categoryIco;

    @Schema(description = "是否热门")
    @TableField(value = "is_hot")
    private Integer isHot;

    @Schema(description = "搜索关键字")
    @TableField(value = "search_keywords")
    private String searchKeywords;
}
