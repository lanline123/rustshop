package com.tigshop.bean.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 初始化会员成长值DTO
 * @author Tigshop团队
 * @create 2024年12月03日 17:17
 */
@Data
@Schema(description = "初始化会员成长值DTO")
public class GrowUpSettingInitDTO {
    @Schema(description = "订单")
    private Integer buyOrder;

    @Schema(description = "订单号")
    private Integer buyOrderNumber;

    @Schema(description = "订单成长值")
    private Integer buyOrderGrowth;

    @Schema(description = "evpi")
    private Integer evpi;

    @Schema(description = "evpi_growth")
    private Integer evpiGrowth;

    @Schema(description = "绑定手机")
    private Integer bindPhone;

    @Schema(description = "绑定手机成长值")
    private Integer bindPhoneGrowth;
}
