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

package com.tigshop.bean.feign.wechat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 微信公众号发送模板消息
 *
 * @author Tigshop团队
 * @create 2024年12月17日 15:46
 */
@Data
@Schema(description = "微信公众号发送模板消息")
public class SendTemplateMessageParam {
//    注：url和miniprogram都是非必填字段，若都不传则模板无跳转；若都传，会优先跳转至小程序。
//    开发者可根据实际需要选择其中一种跳转方式即可。当用户的微信客户端版本不支持跳小程序时，将会跳转至url。

    @Schema(description = "接收者（用户）的 openid")
    private String touser;

    @Schema(description = "模板ID")
    private String templateId;

    @Schema(description = "模板跳转链接（海外账号没有跳转能力）")
    private String url;

    @Schema(description = "小程序信息")
    private MiniProgram miniprogram;

    @Schema(description = "消息id")
    private String clientMsgId;

    @Schema(description = "模板内容，不填则下发空模板")
    private Map<String, Keyword> data;

    @Data
    public static class MiniProgram {
        private String appid;
        private String pagepath;
    }

    @Data
    public static class Keyword {
        private String value;
    }
}