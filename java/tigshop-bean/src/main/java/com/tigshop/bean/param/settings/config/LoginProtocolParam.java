package com.tigshop.bean.param.settings.config;

import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录协议配置
 *
 * @author kidd
 * @since 2025/4/8 17:44
 */
@Schema(description = "登录协议配置")
@Data
public class LoginProtocolParam implements ConfigSaveParam {

    @Schema(description = "配置Code  termsOfService:服务协议；privacyPolicy:隐私政策；afterSalesService:售后服务")
    private String code;

    @Schema(description = "展示")
    private Integer show;

    @Schema(description = "协议内容")
    private String content;

}
