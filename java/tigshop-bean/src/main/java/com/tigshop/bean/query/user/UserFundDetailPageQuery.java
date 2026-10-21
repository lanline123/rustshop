package com.tigshop.bean.query.user;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会员资金明细分页查询条件
 *
 * @author kidd
 * @since 2025/4/10 09:55
 */
@Schema(description = "会员资金明细分页查询条件")
@EqualsAndHashCode(callSuper = true)
@Data
public class UserFundDetailPageQuery extends BasePage {

    @Schema(description = "会员ID")
    private Integer userId;

    @NotNull(message = "来源标签不能为空")
    @Schema(description = "来源标签")
    private Integer fromTag;

    @Schema(description = "关键字")
    private String keyword;
}
