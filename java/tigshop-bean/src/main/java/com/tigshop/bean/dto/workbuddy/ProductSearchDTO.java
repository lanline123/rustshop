package com.tigshop.bean.dto.workbuddy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * WorkBuddy 商品查询入参。
 *
 * <p>只暴露商家会用到的筛选维度。后台列表 DTO 有 40 余个字段，多数是前台搜索、装修选品、
 * POS 收银台等场景专用，原样开放只会让调用方误用。
 */
@Data
@Schema(description = "WorkBuddy商品查询入参")
public class ProductSearchDTO {

    @Schema(description = "商品名称或货号关键词")
    private String keyword;

    @Schema(description = "查询范围：on_sale/off_sale/pending_audit/audit_failed/recycled/all，缺省为 on_sale")
    private String scope;

    @Schema(description = "商品分类ID")
    private Integer categoryId;

    @Schema(description = "品牌ID")
    private Integer brandId;

    @Schema(description = "商品类型：1实物 2虚拟 3卡密 4付费内容 5预约上门 6预约到店 7实体礼品卡 8电子礼品卡")
    private Integer productType;

    @Schema(description = "价格区间下限")
    private BigDecimal minPrice;

    @Schema(description = "价格区间上限")
    private BigDecimal maxPrice;

    @Schema(description = "库存区间下限")
    private Integer minStock;

    @Schema(description = "库存区间上限，用于筛选临近缺货的商品")
    private Integer maxStock;

    @Schema(description = "销量区间下限")
    private Integer minSales;

    @Schema(description = "销量区间上限")
    private Integer maxSales;

    @Schema(description = "商品创建时间下限（分析工具内部使用）")
    private LocalDateTime addedStartTime;

    @Schema(description = "商品创建时间上限（分析工具内部使用）")
    private LocalDateTime addedEndTime;

    @Schema(description = "排序字段：added/price/stock/sales/sort_order")
    private String sortField;

    @Schema(description = "排序方向：asc 或 desc，缺省为 desc")
    private String sortOrder;

    @Schema(description = "页码，从 1 开始")
    private Integer page;

    @Schema(description = "每页条数，上限 100")
    private Integer size;
}
