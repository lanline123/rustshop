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
public class AppVersionEditParam {
    @Schema(description = "Android版本号")
    private String androidVersion;

    @Schema(description = "iOS版本号")
    private String iosVersion;

    @Schema(description = "iOS下载链接")
    private String iosLink;

    @Schema(description = "Android下载链接")
    private String androidLink;
}
