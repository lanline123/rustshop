package com.tigshop.bean.dto.vendor;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 供应商提现汇总列表查询参数
 *
 * @author Tigshop团队
 */
@Setter
@Getter
@Schema(description = "供应商提现汇总列表查询参数")
public class VendorWithdrawSummaryListDTO extends BasePage {

    @Schema(description = "供应商名称")
    private String vendorName;

    @Schema(description = "供应商状态：1开启 2关闭")
    private Integer status;

    @Schema(description = "新增开始时间")
    private String addTimeStart;

    @Schema(description = "新增结束时间")
    private String addTimeEnd;

    @Schema(description = "提现开始时间")
    private String withdrawTimeStart;

    @Schema(description = "提现结束时间")
    private String withdrawTimeEnd;
}
