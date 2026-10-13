package com.tigshop.bean.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户等级配置表
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "用户等级配置表")
public class UserGrowConfig {
    @Schema(description = "类型")
    private Integer buyOrder;

    @Schema(description = "值")
    private Integer buyOrderNumber;

    @Schema(description = "值")
    private Integer buyOrderGrowth;

    @Schema(description = "类型")
    private Integer evpi;

    @Schema(description = "值")
    private Integer evpiGrowth;

    @Schema(description = "类型")
    private Integer bindPhone;

    @Schema(description = "值")
    private Integer bindPhoneGrowth;

}
