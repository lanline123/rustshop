// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.setting.wechat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 微信公众号回复内容载荷 VO
 * <p>
 * 与 {@link com.tigshop.bean.dto.setting.wechat.ReplyPayloadDTO} 同构，用于：
 * <ul>
 *   <li>自动回复（M3）接口 GET /adminapi/wechat/autoReply/{subscribe,default} 的出参</li>
 *   <li>关键字回复（M2）VO 内回复内容段（若后续需要嵌入）</li>
 * </ul>
 *
 * @author Tigshop团队
 * @create 2026-04-21
 */
@Data
@Schema(description = "微信公众号回复内容载荷 VO（自动回复 / 关键字回复通用）")
public class ReplyPayloadVO {

    @Schema(description = "消息类型：1=文本 2=图文")
    private Integer msgType;

    @Schema(description = "文本内容（msgType=1 时必填，≤600 字符，支持变量占位符 {nickname}{shopName}{date}）")
    private String content;

    @Schema(description = "图文标题（msgType=2 时必填，≤64 字符）")
    private String newsTitle;

    @Schema(description = "图文封面图 URL（msgType=2 时必填，HTTPS 公网可访问）")
    private String newsCoverUrl;

    @Schema(description = "图文摘要（msgType=2 时必填，≤120 字符）")
    private String newsDescription;

    @Schema(description = "图文跳转链接（msgType=2 时必填，http/https）")
    private String newsUrl;
}
