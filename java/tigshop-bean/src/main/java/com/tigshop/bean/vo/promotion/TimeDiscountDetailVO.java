package com.tigshop.bean.vo.promotion;

import com.alibaba.fastjson.JSONArray;
import com.tigshop.bean.enums.promotion.PromotionTimeStatusEnum;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.model.promotion.TimeDiscount;
import com.tigshop.bean.model.promotion.TimeDiscountItem;
import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.TigUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 限时折扣详情VO
 *
 * @author kidd
 * @create 2025/7/1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "限时折扣参数")
public class TimeDiscountDetailVO {

    @Schema(description = "折扣详情")
    private DiscountDetailVO discountInfo;

    public TimeDiscountDetailVO(TimeDiscount timeDiscount, List<TimeDiscountItem> timeDiscountItems, List<Product> products, List<ProductSku> productSkus) {
        this.discountInfo = new DiscountDetailVO(timeDiscount, timeDiscountItems, products, productSkus);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "限时折扣商品信息")
    public static class DiscountDetailVO {

        // *** TimeDiscount ***

        @Schema(description = "折扣id")
        private Integer discountId;

        @Schema(description = "折扣名称")
        private String promotionName;

        @Schema(description = "店铺id")
        private Integer shopId;

        // *** Other ***

        @Schema(description = "开始时间")
        private String startTime;

        @Schema(description = "折扣结束时间")
        private String endTime;

        @Schema(description = "添加时间")
        private String addTime;

        @Schema(description = "状态描述")
        private String statusName;

        @Schema(description = "限时折扣商品详情")
        private List<DiscountItemVO> item;

        @Schema(description = "适用会员范围：0-全部会员，1-指定标记的会员")
        private Integer memberRange;

        @Schema(description = "选择的会员标记列表")
        private List<Integer> memberMarks;

        @Schema(description = "会员人群圈选（历史结构，无法无损表达 sendUserTarget 时为空）")
        private com.tigshop.bean.dto.user.MemberAudienceDTO memberAudience;

        @Schema(description = "发送会员/参与人群（指定会员+等级+标签三组并集）")
        private com.tigshop.bean.dto.user.SendUserTargetDTO sendUserTarget;

        public DiscountDetailVO(TimeDiscount timeDiscount, List<TimeDiscountItem> timeDiscountItems, List<Product> products, List<ProductSku> productSkus) {
            this.discountId = timeDiscount.getDiscountId();
            this.promotionName = timeDiscount.getPromotionName();
            this.shopId = timeDiscount.getShopId();

            this.startTime = TigUtils.handelTime(timeDiscount.getStartTime());
            this.endTime = TigUtils.handelTime(timeDiscount.getEndTime());
            this.addTime = TigUtils.handelTime(timeDiscount.getAddTime());
            this.statusName = PromotionTimeStatusEnum.handleStatusName(timeDiscount.getStartTime(), timeDiscount.getEndTime());

            this.item = timeDiscountItems.stream()
                    .map(item -> {
                        final List<Product> list = products.stream().filter(product -> product.getProductId().equals(item.getProductId())).toList();
                        if(Objects.isNull(list) || list.isEmpty()){
                            return null;
                        }
                        Product currProduct = list.get(0);
                        List<ProductSku> currProductSkus = productSkus.stream().filter(productSku -> productSku.getProductId().equals(item.getProductId())).toList();
                        return new DiscountItemVO(item, currProduct, currProductSkus);
                    }).filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "限时折扣商品item")
    public static class DiscountItemVO {

        // *** TimeDiscountItem ***

        @Schema(description = "item_id")
        private Integer itemId;

        @Schema(description = "折扣id")
        private Integer discountId;

        @Schema(description = "商品id")
        private Integer productId;

        @Schema(description = "折扣类型")
        private Integer discountType;

        @Schema(description = "折扣价值")
        private BigDecimal value;

        // *** Other ***

        @Schema(description = "开始时间")
        private String startTime;

        @Schema(description = "折扣结束时间")
        private String endTime;

        @Schema(description = "商品规格id")
        private List<String> skuIds;

        @Schema(description = "商品信息")
        private ProductInfo product;


        public DiscountItemVO(TimeDiscountItem item, Product currProduct, List<ProductSku> productSkus) {
            this.itemId = item.getItemId();
            this.discountId = item.getDiscountId();
            this.productId = item.getProductId();
            this.discountType = item.getDiscountType();
            this.value = item.getValue();

            this.startTime = TigUtils.handelTime(item.getStartTime());
            this.endTime = TigUtils.handelTime(item.getEndTime());
            this.skuIds = JSONArray.parseArray(item.getSkuIds(), String.class);

            if(currProduct!=null){
                this.product = new ProductInfo(currProduct, productSkus);
            }
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "商品信息")
    public static class ProductInfo {

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
        private BigDecimal productPrice;

        @Schema(description = "市场价（划线价）")
        private BigDecimal marketPrice;

        @Schema(description = "成本价")
        private BigDecimal costPrice;

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
        @JsonTranslate(dataType = 7)
        private String picThumb;

        @Schema(description = "原图URL")
        @JsonTranslate(dataType = 7)
        private String picOriginal;

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
        private cn.hutool.json.JSONArray paidContent;

        // *** Other ***

        @Schema(description = "商品规格")
        private List<ProductSkuInfo> productSku;

        public ProductInfo(Product product, List<ProductSku> productSkus) {
            BeanUtils.copyProperties(product, this);
            this.paidContent = JsonUtil.checkJsonType(product.getPaidContent());

            if(Objects.nonNull(productSkus) && !productSkus.isEmpty()){
                this.productSku = productSkus.stream().map(ProductSkuInfo::new).toList();
            }
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "sku_ids")
    public static class ProductSkuInfo {

        // *** ProductSku ***

        @Schema(description = "规格ID")
        private Integer skuId;

        @Schema(description = "商品ID")
        private Integer productId;

        @Schema(description = "商品规格值")
        private String skuValue;

        @Schema(description = "货品编码")
        private String skuSn;

        @Schema(description = "货品库存")
        private Integer skuStock;

        @Schema(description = "条形码")
        private String skuTsn;

        @Schema(description = "货品价格")
        private BigDecimal skuPrice;

        // *** Other ***

        @Schema(description = "商品规格值（JSON）")
        private cn.hutool.json.JSONArray skuData;

        public ProductSkuInfo(ProductSku sku) {
            this.skuId = sku.getSkuId();
            this.productId = sku.getProductId();
            this.skuValue = sku.getSkuValue();
            this.skuSn = sku.getSkuSn();
            this.skuStock = sku.getSkuStock();
            this.skuTsn = sku.getSkuTsn();
            this.skuPrice = sku.getSkuPrice();

            this.skuData = JsonUtil.fromJson(sku.getSkuData(), cn.hutool.json.JSONArray.class);
        }
    }
}
