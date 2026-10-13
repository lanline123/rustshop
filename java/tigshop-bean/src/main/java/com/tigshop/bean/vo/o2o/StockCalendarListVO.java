package com.tigshop.bean.vo.o2o;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 库存日历管理分页查询返回结果
 *
 * @author Tigshop项目组
 * @create 2025年01月01日
 */
@Data
@Schema(description = "库存日历管理分页查询返回结果")
public class StockCalendarListVO {

    /**
     * 商品信息
     */
    @Schema(description = "商品信息")
    private ProductInfo productInfo;

    /**
     * 门店信息
     */
    @Schema(description = "门店信息")
    private ShopInfo shopInfo;

    /**
     * 日期范围库存列表
     */
    @Schema(description = "日期范围库存列表")
    private List<DateStockSummary> dateStockList;

    /**
     * 商品信息
     */
    @Data
    @Schema(description = "商品信息")
    public static class ProductInfo {

        /**
         * 商品ID
         */
        @Schema(description = "商品ID")
        private Integer productId;

        /**
         * 商品名称
         */
        @Schema(description = "商品名称")
        private String productName;

        /**
         * 商品图片
         */
        @Schema(description = "商品图片")
        private String productImage;

        /**
         * SKU ID
         */
        @Schema(description = "SKU ID")
        private Integer skuId;

        /**
         * 规格值
         */
        @Schema(description = "规格值")
        private String specValue;
    }

    /**
     * 门店信息
     */
    @Data
    @Schema(description = "门店信息")
    public static class ShopInfo {

        /**
         * 门店ID
         */
        @Schema(description = "门店ID")
        private Integer shopId;

        /**
         * 门店名称
         */
        @Schema(description = "门店名称")
        private String shopName;
    }

    /**
     * 日期库存汇总信息
     */
    @Data
    @Schema(description = "日期库存汇总信息")
    public static class DateStockSummary {

        /**
         * 日期
         */
        @Schema(description = "日期，格式：yyyy-MM-dd")
        private String stockDate;

        /**
         * 总库存
         */
        @Schema(description = "总库存")
        private Integer totalStock;

        /**
         * 已用库存
         */
        @Schema(description = "已用库存")
        private Integer usedStock;

        /**
         * 可用库存
         */
        @Schema(description = "可用库存")
        private Integer availableStock;
    }
}