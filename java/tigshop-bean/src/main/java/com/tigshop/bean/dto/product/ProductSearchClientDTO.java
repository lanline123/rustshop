package com.tigshop.bean.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 前台商品搜索列表参数
 *
 * @author Jayce
 * @create 2024年11月20日 15:10
 */
@Getter
@Setter
public class ProductSearchClientDTO {

    @Schema(description = "分类")
    private Integer cat;

    @Schema(description = "品牌，可多个，用逗号组合")
    private String brand;

    @Schema(description = "价格区间")
    private BigDecimal min;

    @Schema(description = "价格区间")
    private BigDecimal max;

    @Schema(description = "关键字")
    private String keyword;

    @Schema(description = "用于前台搜索，sale|price|time")
    private String sort;

    @Schema(description = "排序升降")
    private String order;

    @Schema(description = "page")
    private Integer page;

    @Schema(description = "size")
    private Integer size;

    @Schema(description = "用户当前位置经度；与纬度同时传入时，O2O 搜索按商品选择最近可售门店")
    private BigDecimal longitude;

    @Schema(description = "用户当前位置纬度；与经度同时传入时，O2O 搜索按商品选择最近可售门店")
    private BigDecimal latitude;

    @Schema(description = "活动类型")
    private String intro;

    @Schema(description = "优惠券ID")
    private Integer couponId;

    @Schema(description = "店铺分类ID")
    private Integer shopCategoryId;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "商品状态")
    private Integer productStatus;

    @Schema(description = "属性查询")
    private List<AttrObj> attrs;

    @Schema(description = "收银台商品来源过滤：0-全部商品，1-门店自建商品，2-平台分配商品，3-供应商商品")
    private List<Integer> posProductSource;

    @Schema(description = "是否排除周期购商品")
    private Boolean excludeCycleProduct;

    @Schema(description = "是否仅排除按计划配送的周期购，保留次卡（按需兑换）商品")
    private Boolean excludePlanDeliveryCycleOnly;

    @Schema(description = "是否排除礼品卡商品")
    private Boolean excludeGiftCardProduct;

    @Schema(description = "是否过滤预售商品")
    private Integer isFilterPresaleProduct;

    @Schema(description = "属性查询")
    @Data
    public static class AttrObj{
        @Schema(description = "属性名称")
        private String attrName;
        @Schema(description = "属性值")
        private List<String> attrValue;
    }

}
