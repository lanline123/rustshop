// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.param.settings.config;


import com.tigshop.bean.annotations.settings.ConfigIgnoreField;
import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.dto.shop.StoreExtendedDto;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Tigshop团队
 * @create 2025/8/12 11:37
 */
@Schema(description = "平台门店设置参数")
@Data
public class StoreSelfOperatedSaveParam implements ConfigSaveParam {
    @ConfigItemField(SettingsEnum.STORE_SELF_OPERATED_NAME)
    @Schema(description = "门店名称")
    private String storeSelfOperatedName;
    @ConfigItemField(SettingsEnum.STORE_SELF_OPERATED_LOGO)
    @Schema(description = "门店Logo")
    private String storeSelfOperatedLogo;
    @ConfigItemField(SettingsEnum.STORE_SELF_OPERATED_REGION)
    @Schema(description = "门店地域")
    private String storeSelfOperatedRegion;
    @ConfigItemField(SettingsEnum.STORE_SELF_OPERATED_ADDRESS)
    @Schema(description = "详细地址")
    private String storeSelfOperatedAddress;
    @ConfigItemField(SettingsEnum.STORE_SELF_OPERATED_LONGITUDE)
    @Schema(description = "经度")
    private String storeSelfOperatedLongitude;
    @ConfigItemField(SettingsEnum.STORE_SELF_OPERATED_LATITUDE)
    @Schema(description = "维度")
    private String storeSelfOperatedLatitude;

    @ConfigItemField(SettingsEnum.STORE_SELF_OPERATED_PHONE)
    @Schema(description = "门店电话")
    private String storeSelfOperatedPhone;

    @ConfigItemField(SettingsEnum.STORE_SELF_OPERATED_DESCRIPTION)
    @Schema(description = "门店介绍")
    private String storeSelfOperatedDescription;

    @ConfigItemField(SettingsEnum.STORE_SELF_OPERATED_STATUS)
    @Schema(description = "营业状态 1开业 4暂停运营 10关店")
    private Integer storeSelfOperatedStatus;

    /** 由 {@link #shopContactConfig} 序列化而来，前端无需传 */
    @ConfigItemField(SettingsEnum.STORE_SELF_OPERATED_CONTACT_JSON)
    @Schema(hidden = true)
    private String storeSelfOperatedContactJson;

    /** 由 {@link #shopOpenCloseConfig} 序列化而来，前端无需传 */
    @ConfigItemField(SettingsEnum.STORE_SELF_OPERATED_OPEN_CLOSE_JSON)
    @Schema(hidden = true)
    private String storeSelfOperatedOpenCloseJson;

    /** 由 {@link #shopTips} 序列化而来，前端无需传 */
    @ConfigItemField(SettingsEnum.STORE_SELF_OPERATED_TIPS_JSON)
    @Schema(hidden = true)
    private String storeSelfOperatedTipsJson;

    @ConfigIgnoreField
    @Schema(description = "联系方式")
    private List<StoreExtendedDto.ShopContactConfig> shopContactConfig;

    @ConfigIgnoreField
    @Schema(description = "营业时间")
    private StoreExtendedDto.ShopOpenCloseConfig shopOpenCloseConfig;

    @ConfigIgnoreField
    @Schema(description = "门店标签ID列表（与入驻门店 shopTips 同口径）")
    private List<Integer> shopTips;
}
