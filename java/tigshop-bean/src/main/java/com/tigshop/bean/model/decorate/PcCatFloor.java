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
@TableName("pc_cat_floor")
@Schema(description = "首页分类栏")
public class PcCatFloor {

    @TableId(value = "cat_floor_id", type = IdType.AUTO)
    @Schema(description = "分类抽屉ID")
    private Integer catFloorId;

    @Schema(description ="最终显示的分类名")
    private String catFloorName;

    @Schema(description ="分类ID，JSON格式")
    private String categoryIds;

    @Schema(description ="分类名称，JSON格式")
    private String categoryNames;

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
    private String brandIds;
}
