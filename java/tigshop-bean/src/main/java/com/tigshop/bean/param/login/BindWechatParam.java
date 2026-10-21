package com.tigshop.bean.param.login;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 绑定微信公众号code
 *
 * @author Tigshop项目组
 * @create 2025年08月06日 15:50
 */
@Data
@Schema(description = "绑定微信公众号code")
public class BindWechatParam {
    private String code;
}