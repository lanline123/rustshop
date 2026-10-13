package com.tigshop.bean.param.settings.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 自动生成授权数据配置
 *
 * @author kidd
 * @since 2025/4/3 10:53
 */
@Data
public class LicenseSaveParam implements ConfigSaveParam {

    @ConfigItemField(SettingsEnum.ORDER_ID)
    @Schema(description = "订单ID")
    private int orderId;

    /**
     * 以 '|' 分隔的字符串
     */
    @ConfigItemField(SettingsEnum.LICENSED_TYPE)
    @Schema(description = "授权类型")
    private String licensedType;

    /**
     * 以 '|' 分隔的字符串
     */
    @ConfigItemField(SettingsEnum.LICENSED_TYPE_NAME)
    @Schema(description = "授权类型名称")
    private String licensedTypeName;

    @ConfigItemField(SettingsEnum.DE_COPYRIGHT)
    @Schema(description = "去版权")
    private int deCopyright;

    @ConfigItemField(SettingsEnum.IS_ENTERPRISE)
    @Schema(description = "是否企业用户")
    private int isEnterprise;

    /**
     * 以 ',' 分隔的字符串
     */
    @ConfigItemField(SettingsEnum.AUTHORIZED_DOMAIN)
    @Schema(description = "授权域名")
    private String authorizedDomain;

    @ConfigItemField(SettingsEnum.HOLDER)
    @Schema(description = "持有者")
    private String holder;

    @ConfigItemField(SettingsEnum.LICENSED_ID)
    @Schema(description = "订单ID")
    private List<Integer> licensedId;

    @ConfigItemField(SettingsEnum.RELEASE_TIME)
    @Schema(description = "发布时间")
    private String releaseTime;

    @ConfigItemField(SettingsEnum.EXPIRATION_TIME)
    @Schema(description = "过期时间")
    private String expirationTime;

    @ConfigItemField(SettingsEnum.LICENSE)
    @Schema(description = "授权码")
    private String license;

}
