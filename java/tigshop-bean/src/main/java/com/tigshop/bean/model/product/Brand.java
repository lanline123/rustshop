package com.tigshop.bean.model.product;

//**---------------------------------------------------------------------+
//** 实体类文件 -- 品牌
//**---------------------------------------------------------------------+
//** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
//**---------------------------------------------------------------------+
//** 作者：Tigshop团队，yq@tigshop.com
//**---------------------------------------------------------------------+
//** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
//**---------------------------------------------------------------------+

import com.baomidou.mybatisplus.annotation.IdType;
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
 * 商品品牌表
 * @author Jayce
 * @create 2024-09-30 15:16:06
 */
@TableName(value ="brand")
@Schema(description = "商品品牌表")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Brand implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
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
}