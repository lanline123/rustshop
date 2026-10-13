package com.tigshop.bean.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 前台提交的邮箱信息
 * @author Tigshop团队
 * @create 2024年12月03日 17:17
 */
@Data
@Schema(description = "前台提交的邮箱信息")
public class CheckEmailModifyDTO {
    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "type")
    private Integer type;

}
