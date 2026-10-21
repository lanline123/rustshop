package com.tigshop.bean.query.wholesale;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 可选批发商品分页查询入参（admin / 商家选择名下商品加入批发的列表）。
 *
 * <p>语义：列出**未配置批发能力**的**当前操作者名下商品**（平台 admin={@code shopId 0}，
 * 商家=自己店铺商品；CR-20260610-162600 / CD-039 多租户），供选中后调用
 * {@code /adminapi/wholesale/product/add} / {@code /batchAdd} 批量加入批发。
 *
 * <p>需求基准：CR-20260603-1700（W6 之后的 P2 增量；用户原话「新增一个平台商品列表，
 * 用来选择加入到批发商品， 如果已经被选择加入了的商品 不展示」）+ CR-20260610-162600。
 *
 * @author Tigshop团队
 * @create 2026-06-03
 */
@Data
@Schema(description = "可选批发商品分页查询入参（当前操作者名下商品 + 剔除已配置批发）")
public class WholesaleSelectableProductPageQuery {

    @Schema(description = "页码，默认1", example = "1")
    private Integer page;

    @Schema(description = "每页条数，默认20，1-100", example = "20")
    private Integer size;

    @Schema(description = "关键词：模糊匹配 productName / productSn / productTsn")
    private String keyword;

    @Schema(description = "分类 ID 过滤")
    private Integer categoryId;

    @Schema(description = "品牌 ID 过滤")
    private Integer brandId;

    @Schema(description = "商品上架状态过滤：1=上架（默认），0=下架；不传 = 1（仅上架）")
    private Integer productStatus;
}
