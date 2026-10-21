// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.setting.wechat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 公众号回复内容载荷（文本或图文），共用于关键字回复与自动回复。
 *
 * <p>条件必填规则（由 Service 层校验，不在注解层处理）：
 * <ul>
 *   <li>msgType=1 时 content 非空</li>
 *   <li>msgType=2 时 newsTitle / newsCoverUrl / newsDescription / newsUrl 全部非空，
 *       且 newsUrl 需为合法 http/https URL</li>
 * </ul>
 *
 * @author Tigshop团队
 * @create 2026年04月21日
 */
@Data
@Schema(description = "公众号回复内容载荷（文本或图文，共用于关键字回复和自动回复）")
public class ReplyPayloadDTO {

    @Schema(description = "消息类型：1=文本 2=图文")
    @NotNull(message = "消息类型不能为空")
    private Integer msgType;

    @Schema(description = "文本内容（msgType=1 时必填），≤600，支持变量 {nickname}{shopName}{date}")
    @Size(max = 600, message = "文本内容长度不能超过600")
    private String content;

    @Schema(description = "图文标题（msgType=2 时必填），≤64")
    @Size(max = 64, message = "图文标题长度不能超过64")
    private String newsTitle;

    @Schema(description = "图文封面图 URL（HTTPS，msgType=2 时必填）")
    @Size(max = 500, message = "封面图URL长度不能超过500")
    private String newsCoverUrl;

    @Schema(description = "图文摘要（msgType=2 时必填），≤120")
    @Size(max = 120, message = "图文摘要长度不能超过120")
    private String newsDescription;

    @Schema(description = "图文跳转链接（msgType=2 时必填）")
    @Size(max = 500, message = "跳转链接长度不能超过500")
    private String newsUrl;
}
