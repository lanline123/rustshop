package com.tigshop.bean.model.o2o;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;


@TableName(value = "reservation_tpl")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Schema(description = "预约模板")
public class ReservationTpl implements Serializable {
    /**
     * 预约模板ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "预约模板ID")
    private Integer reservationTplId;

    /**
     * 模板名称
     */
    @Schema(description = "模板名称")
    private String templateName;

    /**
     * 预约时间类型：0=无需提前,1=需提前N天/小时
     */
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
    @Schema(description = "店铺id")
    private Integer shopId;

    /**
     * 是否默认
     */
    @Schema(description = "是否默认")
    private Integer isDefault;
}