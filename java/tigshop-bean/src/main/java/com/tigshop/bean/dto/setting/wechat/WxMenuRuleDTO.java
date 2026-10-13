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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 微信菜单规则DTO（click/view/miniprogram 字段联合，oneOf 校验由 Service 侧手写）
 *
 * @author Tigshop团队
 * @create 2026年04月21日
 */
@Data
@Schema(description = "微信菜单规则")
public class WxMenuRuleDTO {

    @Schema(description = "规则类型：click=发送消息（微信 click），view=跳转网页，miniprogram=小程序")
    @NotBlank(message = "规则类型不能为空")
    @Pattern(regexp = "^(click|view|miniprogram)$", message = "规则类型只能是 click/view/miniprogram")
    private String type;

    @Schema(description = "type=click 的微信回调 EventKey。可留空，由服务端 saveAndPublish 时统一分配；非法/重复的旧值会被重新分配，≤128。")
    @Size(max = 128, message = "事件 Key 长度不能超过 128")
    private String eventKey;

    @Schema(description = "type=click 时必填：1=文本 2=图文")
    @Min(value = 1, message = "消息类型取值 1 或 2")
    @Max(value = 2, message = "消息类型取值 1 或 2")
    private Integer msgType;

    @Schema(description = "type=click 且 msgType=1：文本内容，≤600")
    @Size(max = 600, message = "文本内容不能超过 600 字符")
    private String content;

    @Schema(description = "type=click 且 msgType=2：图文标题，≤64")
    @Size(max = 64, message = "图文标题不能超过 64 字符")
    private String newsTitle;

    @Schema(description = "type=click 且 msgType=2：封面图 URL")
    @Size(max = 500, message = "封面图 URL 过长")
    private String newsCoverUrl;

    @Schema(description = "type=click 且 msgType=2：摘要，≤120")
    @Size(max = 120, message = "图文摘要不能超过 120 字符")
    private String newsDescription;

    @Schema(description = "type=click 且 msgType=2：跳转链接")
    @Size(max = 500, message = "跳转链接过长")
    private String newsUrl;

    @Schema(description = "type=view 时必填：http/https 跳转 URL")
    @Size(max = 500, message = "跳转地址长度不能超过 500")
    private String url;

    @Schema(description = "type=miniprogram 时必填：小程序 appid")
    @Size(max = 64, message = "appid 长度不能超过 64")
    private String appid;

    @Schema(description = "type=miniprogram 时必填：小程序路径，形如 pages/xxx/xxx")
    @Size(max = 255, message = "pagepath 长度不能超过 255")
    private String pagepath;

    @Schema(description = "type=miniprogram 时必填：老客户端 fallback 网页")
    @Size(max = 500, message = "备用网页长度不能超过 500")
    private String miniprogramUrl;
}
