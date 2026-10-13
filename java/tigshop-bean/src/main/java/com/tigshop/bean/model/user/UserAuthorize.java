package com.tigshop.bean.model.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户第三方授权信息
 * @author Tigshop
 */
@TableName(value = "user_authorize")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户第三方授权信息")
public class UserAuthorize implements Serializable {

    @Schema(description = "会员登录绑定")
    @TableId(type = IdType.AUTO)
    private Integer authorizeId;

    @Schema(description = "绑定平台：1:微信，2:小程序")
    private Integer authorizeType;

    @Schema(description = "用户id")
    private Integer userId;

    @Schema(description = "openid")
    private String openId;

    @Schema(description = "返回数据")
    private String openData;

    @Schema(description = "微信用户名")
    private String openName;

    @Schema(description = "用户头像")
    private String openPhoto;

    @Schema(description = "微信开放平台唯一识别码")
    private String unionid;

    @Schema(description = "授权时间（留作登录时效）")
    private Long addTime;
}

