package com.tigshop.bean.query.finance;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分账单列表查询参数
 *
 * @author Tigshop团队
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "分账单列表查询参数")
public class ProfitSharingPageQuery extends BasePage {

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "结算主体类型:1店铺 2供应商(不传默认查店铺)")
    private Integer settlementType;

    @Schema(description = "供应商ID")
    private Integer vendorId;

    @Schema(description = "分账状态 PENDING/PROCESSING/SUCCESS/FAILED 或数字code")
    private String status;

    @Schema(description = "完结状态 UNFINISHED/FINISHED 或数字code")
    private String finishStatus;

    @Schema(description = "开始日期")
    private String startDate;

    @Schema(description = "结束日期")
    private String endDate;
}
