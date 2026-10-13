package com.tigshop.bean.bo.salesman;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分销设置条件
 *
 * @author kidd
 * @since 2025/6/21 09:21
 */
@Data
public class SalesmanConfigConditionBO {

    @Schema(description = "自购金额条件")
    private ConditionDetail selfBuyAmount;

    @Schema(description = "推广金额条件")
    private ConditionDetail salesAmount;

    @Schema(description = "发展客户数条件")
    private ConditionDetail salesInviteUsers;

    @Data
    @Schema(description = "条件详情DTO")
    public static class ConditionDetail {

        @Schema(description = "是否选中")
        private Boolean checked;

        @Schema(description = "条件值")
        private String value;

        @Schema(description = "条件标题")
        private String title;

        @Schema(description = "单位")
        private String unit;

        @Schema(description = "是否禁用")
        private Boolean disabled; // 此项根据 JSON 数据可选
    }
}
