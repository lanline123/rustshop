// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.param.login;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Tigshop团队
 * @create 2025年03月10日 09:55
 */
@Data
@Schema(description = "校验邮箱·请求参数")
public class CheckEmailParam {
    @Schema(description = "邮箱")
    @NotNull(message = "邮箱不能为空")
    private String email;

    @Schema(description = "验证码")
    @NotNull(message = "验证码不能为空")
    private String code;
}