package com.tigshop.bean.query.vendor;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 供应商商品列表查询条件
 *
 * @author kidd
 * @since 2025/7/10 09:40
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class VendorProductPageQuery extends BasePage {

    @Schema(description = "商品ID集合")
    private List<Long> ids;

    @Schema(description = "商品品牌ID")
    private Integer brandId;

    @Schema(description = "商品类目ID")
    private Integer categoryId;

    @Schema(description = "商品状态；0-断供，1-在售")
    private Integer productState;

    @Schema(description = "商品审核状态；0-待审核，1-审核通过，2-审核失败")
    private Integer auditState;

    @Schema(description = "商品是否回收；0-否，1-是")
    private Integer isRecycle;

    @Schema(description = "开始创建时间")
    private String searchStartTime;

    @Schema(description = "结束创建时间")
    private String searchEndTime;

    @Schema(description = "开始创建时间", hidden = true)
    private Long startCreateTime;

    @Schema(description = "结束创建时间", hidden = true)
    private Long endCreateTime;

}
