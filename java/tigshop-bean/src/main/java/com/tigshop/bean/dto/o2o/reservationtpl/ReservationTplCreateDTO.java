package com.tigshop.bean.dto.o2o.reservationtpl;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "预约模版创建参数")
public class ReservationTplCreateDTO {
    /**
     * 预约模板ID
     */
    @Schema(description = "预约模板ID")
    private Integer reservationTplId;

    /**
     * 模板名称
     */
    @NotBlank(message = "预约模版名称不能为空")
    @Schema(description = "模板名称")
    private String templateName;

    /**
     * 预约时间类型：0=无需提前,1=需提前N天/小时
     */
    @NotNull(message = "预约时间类型不能为空")
    @Schema(description = "预约时间类型：0=无需提前,1=需提前N天/小时")
    private Integer reservationType;

    /**
     * 需提前预约的数量，仅当reservation_type=1时有效
     */
    @Schema(description = "需提前预约的数量，仅当reservation_type=1时有效")
    private Integer advanceNumber;

    /**
     * 需提前预约的时间单位：d=天,h=小时，仅当reservation_type=1时有效
     */
    @Schema(description = "需提前预约的时间单位：d=天,h=小时，仅当reservation_type=1时有效")
    private String advanceUnit;

    /**
     * 最长允许预约天数
     */
    @Schema(description = "最长允许预约天数")
    private Integer maxDays;

    /**
     * 店铺id
     */
    @NotNull(message = "店铺id不能为空")
    @Schema(description = "店铺id")
    private Integer shopId;

    /**
     * 是否默认
     */
    @NotNull(message = "是否默认不能为空")
    @Schema(description = "是否默认")
    private Integer isDefault;
}
