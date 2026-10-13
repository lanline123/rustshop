package com.tigshop.bean.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 人群圈选「指定会员」明细，仅用于 detail 回显。
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "指定会员明细")
public class TargetUserVO {

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "手机号（脱敏，如 138****0000）")
    private String mobile;
}
