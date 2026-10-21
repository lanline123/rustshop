package com.tigshop.bean.param.user;

import com.tigshop.bean.dto.common.BatchDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * @author wzh
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserBatchParam extends BatchDTO {

    @Schema(description = "等级id")
    private Integer rankId;

    @Schema(description = "增加的余额")
    private BigDecimal balance;
}
