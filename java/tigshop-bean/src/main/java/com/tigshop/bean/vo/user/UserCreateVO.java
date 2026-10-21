package com.tigshop.bean.vo.user;

import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建用户返回VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "用户创建返回")
public class UserCreateVO {

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "用户名")
    private String username;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "手机号")
    private String mobile;
}
