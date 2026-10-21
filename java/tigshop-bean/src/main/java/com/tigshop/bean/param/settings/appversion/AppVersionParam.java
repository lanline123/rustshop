package com.tigshop.bean.param.settings.appversion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * app 版本编辑参数
 *
 * @author kidd
 * @since 2025/4/10 17:41
 */
@Data
public class AppVersionParam {
    @Schema(description = "ios  android")
    private String type;

    @Schema(description = "版本号")
    private String version;
}
