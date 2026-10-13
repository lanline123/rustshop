// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.login;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册验证码请求参数
 *
 * @author Tigshop团队
 * @create 2025年02月28日 16:37
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "注册验证码请求参数")
public class RegisterEmailDTO {
    @Schema(description = "验证码行为")
    private String event;

    @Schema(description = "邮箱")
    @NotNull(message = "邮箱不能为空")
    private String email;

    @Schema(description = "行为验证码提供的加密验签")
    private String verifyToken;
}