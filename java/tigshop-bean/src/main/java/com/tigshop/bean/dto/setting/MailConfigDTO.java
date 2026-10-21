// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 邮件配置参数
 *
 * @author Jayce
 * @create 2024年11月08日 15:28
 */
@Data
@Schema(description = "邮件配置参数")
public class MailConfigDTO {

    @Schema(description = "邮件服务标识")
    private int mailService;

    @Schema(description = "是否启用SSL")
    private int smtpSsl;

    @Schema(description = "SMTP服务器地址")
    private String smtpHost;

    @Schema(description = "SMTP服务器端口")
    private Integer smtpPort;

    @Schema(description = "SMTP用户名")
    private String smtpUser;

    @Schema(description = "SMTP密码")
    private String smtpPass;

    @Schema(description = "发件人邮箱地址")
    private String smtpMail;

    @Schema(description = "邮件字符集")
    private String mailCharset;

    @Schema(description = "测试邮件接收地址")
    private String testMailAddress;
}