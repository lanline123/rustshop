// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.vo.decorate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 装修推广短链
 *
 * @author Tigshop团队
 */
@Data
@Builder
public class DecoratePromoteVO {

    @Schema(description = "H5 短链")
    private String shortLink;

    @Schema(description = "PC 短链")
    private String pcShortLink;

    @Schema(description = "微信短链")
    private String wechatShortLink;

    @Schema(description = "微信短链生成失败原因")
    private String wechatShortLinkError;

    @Schema(description = "小程序二维码（Base64 data URL）")
    private String miniCode;
}
