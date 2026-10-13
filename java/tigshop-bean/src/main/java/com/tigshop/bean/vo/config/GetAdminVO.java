package com.tigshop.bean.vo.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author kidd
 * @since 2025/6/9 15:26
 */
@Data
public class GetAdminVO {

    @ConfigItemField(SettingsEnum.SHOP_LOGO)
    @Schema(description = "商城LOGO")
    private String shopLogo;

    @ConfigItemField(SettingsEnum.SHOP_NAME)
    @Schema(description = "商城名称")
    private String shopName;

}
