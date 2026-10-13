package com.tigshop.bean.query.finance;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 对账单下载分页查询
 *
 * @author Tigshop项目组
 * @create 2025年08月02日 13:21
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "对账单下载分页查询")
public class StatementDownloadListPageQuery extends BasePage {
    @Schema(description = "开始时间")
    private Long startTime;
    @Schema(description = "结束时间")
    private Long endTime;
}