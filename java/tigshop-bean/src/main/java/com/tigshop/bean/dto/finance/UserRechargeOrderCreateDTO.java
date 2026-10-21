// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

import static com.tigshop.common.constant.finance.UserRechargeOrderConstants.*;

/**
 * 充值记录创建数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "充值记录参数")
public class UserRechargeOrderCreateDTO {

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "充值的金额")
    private BigDecimal amount;

    @Schema(description = "赠送金额")
    private BigDecimal discountMoney;

    @Schema(description = "订单添加时间")
    private Integer addTime;

    @Schema(description = "支付时间")
    private Integer paidTime;

    @Schema(description = "管理员的备注")
    private String postscript;

    @Schema(description = "状态，0：待支付，1：已支付，2：无效")
    private Integer status;
}
