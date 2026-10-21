// **---------------------------------------------------------------------+
// ** 文件 --
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

import java.util.Map;

/**
 * @author Tigshop团队
 * @create 2025/4/7 9:46
 */
@Data
@Schema(description = "绑定手机号")
public class BindMobileDTO {
    @Schema(description = "手机号码")
    @NotNull(message = "手机号码不能为空")
    private String mobile;
    @Schema(description = "验证码")
    @NotNull(message = "验证码不能为空")
    private String mobileCode;
    @Schema(description = "密码")
    @NotNull(message = "密码不能为空")
    private String password;
    @Schema(description = "第三方数据")
    private Map<String, Object> openData;
    @Schema(description = "推荐人")
    private Integer referrerUserId;
}
