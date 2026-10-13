package com.tigshop.bean.vo.promotion;

import com.tigshop.bean.model.promotion.SeckillItem;

import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 限时秒杀VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Getter
@Setter
@Schema(description = "限时秒杀列表参数")
public class SeckillListVO {

    @Schema(description = "秒杀活动ID")
    private Integer seckillId;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "商品缩略图")
    private String picThumb;

    @Schema(description = "商品图片URL")
    @JsonTranslate(dataType = 7)
    private String picUrl;

    @Schema(description = "大图")
    private String picLarge;

    @Schema(description = "商品名称")
    @JsonTranslate(dataType = 2)
    private String productName;

    @Schema(description = "审核状态")
    private Integer checkStatus;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "供应商ID")
    private Integer suppliersId;

    @Schema(description = "商品类型")
    private Integer productType;

    @Schema(description = "商品序列号")
    private String productSn;

    @Schema(description = "商品价格")
    private BigDecimal productPrice;

    @Schema(description = "市场价（划线价）")
    private BigDecimal marketPrice;

    @Schema(description = "成本价")
    private BigDecimal costPrice;

    @Schema(description = "商品状态")
    private Integer productStatus;

    @Schema(description = "是否精品")
    private Integer isBest;

    @Schema(description = "是否新品")
    private Integer isNew;

    @Schema(description = "是否热销")
    private Integer isHot;

    @Schema(description = "商品库存")
    private Integer productStock;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "商品简介")
    private String productBrief;

    @Schema(description = "SKU价格")
    private String skuPrice;

    @Schema(description = "秒杀价格")
    private String seckillPrice;

    @Schema(description = "原始商品价格")
    private String orgProductPrice;

    @Schema(description = "商品SKU列表")
    private List<?> productSku;

    @Schema(description = "店铺简要信息")
    private Object shopSimple;

    @Schema(description = "秒杀限购数量")
    private Integer seckillLimitNum;

    @Schema(description = "秒杀销量")
    private Integer seckillSales;

    @Schema(description = "秒杀库存")
    private Integer seckillStock;

    @Schema(description = "秒杀数据")
    private SeckillData seckkillData;

    @Schema(description = "SKU ID")
    private Integer skuId;

    @Schema(description = "SKU序列号")
    private String skuSn;

    @Schema(description = "当前用户是否可参与（二期会员标签人群；false 时前台展示秒杀价但置灰「抢购」按钮）")
    private Boolean canJoin;

    @Schema(description = "不可参与提示文案，可参与时为空")
    private String restrictTip;

    @Data
    @Schema(description = "秒杀数据详情")
    public static class SeckillData {
        @Schema(description = "秒杀活动ID")
        private Integer seckillId;

        @Schema(description = "秒杀活动名称")
        @JsonTranslate
        private String seckillName;

        @Schema(description = "秒杀开始时间")
        private String seckillStartTime;

        @Schema(description = "秒杀结束时间")
        private String seckillEndTime;

        @Schema(description = "秒杀限购数量")
        private Integer seckillLimitNum;

        @Schema(description = "商品ID")
        private Integer productId;

        @Schema(description = "店铺ID")
        private Integer shopId;

        @Schema(description = "秒杀商品项")
        private List<SeckillItem> seckillItem;
    }

}
