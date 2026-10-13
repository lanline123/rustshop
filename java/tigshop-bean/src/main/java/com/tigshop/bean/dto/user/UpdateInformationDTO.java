package com.tigshop.bean.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新会员消息
 * @author Tigshop团队
 * @create 2024年12月03日 17:17
 */
@Data
@Schema(description = "更新会员消息")
public class UpdateInformationDTO {
    @Schema(description = "会员生日")
    private String birthday;

    @Schema(description = "会员昵称")
    private String nickname;

    @Schema(description = "会员email")
    private String email;

    @Schema(description = "会员手机号")
    private String mobile;

    @Schema(description = "会员微信图片")
    private String wechatImg;
}
