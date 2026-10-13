// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 充值余额VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "充值余额参数")
public class RechargeSettingVO {
    @Schema(description = "充值ID")
    private Integer rechargeId;

    @Schema(description = "充值金额")
    private BigDecimal money;

    @Schema(description = "赠送金额")
    private BigDecimal discountMoney;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "是否展示")
    private Integer isShow;
}