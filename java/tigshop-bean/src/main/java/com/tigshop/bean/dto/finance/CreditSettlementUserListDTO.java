package com.tigshop.bean.dto.finance;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 账期结算会员列表查询DTO
 *
 * @author Tigshop团队
 * @create 2026-01-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "账期结算会员列表查询参数")
public class CreditSettlementUserListDTO extends BasePage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "搜索关键字（用户名/手机号）")
    private String keyword;
}
