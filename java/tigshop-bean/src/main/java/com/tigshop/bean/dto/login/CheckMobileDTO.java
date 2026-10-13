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
 * @author Tigshop团队
 * @create 2025年03月10日 09:55
 */
@Data
@Schema(description = "校验手机号请求参数")
public class CheckMobileDTO {
    @Schema(description = "手机号码")
    @NotNull(message = "手机号码不能为空")
    private String mobile;

    @Schema(description = "验证码")
    @NotNull(message = "验证码不能为空")
    private String code;
}