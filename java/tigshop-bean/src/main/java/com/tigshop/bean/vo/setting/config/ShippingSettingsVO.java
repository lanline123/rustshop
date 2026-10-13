package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.tigshop.bean.enums.setting.SettingsEnum.*;

/**
 * 物流配置
 *
 * @author kidd
 * @since 2025/4/3 15:20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShippingSettingsVO {

    // *** 快递发货设置 ***

    @ConfigItemField(SettingsEnum.DEFAULT_LOGISTICS_NAME)
    @Schema(description = "默认配送名称")
    private String defaultLogisticsName;

    // *** 快递接口设置 ***

    @ConfigItemField(LOGISTICS_PROVIDER)
    @Schema(description = "物流服务商；1-快递鸟，2-快递100")
    private Integer logisticsProvider;

    @ConfigItemField(KDNIAO_API_KEY)
    @Schema(description = "快递鸟API Key")
    private String kdniaoApiKey;

    @ConfigItemField(KDNIAO_BUSINESS_ID)
    @Schema(description = "快递鸟服务ID")
    private String kdniaoBusinessId;

    @ConfigItemField(KUAIDI100_CUSTOMER)
    @Schema(description = "快递100授权码(customer)")
    private String kuaidi100Customer;

    @ConfigItemField(KUAIDI100_KEY)
    @Schema(description = "快递100授权key")
    private String kuaidi100Key;

    @ConfigItemField(KUAIDI100_SECRET)
    @Schema(description = "快递100授权secret（电子面单签名用）")
    private String kuaidi100Secret;

    @ConfigItemField(KUAIDI100_SIID)
    @Schema(description = "快递100云打印机设备码（printType=CLOUD时必填）")
    private String kuaidi100Siid;

    @ConfigItemField(KUAIDI100_PRINT_TYPE)
    @Schema(description = "快递100电子面单打印类型；NON/IMAGE/HTML/CLOUD，默认HTML")
    private String kuaidi100PrintType;

    // *** 发货信息设置 ***

    @ConfigItemField(SENDER)
    @Schema(description = "发货人")
    private String sender;

    @ConfigItemField(MOBILE)
    @Schema(description = "发货人联系方式")
    @Sensitive(type = SensitiveType.PHONE)
    private String mobile;

    @ConfigItemField(PROVINCE_NAME)
    @Schema(description = "发货地省份")
    private String provinceName;

    @ConfigItemField(CITY_NAME)
    @Schema(description = "发货城市")
    private String cityName;

    @ConfigItemField(AREA_NAME)
    @Schema(description = "发货地区")
    private String areaName;

    @ConfigItemField(ADDRESS)
    @Schema(description = "发货详细地址")
    private String address;

    // *** 收货信息设置 ***

    @ConfigItemField(RECEIVE_SENDER)
    @Schema(description = "收货人")
    private String receiveSender;

    @ConfigItemField(RECEIVE_MOBILE)
    @Schema(description = "收货人联系方式")
    @Sensitive(type = SensitiveType.PHONE)
    private String receiveMobile;

    @ConfigItemField(RECEIVE_PROVINCE_NAME)
    @Schema(description = "收货地省份")
    private String receiveProvinceName;

    @ConfigItemField(RECEIVE_CITY_NAME)
    @Schema(description = "收货城市")
    private String receiveCityName;

    @ConfigItemField(RECEIVE_AREA_NAME)
    @Schema(description = "收货地区")
    private String receiveAreaName;

    @ConfigItemField(RECEIVE_ADDRESS)
    @Schema(description = "收货详细地址")
    private String receiveAddress;

    public void encryptData() {
        this.kdniaoApiKey = StringUtils.maskMiddleHalf(this.kdniaoApiKey);
        this.kdniaoBusinessId = StringUtils.maskMiddleHalf(this.kdniaoBusinessId);
        this.kuaidi100Customer = StringUtils.maskMiddleHalf(this.kuaidi100Customer);
        this.kuaidi100Key = StringUtils.maskMiddleHalf(this.kuaidi100Key);
        this.kuaidi100Secret = StringUtils.maskMiddleHalf(this.kuaidi100Secret);
    }
}
