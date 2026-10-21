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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static com.tigshop.common.constant.setting.MailTemplatesConstants.*;

/**
 * 邮件模板实体类
 *
 * @author Jayce
 * @create 2024年12月24日 11:22
 */
@Data
@Schema(description = "邮件模板实体类")
public class MailTemplatesDTO {
    @Schema(description = "邮件模板自增id")
    @NotNull(message = TEMPLATE_ID_NOT_NULL)
    private Integer templateId;

    @Schema(description = "邮件是否是html格式；0，否；1，是")
    private Short isHtml;

    @Schema(description = "该邮件模板的邮件主题")
    @NotBlank(message = TEMPLATE_SUBJECT_NOT_NULL)
    @Size(max = 100, message = TEMPLATE_SUBJECT_MAX_LENGTH)
    private String templateSubject;

    @Schema(description = "邮件模板的内容")
    @NotBlank(message = TEMPLATE_CONTENT_NOT_NULL)
    private String templateContent;
}