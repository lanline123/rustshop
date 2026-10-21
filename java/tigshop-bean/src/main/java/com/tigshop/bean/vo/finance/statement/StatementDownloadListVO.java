package com.tigshop.bean.vo.finance.statement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 对账单下载列表返回
 *
 * @author Tigshop项目组
 * @create 2025年08月13日 13:31
 */
@Data
public class StatementDownloadListVO {
    @Schema(description = "申请时间")
    private Long gmtCreate;
    @Schema(description = "开始时间")
    private Long startTime;
    @Schema(description = "结束时间")
    private Long endTime;
}