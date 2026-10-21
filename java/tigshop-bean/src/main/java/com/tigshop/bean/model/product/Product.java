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
import java.math.BigDecimal;

/**
 * 商品表
 *
 * @author Jayce
 * @create 2024年11月19日 14:33
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品表")
@TableName("product")
public class Product implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "商品名称")
    @JsonTranslate(dataType = 2)
    private String productName;

    @Schema(description = "SEO标识")
    private String seoIdentification;

    @Schema(description = "商品编号")
    private String productSn;

    @Schema(description = "商品临时编号")
    private String productTsn;

    @Schema(description = "商品库存")
    private Integer productStock;

    @Schema(description = "商品价格")
    private BigDecimal productPrice;

    @Schema(description = "市场价")
    private BigDecimal marketPrice;

    @Schema(description = "运费模板ID")
    private Long shippingTplId;

    @Schema(description = "商品状态")
    private Integer productStatus;

    @Schema(description = "商品类型")
    private Integer productType;

    @Schema(description = "交付模式：1-单次交付（默认），2-多次交付（周期购）")
    private Integer deliveryMode;

    @Schema(description = "销售模式：0-现货销售，1-全款预售，2-定金预售")
    private Integer saleMode;

    @Schema(description = "分类ID")
    private Integer categoryId;

    @Schema(description = "品牌ID")
    private Integer brandId;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "关键词")
    private String keywords;

    @Schema(description = "店铺分类ID")
    private Integer shopCategoryId;

    @Schema(description = "审核状态")
    private Integer checkStatus;

    @Schema(description = "审核原因")
    private String checkReason;

    @Schema(description = "点击次数")
    private Integer clickCount;

    @Schema(description = "商品重量")
    private BigDecimal productWeight;

    @Schema(description = "是否促销")
    private Integer isPromote;

    @Schema(description = "是否促销活动")
    private Integer isPromoteActivity;

    @Schema(description = "促销价格")
    private BigDecimal promotePrice;

    @Schema(description = "促销开始日期")
    private Long promoteStartDate;

    @Schema(description = "促销结束日期")
    private Long promoteEndDate;

    @Schema(description = "秒杀最大数量")
    private Integer seckillMaxNum;

    @Schema(description = "商品简介")
    private String productBrief;

    @Schema(description = "商品描述")
    private String productDesc;

    @Schema(description = "图片URL")
    @JsonTranslate(dataType = 7)
    private String picUrl;

    @Schema(description = "缩略图URL")
    private String picThumb;

    @TableField("pic_original")
    @Schema(description = "原图URL")
    private String picOriginal;

    @Schema(description = "大图")
    private String picLarge;

    @Schema(description = "评论标签")
    private String commentTag;

    @Schema(description = "是否包邮")
    private Integer freeShipping;

    @Schema(description = "积分")
    private Integer integral;

    @Schema(description = "添加时间")
    private Long addTime;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "店铺排序")
    private Integer storeSortOrder;

    @Schema(description = "是否删除")
    private Integer isDelete;

    @Schema(description = "是否精品")
    private Integer isBest;

    @Schema(description = "是否新品")
    private Integer isNew;

    @Schema(description = "是否热销")
    private Integer isHot;

    @Schema(description = "最后更新时间")
    private Long lastUpdate;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "赠送积分")
    private Integer giveIntegral;

    @Schema(description = "等级积分")
    private Integer rankIntegral;

    @Schema(description = "供应商ID")
    private Integer suppliersId;

    @Schema(description = "虚拟销量")
    private Integer virtualSales;

    @Schema(description = "限购数量")
    private Integer limitNumber;

    @Schema(description = "商品保养")
    private String productCare;

    @Schema(description = "相关商品")
    private String productRelated;

    @Schema(description = "商品服务ID")
    private String productServiceIds;

    @Schema(description = "是否支持退货")
    private Integer isSupportReturn;

    @Schema(description = "是否支持货到付款")
    private Integer isSupportCod;

    @Schema(description = "商品视频")
    private String productVideo;

    @Schema(description = "预付价格")
    private BigDecimal prepayPrice;

    @Schema(description = "卡组ID")
    private Integer cardGroupId;

    @Schema(description = "虚拟样品")
    private String virtualSample;

    @Schema(description = "付费内容")
    private String paidContent;

    @Schema(description = "是否无需配送；0-否，1-是")
    private Integer noShipping;

    @Schema(description = "运费类型")
    private Integer fixedShippingType;

    @Schema(description = "固定运费金额")
    private BigDecimal fixedShippingFee;

    @Schema(description = "是否到店自提；0-否，1-是")
    private Integer isShopPickup;

    @Schema(description = "到店自提模板ID")
    private Long shopPickupTplId;

    @Schema(description = "供应商产品ID")
    private Integer vendorProductId;

    @Schema(description = "供应商ID")
    private Integer vendorId;

    @TableField(exist = false)
    @Schema(description = "商品名称")
    private String storeProductName;

    @TableField(exist = false)
    @Schema(description = "店铺商品价格")
    private BigDecimal storeProductPrice;

    @TableField(exist = false)
    @Schema(description = "门店覆盖主图")
    private String storePicUrl;

    @TableField(exist = false)
    @Schema(description = "商品状态；1-上架，0-下架")
    private Integer storeProductStatus;

    @TableField(exist = false)
    @Schema(description = "产品库存")
    private Integer storeProductStock;

    @TableField(exist = false)
    @Schema(description = "卡组ID")
    private Integer storeCardGroupId;

    @Schema(description = "是否物流配送0-否，1-是")
    private Integer isLogistics;
    @Schema(description = "是否门店配送0-否，1-是")
    private Integer isShopDelivery;

    @Schema(description = "预约模板Id")
    private Integer reservationTplId;

    @Schema(description = "礼品卡组id")
    private Integer giftCardGroupsId;

    @Schema(description = "商品单位")
    private String productUnit;

    @Schema(description = "成本价")
    private BigDecimal costPrice;

    @Schema(description = "是否需要重新导入供应商商品")
    private Integer isNeedReImportVendorProduct;

    @Schema(description = "属性模板ID")
    private Integer attributesTplId;

    @Schema(description = "分享信息（JSON：productName/productPrice/picUrl）")
    private String shareInfo;

    @Schema(description = "配送员服务费类型（平台层，仅单规格商品使用）：0=固定金额 1=按比例")
    private Integer deliveryServiceFeeType;

    @Schema(description = "配送员服务费值（平台层，仅单规格商品使用）：固定金额(元)或按比例(%)")
    private BigDecimal deliveryServiceFeeValue;

    @Schema(description = "服务员服务费类型（平台层，仅单规格商品使用）：0=固定金额 1=按比例")
    private Integer serverServiceFeeType;

    @Schema(description = "服务员服务费值（平台层，仅单规格商品使用）：固定金额(元)或按比例(%)")
    private BigDecimal serverServiceFeeValue;
}
