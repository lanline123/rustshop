package com.tigshop.bean.vo.workbuddy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品查询结果。
 *
 * <p>带上本次实际生效的查询范围与排序，供摘要如实复述。
 * 调用方传的 scope 可能被缺省值替换，不回显的话，用户以为查的是 A 实际查的是 B。
 */
@Data
@Schema(description = "WorkBuddy商品查询结果")
public class ProductSearchVO {

    private List<ProductSummaryVO> items = new ArrayList<>();

    /** 命中总数，非本页条数。 */
    private Long total;

    private Integer page;

    private Integer size;

    /** 本次实际生效的查询范围中文名，如"出售中"。 */
    private String scope;

    /** 本次实际生效的排序说明，如"按库存升序"；未指定排序时为空。 */
    private String sortedBy;
}
