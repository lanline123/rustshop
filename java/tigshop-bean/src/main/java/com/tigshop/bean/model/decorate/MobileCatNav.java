package com.tigshop.bean.model.decorate;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 首页分类栏model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("mobile_cat_nav")
@Schema(description = "首页分类栏")
public class MobileCatNav {

    @TableId(value = "mobile_cat_nav_id", type = IdType.AUTO)
    @Schema(description = "首页分类栏ID")
    private Integer mobileCatNavId;

    @Schema(description ="选择分类")
    private Integer categoryId;

    @Schema(description ="分类颜色（颜色选择器）")
    private String catColor;

    @Schema(description ="推荐子分类（多选，JSON格式）")
    private String childCatIds;

    @Schema(description ="推荐品牌")
    private String brandIds;

    @Schema(description ="是否显示：1显示，0不显示")
    private Integer isShow;

    @Schema(description ="分类banner字符串，使用英文符号'|'分割")
    private String imgUrl;

    @Schema(description ="在页面显示的顺序，数字越大顺序越靠后")
    private Short sortOrder;

    @Schema(description ="自定义分类名")
    private String catNameAlias;
}
