package com.tigshop.bean.vo.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 我的申请VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "我的申请")
public class MerchantMyApplyVO {
    @Schema(description = "商家入驻申请ID")
    private Integer merchantApplyId;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "类型")
    private Integer type;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "审核时间(秒级时间戳，0 或未审核时为 null/0)")
    private Long auditTime;

    @Schema(description = "审核管理员姓名，未审核 / 系统自动通过 / 管理员已删除时返回空串")
    private String auditAdminName;

}
