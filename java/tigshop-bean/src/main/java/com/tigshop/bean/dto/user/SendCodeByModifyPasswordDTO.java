package com.tigshop.bean.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
/**
 * 修改密码发送短信dto
 *
 * @author Tigshop团队
 * @create 2024年11月19日 10:19
 */
@Data
@Schema(description = "发送手机验证码")
public class SendCodeByModifyPasswordDTO {
    @Schema(description = "手机号")
    private String mobile;
}
