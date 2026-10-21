package com.tigshop.bean.query.promotion.recharge;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 充值余额列表分页参数
 *
 * @author kidd
 * @since 2025/4/22 13:47
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "充值余额列表分页参数")
public class RechargeSettingPageQuery extends BasePage {

    @Schema(description = "是否展示")
    private Integer isShow;

}
