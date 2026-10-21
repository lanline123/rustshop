package com.tigshop.bean.dto.salesman;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

import static com.tigshop.common.constant.ResultTextConstants.UNSELECTED_ITEM;

/**
 * 分销业绩人工结算
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "分销业绩人工结算")
public class SalesmanOrderSettlementDTO {

    @Schema(description = "ids")
    @NotEmpty(message = UNSELECTED_ITEM)
    private List<Integer> ids;
}
