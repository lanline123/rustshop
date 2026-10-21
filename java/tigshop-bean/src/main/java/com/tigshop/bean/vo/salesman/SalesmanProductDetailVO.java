package com.tigshop.bean.vo.salesman;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductGallery;
import com.tigshop.bean.model.salesman.SalesmanProduct;
import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.core.BigDecimalSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 客户端分销商品详情
 *
 * @author kidd
 * @since 2025/6/20 17:18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesmanProductDetailVO {

    // *** Product ***

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "商品名称")
    @JsonTranslate(dataType = 2)
    private String productName;

    @Schema(description = "商品编号")
    private String productSn;

    @Schema(description = "商品临时编号")
    private String productTsn;

    @Schema(description = "商品库存")
    private Integer productStock;

    @Schema(description = "商品价格")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal productPrice;

    @Schema(description = "市场价")
    private BigDecimal marketPrice;

    @Schema(description = "运费模板ID")
    private Long shippingTplId;

    @Schema(description = "商品状态")
    private Integer productStatus;

    @Schema(description = "商品类型")
    private Integer productType;

    @Schema(description = "分类ID")
    private Integer categoryId;

    @Schema(description = "品牌ID")
    private Integer brandId;

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
    private String picUrl;

    @Schema(description = "缩略图URL")
    private String picThumb;

    @TableField("pic_original")
    @Schema(description = "原图URL")
    private String picOriginal;

    @Schema(description = "评论标签")
    private String commentTag;

    @Schema(description = "是否包邮")
    private Integer freeShipping;

    @Schema(description = "积分")
    private Integer integral;

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

    // *** ProductGallery ***

    @Schema(description = "商品图片列表")
    private List<ProductGallery> pics;

    // *** SalesmanProduct ***

    @Schema(description = "分销商品")
    private SalesmanProduct salesmanProduct;

    // *** Other ***

    @Schema(description = "最终佣金金额")
    private String finalCommissionPrice;

    public SalesmanProductDetailVO(Product product, List<ProductGallery> productGalleries, SalesmanProduct salesmanProduct, String finalCommissionPrice) {
        BeanUtil.copyProperties(product,  this);
        this.pics = productGalleries;
        this.salesmanProduct = salesmanProduct;
        this.finalCommissionPrice = finalCommissionPrice;

    }
}
