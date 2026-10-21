// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.param.account;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Tigshop团队
 * @create 2025/8/6 15:16
 */
@Data
@Schema(description = "账号列表查询DTO")
public class UpdateMainAccountParam {
    @Schema(description = "账号id")
    @NotNull(message = "账号id不能为空")
    private Integer adminId;
    @Schema(description = "账号名称")
    @NotBlank(message = "账号名称不能为空")
    private String username;
    @Schema(description = "手机号")
    @NotBlank(message = "手机号不能为空")
    private String mobile;
    @Schema(description = "邮箱")
    private String email;
}
