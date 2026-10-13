// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import static com.tigshop.common.constant.promotion.RechargeSettingConstants.*;

/**
 * 充值余额创建数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "充值余额参数")
public class RechargeSettingCreateDTO {
    @Schema(description = "充值金额")
    @NotNull(message = RECHARGE_MONEY_ID_NOT_NULL)
    private BigDecimal money;

    @Schema(description = "赠送金额")
    @NotNull(message = DISCOUNT_MONEY_ID_NOT_NULL)
    private BigDecimal discountMoney;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "是否展示")
    private Integer isShow;

}
