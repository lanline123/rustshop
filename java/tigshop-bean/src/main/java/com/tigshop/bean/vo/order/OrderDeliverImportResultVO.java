package com.tigshop.bean.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量发货导入结果VO
 *
 * @author Tigshop团队
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "批量发货导入结果VO")
public class OrderDeliverImportResultVO {

    @Schema(description = "总数量")
    private Integer totalCount;

    @Schema(description = "成功数量")
    private Integer successCount;

    @Schema(description = "失败数量")
    private Integer failCount;

    @Schema(description = "失败详情列表")
    private List<FailDetail> failDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "失败详情")
    public static class FailDetail {

        @Schema(description = "行号")
        private Integer rowNum;

        @Schema(description = "订单编号")
        private String orderSn;

        @Schema(description = "失败原因")
        private String reason;
    }
}
