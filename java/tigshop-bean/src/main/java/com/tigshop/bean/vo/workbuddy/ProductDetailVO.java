package com.tigshop.bean.vo.workbuddy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品详情。
 *
 * <p>只取商家会问到的字段。后台详情 DTO 有 90 余个字段，含大量装修、预售、周期购、
 * O2O 服务费等条件编译专属内容，整份转述会淹没有效信息。
 */
@Data
@Schema(description = "WorkBuddy商品详情")
public class ProductDetailVO {

    private Integer productId;

    private String productName;

    private String productSn;

    private BigDecimal price;

    /** 市场价，即前台的划线价。 */
    private BigDecimal marketPrice;

    private Integer stock;

    /** 上下架状态中文名。 */
    private String status;

    /** 审核状态中文名。 */
    private String auditStatus;

    /** 审核未通过时的原因；其余状态为空。 */
    private String auditReason;

    private String productType;

    private String categoryName;

    private String brandName;

    private String brief;

    /** 主图地址。 */
    private String picUrl;

    private Integer sales;

    private Integer sortOrder;

    private boolean recycled;

    /** 单规格商品为空列表。 */
    private List<Sku> skus = new ArrayList<>();

    @Data
    @Schema(description = "商品规格")
    public static class Sku {

        private Integer skuId;

        /** 规格值，如"红色 / XL"。 */
        private String specs;

        private String skuSn;

        private BigDecimal price;

        private Integer stock;
    }
}
