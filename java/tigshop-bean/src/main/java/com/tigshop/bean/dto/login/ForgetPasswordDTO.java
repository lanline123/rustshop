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
import lombok.Data;

/**
 * 修改密码
 *
 * @author Tigshop团队
 * @create 2025年03月10日 11:18
 */
@Data
@Schema(description = "修改密码")
public class ForgetPasswordDTO {
    @Schema(description = "加密号码")
    @NotNull(message = "手机号/邮箱不能为空")
    private String mobileKey;

    @Schema(description = "新密码")
    @NotNull(message = "新密码不能为空")
    private String password;
}