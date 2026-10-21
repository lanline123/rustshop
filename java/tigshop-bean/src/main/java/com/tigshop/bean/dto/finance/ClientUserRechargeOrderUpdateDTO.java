package com.tigshop.bean.dto.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 充值记录更新数据
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "充值记录更新参数")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientUserRechargeOrderUpdateDTO {
    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "充值的金额")
    private BigDecimal amount;


}
