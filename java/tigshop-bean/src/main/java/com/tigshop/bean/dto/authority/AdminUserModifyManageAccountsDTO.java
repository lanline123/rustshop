// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.authority;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理员用户修改
 *
 * @author Jayce
 * @create 2024年10月29日 14:27
 */
@Data
@Schema(description = "管理员用户修改")
public class AdminUserModifyManageAccountsDTO {

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String mobile;

    /**
     * 头像
     */
    @Schema(description = "头像")
    private String avatar;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱")
    private String email;

    /**
     * 密码
     */
    @Schema(description = "密码")
    private String password;

    /**
     * 确认密码
     */
    @Schema(description = "确认密码")
    private String pwdConfirm;

    /**
     * 原密码
     */
    @Schema(description = "原密码")
    private String oldPassword;

    /**
     * 验证码
     */
    @Schema(description = "验证码")
    private String code;

    /**
     * 修改类型
     */
    @Schema(description = "修改类型")
    private Integer modifyType;

}
