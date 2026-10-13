package com.tigshop.bean.vo.workbuddy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品列表条目。
 *
 * <p>状态、审核状态、商品类型、分类一律以中文名给出，数字码只在需要回传的字段（如 productId）保留。
 * 后台列表接口返回的是数字码且不含分类名与品牌名，翻译若留给上层，摘要层要查库、模型层会翻错，
 * 故在此层一次性做完。
 */
@Data
@Schema(description = "WorkBuddy商品列表条目")
public class ProductSummaryVO {

    private Integer productId;

    private String productName;

    /** 商品货号。 */
    private String productSn;

    private BigDecimal price;

    /** 商品总库存；多规格商品为各规格之和。 */
    private Integer stock;

    /** 上下架状态的中文名，如"出售中""已下架"。 */
    private String status;

    /** 审核状态的中文名，如"审核通过""待审核""审核未通过"。 */
    private String auditStatus;

    /** 商品类型的中文名，如"实物商品""卡密商品"。 */
    private String productType;

    private String categoryName;

    /** 规格数；1 表示单规格商品。 */
    private Integer skuCount;

    private Integer sales;

    /** 是否在回收站中。用于让调用方判断能否彻底删除，免得先删后碰壁。 */
    private boolean recycled;
}
