package com.tigshop.bean.vo.product;

import com.tigshop.bean.model.shop.Shop;
import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author wzh
 */
@Data
public class BrandVO {
    @Schema(description = "id")
    private Integer brandId;
    /**
     * 品牌名称
     */
    @Schema(description = "品牌名称")
    @JsonTranslate
    private String brandName;

    /**
     * 品牌英文名称
     */
    @Schema(description = "品牌英文名称")
    private String brandEnName;

    /**
     * 上传的该公司Logo图片
     */
    @Schema(description = "上传的该公司Logo图片")
    private String brandLogo;

    /**
     * 品牌描述
     */
    @Schema(description = "品牌描述")
    private String brandDesc;

    /**
     * 否 /	品牌的网址
     */
    @Schema(description = "否 /	品牌的网址")
    private String siteUrl;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "审核状态:0:待审核,1:审核通过,2:已拒绝")
    private Integer status;

    @Schema(description = "审核状态名称:0:待审核,1:审核通过,2:已拒绝")
    private String statusText;

    @Schema(description = "拒绝原因")
    private String rejectRemark;

    /**
     * 品牌在前台页面的显示顺序,数字越大越靠后
     */
    @Schema(description = "品牌在前台页面的显示顺序,数字越大越靠后")
    private Integer sortOrder;

    /**
     * 该品牌是否显示;0否1显示
     */
    @Schema(description = "该品牌是否显示;0否1显示")
    private Integer isShow;

    /**
     * 品牌分类
     */
    @Schema(description = "品牌分类")
    private String brandType;

    /**
     * 是否热销品牌:0,否;1,是
     */
    @Schema(description = "是否热销品牌:0,否;1,是")
    private Integer brandIsHot;

    /**
     * 首字母
     */
    @Schema(description = "首字母")
    private String firstWord;

    /**
     * (已废弃)
     */
    @Schema(description = "(已废弃)")
    private Integer isStoreBrand;

    /**
     * 审核状态:0,待审核;1,审核通过;2,不通过
     */
    @Schema(description = "审核状态:0,待审核;1,审核通过;2,不通过")
    private Integer checkStatus;


    @Schema(description = "店铺信息")
    private Shop shop;
}
