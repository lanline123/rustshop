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

package com.tigshop.bean.model.setting;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 邮件模板实体类
 *
 * @author Jayce
 * @create 2024年12月24日 11:22
 */
@Data
@TableName("mail_templates")
@Schema(description = "邮件模板实体类")
public class MailTemplates {
    @Schema(description = "邮件模板自增id")
    @TableId(type = IdType.AUTO, value = "template_id")
    private Integer templateId;

    @Schema(description = "模板字符串名称，主要用于插件言语包时匹配语言包文件等用途")
    private String templateCode;

    @Schema(description = "邮件是否是html格式；0，否；1，是")
    private Short isHtml;

    @Schema(description = "该邮件模板的邮件主题")
    private String templateSubject;

    @Schema(description = "邮件模板的内容")
    private String templateContent;

    @Schema(description = "最后一次修改模板的时间")
    private Long lastModify;

    @Schema(description = "最近一次发送的时间")
    private Long lastSend;

    @Schema(description = "该邮件模板的邮件类型；共2个类型；magazine，杂志订阅；template，关注订阅")
    private String type;
}