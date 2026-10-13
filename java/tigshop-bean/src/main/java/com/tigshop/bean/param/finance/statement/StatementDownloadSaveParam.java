package com.tigshop.bean.param.finance.statement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * 对账单保存
 *
 * @author Tigshop项目组
 * @create 2025年08月02日 13:38
 */
@Data
@Schema(description = "对账单下载保存参数")
@RequiredArgsConstructor
public class StatementDownloadSaveParam {
    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "店铺ID")
    private Long shopId;

    @Schema(description = "供应商id")
    private Integer vendorId;

    @Schema(description = "备注")
    private String remark;
}