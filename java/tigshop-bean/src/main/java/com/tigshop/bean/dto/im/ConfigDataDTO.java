package com.tigshop.bean.dto.im;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * im配置创建数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Getter
@Setter
@Schema(description = "im配置数据")
public class ConfigDataDTO {
    @Schema(description ="激活状态")
    private boolean activate;

    @Schema(description ="发送文本")
    private boolean sendText;

    @Schema(description ="发送微信")
    private boolean sendWechat;

    @Schema(description ="微信图片")
    private String wechatImage;

    @Schema(description ="回复内容")
    private String replyContent;

}
