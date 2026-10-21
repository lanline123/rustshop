package com.tigshop.bean.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 会员企业认证审核数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "会员企业认证审核参数")
public class UserCompanyAuditDTO {
    @Schema(description ="审核不通过建议")
    private String auditRemark;

    @Schema(description ="会员企业认证ID")
    private Integer id;

    @Schema(description ="审核状态：1 待审核 2 审核通过 3 审核未通过")
    private Integer status;
}
