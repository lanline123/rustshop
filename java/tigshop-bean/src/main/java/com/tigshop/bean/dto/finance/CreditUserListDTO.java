package com.tigshop.bean.dto.finance;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 授信会员列表查询DTO
 *
 * @author Tigshop团队
 * @create 2026-01-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "授信会员列表查询参数")
public class CreditUserListDTO extends BasePage implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会员类型：1-个人，2-企业（来自user_company.type）")
    private Integer type;

    @Schema(description = "会员用户名")
    private String username;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
}
