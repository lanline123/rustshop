package com.tigshop.bean.dto.vendor;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 供应商库存列表 / 出入库单列表查询条件
 *
 * @author Tigshop团队
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class VendorStockDTO extends BasePage {

    @Schema(description = "单据编号")
    private String stockCode;

    @Schema(description = "类型（1=入库，2=出库）")
    private Integer inOrOut;

    @Schema(description = "出入库子类型 11/12/10/21/22/23")
    private Integer type;

    @Schema(description = "开始时间")
    private String beginTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "开始时间戳", hidden = true)
    private Long queryBeginTime;

    @Schema(description = "结束时间戳", hidden = true)
    private Long queryEndTime;

    @Schema(description = "供应商id", hidden = true)
    private Integer vendorId;
}
