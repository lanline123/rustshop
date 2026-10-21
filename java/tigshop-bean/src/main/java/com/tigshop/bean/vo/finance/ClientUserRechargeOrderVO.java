package com.tigshop.bean.vo.finance;

import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Resource;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 申请记录VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "充值记录参数")
public class ClientUserRechargeOrderVO {
    @Schema(description = "状态")
    private String statusType;

    @Schema(description = "充值的金额")
    private BigDecimal amount;

    @Schema(description = "订单添加时间")
    private String addTime;

    @Schema(description = "添加时间")
    private Long addTimeLong;

    @JsonTranslate(dataType = 0)
    @Schema(description = "管理员的备注")
    private String postscript;

    @Schema(description = "状态，0：待支付，1：已支付，2：无效")
    private Integer status;

    @Schema(description = "类型")
    private String type;

}
