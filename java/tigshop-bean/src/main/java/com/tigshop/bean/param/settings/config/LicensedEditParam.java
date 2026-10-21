package com.tigshop.bean.param.settings.config;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 软件许可证编辑参数
 *
 * @author kidd
 * @since 2025/6/9 15:09
 */
@Data
public class LicensedEditParam {

    @NotBlank(message = "授权码不能为空")
    @Schema(description = "授权码")
    private String license;

    @NotBlank(message = "授权域名不能为空")
    @Schema(description = "授权域名")
    private String domain;
}
