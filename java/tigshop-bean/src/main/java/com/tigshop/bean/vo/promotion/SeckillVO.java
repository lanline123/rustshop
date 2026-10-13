package com.tigshop.bean.vo.promotion;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.bean.enums.promotion.SeckillStatusEnum;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.model.promotion.Seckill;
import com.tigshop.bean.model.promotion.SeckillItem;
import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.config.TimestampToDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 限时秒杀VO
 *
 * @author kidd
 * @create 2025/7/2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "限时秒杀参数")
public class SeckillVO {

    // *** Seckill ***

    @Schema(description = "秒杀活动ID")
    private Integer seckillId;

    @Schema(description = "活动名称")
    private String seckillName;

    @Schema(description = "限购数量")
    private Integer seckillLimitNum;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "店铺ID，平台为0")
    private Integer shopId;

    // *** Product ***

    @Schema(description = "缩略图URL")
    @JsonTranslate(dataType = 7)
    private String picThumb;

    @Schema(description = "商品名称")
    @JsonTranslate(dataType = 2)
    private String productName;

    // *** Other ***

    @Schema(description = "起始时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String seckillStartTime;

    @Schema(description = "结束时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String seckillEndTime;

    @Schema(description = "秒杀状态")
    private String statusName;

    @Schema(description = "秒杀项")
    private List<SeckillItemVO> seckillItem;

    @Schema(description = "适用会员范围：0-全部会员，1-指定标记的会员")
    private Integer memberRange;

    @Schema(description = "选择的会员标记列表")
    private List<Integer> memberMarks;

    @Schema(description = "会员人群圈选（历史结构，无法无损表达 sendUserTarget 时为空）")
    private com.tigshop.bean.dto.user.MemberAudienceDTO memberAudience;

    @Schema(description = "发送会员/参与人群（指定会员+等级+标签三组并集）")
    private com.tigshop.bean.dto.user.SendUserTargetDTO sendUserTarget;

    @Schema(description = "当前用户是否可参与（前台；未登录或后台返回 true）")
    private Boolean canJoin;

    @Schema(description = "不可参与时的受限提示文案，可空")
    private String restrictTip;

    @Data
    public static class SeckillItemVO {

        @Schema(description = "商品id")
        private Integer productId;

        @Schema(description = "活动商品ID")
        private Integer recId;

        @Schema(description = "结束时间")
        private Long seckillEndTime;

        @Schema(description = "关联活动id")
        private Integer seckillId;

        @Schema(description = "秒杀价格")
        private BigDecimal seckillPrice;

        @Schema(description = "活动销量")
        private Integer seckillSales;

        @Schema(description = "开始时间")
        private Long seckillStartTime;

        @Schema(description = "活动商品库存")
        private Integer seckillStock;

        @Schema(description = "商品规格值（JSON）")
        private JSONArray skuData;

        @Schema(description = "规格id")
        private Integer skuId;

        @Schema(description = "货品价格")
        @TableField(value = "sku_price")
        private BigDecimal skuPrice;

        @Schema(description = "货品编码")
        @TableField(value = "sku_sn")
        private String skuSn;

        @Schema(description = "货品库存")
        @TableField(value = "sku_stock")
        private Integer skuStock;

        public SeckillItemVO(SeckillItem item, ProductSku productSku) {
            this.productId = item.getProductId();
            this.recId = item.getRecId();
            this.seckillEndTime = item.getSeckillEndTime();
            this.seckillId = item.getSeckillId();
            this.seckillPrice = item.getSeckillPrice();
            this.seckillSales = item.getSeckillSales();
            this.seckillStartTime = item.getSeckillStartTime();
            this.seckillStock = item.getSeckillStock();
            if (productSku != null) {
                this.skuData = JSON.parseArray(productSku.getSkuData());
                this.skuId = productSku.getSkuId();
                this.skuPrice = productSku.getSkuPrice();
                this.skuSn = productSku.getSkuSn();
                this.skuStock = productSku.getSkuStock();
            }

        }

        public SeckillItemVO(SeckillItem item, Product product, ProductSku productSku) {
            this.productId = item.getProductId();
            this.recId = item.getRecId();
            this.seckillEndTime = item.getSeckillEndTime();
            this.seckillId = item.getSeckillId();
            this.seckillPrice = item.getSeckillPrice();
            this.seckillSales = item.getSeckillSales();
            this.seckillStartTime = item.getSeckillStartTime();
            this.seckillStock = item.getSeckillStock();
            if (productSku != null) {
                this.skuData = JSON.parseArray(productSku.getSkuData());
                this.skuId = productSku.getSkuId();
                this.skuPrice = productSku.getSkuPrice();
                this.skuSn = productSku.getSkuSn();
                this.skuStock = productSku.getSkuStock();
            } else {
                this.skuPrice = product.getProductPrice();
                this.skuStock = product.getProductStock();
            }

        }

    }

    public SeckillVO(Seckill seckill, Product product, List<SeckillItem> seckillItems, Map<Integer, ProductSku> productSkuMap) {
        this.seckillId = seckill.getSeckillId();
        this.seckillName = seckill.getSeckillName();
        this.seckillLimitNum = seckill.getSeckillLimitNum();
        this.productId = seckill.getProductId();
        this.shopId = seckill.getShopId();

        this.picThumb = product.getPicThumb();
        this.productName = product.getProductName();

        this.seckillStartTime = String.valueOf(seckill.getSeckillStartTime());
        this.seckillEndTime = String.valueOf(seckill.getSeckillEndTime());
        this.statusName = SeckillStatusEnum.getStatusName(seckill.getSeckillStartTime(), seckill.getSeckillEndTime());

        this.seckillItem = seckillItems.stream()
                .map(item -> new SeckillVO.SeckillItemVO(item, product, productSkuMap.get(item.getSkuId())))
                .toList();
    }

    public SeckillVO(Seckill seckill, List<SeckillItem> seckillItem) {
        this.seckillId = seckill.getSeckillId();
        this.seckillName = seckill.getSeckillName();
        this.seckillStartTime = String.valueOf(seckill.getSeckillStartTime());
        this.seckillEndTime = String.valueOf(seckill.getSeckillEndTime());
        this.seckillLimitNum = seckill.getSeckillLimitNum();
        this.productId = seckill.getProductId();
        this.shopId = seckill.getShopId();

        this.seckillItem = seckillItem.stream()
                .map(item -> new SeckillItemVO(item, null))
                .toList();
    }

}