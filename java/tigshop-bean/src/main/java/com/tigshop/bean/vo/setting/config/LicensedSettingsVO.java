package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 授权许可配置
 *
 * @author kidd
 * @since 2025/4/11 14:37
 */
@Data
@NoArgsConstructor
public class LicensedSettingsVO {

    // *** 授权信息 ***

    @ConfigItemField(value = SettingsEnum.LICENSE)
    @Schema(description = "授权码")
    private String license;

    @ConfigItemField(value = SettingsEnum.LICENSED_TYPE)
    @Schema(description = "授权类型")
    private String licensedType;

    @ConfigItemField(value = SettingsEnum.LICENSED_TYPE_NAME)
    @Schema(description = "授权类型名称")
    private String licensedTypeName;

    @ConfigItemField(value = SettingsEnum.IS_ENTERPRISE)
    @Schema(description = "是否企业用户; 0-否，1-是")
    private Integer isEnterprise;

    @ConfigItemField(value = SettingsEnum.DE_COPYRIGHT)
    @Schema(description = "是否去版权；0-否，1-是")
    private Integer deCopyright;

    @ConfigItemField(value = SettingsEnum.AUTHORIZED_DOMAIN)
    @Schema(description = "授权域名")
    private String authorizedDomain;
}
