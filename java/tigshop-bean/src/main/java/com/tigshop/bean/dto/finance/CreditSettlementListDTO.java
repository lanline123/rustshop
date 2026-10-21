package com.tigshop.bean.dto.finance;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 账期结算历史查询DTO
 *
 * @author Tigshop团队
 * @create 2026-01-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "账期结算历史查询参数")
public class CreditSettlementListDTO extends BasePage implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会员ID")
    private Integer userId;

    @Schema(description = "回款流水单号")
    private String settlementSn;

    @Schema(description = "结算状态：1-已结算，2-已撤销")
    private Integer settlementStatus;

    @Schema(description = "结算开始时间（格式：yyyy-MM-dd）")
    private String settlementTimeStart;

    @Schema(description = "结算结束时间（格式：yyyy-MM-dd）")
    private String settlementTimeEnd;
}
