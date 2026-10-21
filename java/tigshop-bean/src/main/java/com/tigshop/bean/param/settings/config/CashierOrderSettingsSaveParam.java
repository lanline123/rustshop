package com.tigshop.bean.param.settings.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Tigshop项目组
 * @create 2025年10月18日 14:02
 */
@Schema(description = "收银台订单设置新增参数")
@Data
public class CashierOrderSettingsSaveParam implements ConfigSaveParam {
    @ConfigItemField(SettingsEnum.AUTO_ERASE_ZERO)
    @Schema(description = "店内收银自动抹零")
    private Integer autoEraseZero;

    @ConfigItemField(SettingsEnum.AUTO_DEDUCTION_POINTS)
    @Schema(description = "收银积分基于订单自动抵扣")
    private Integer autoDeductionPoints;

    @ConfigItemField(SettingsEnum.DEFAULT_COLLECTION_METHOD)
    @Schema(description = "收银默认收款方式")
    private Integer defaultCollectionMethod;

    @ConfigItemField(SettingsEnum.AUTO_ADD_CART_AFTER_SCAN)
    @Schema(description = "扫码后自动加入购物车：0关闭 1开启")
    private Integer autoAddCartAfterScan;

    @ConfigItemField(SettingsEnum.AUTO_PRINT_ONLINE_ORDER)
    @Schema(description = "线上订单自动打印")
    private Integer autoPrintOnlineOrder;

    @ConfigItemField(SettingsEnum.AUTO_PRINT_OFFLINE_ORDER)
    @Schema(description = "线下订单自动打印")
    private Integer autoPrintOfflineOrder;

    @ConfigItemField(SettingsEnum.SHOP_COMMON_REMARKS)
    @Schema(description = "店铺常用备注列表")
    private List<String> shopCommonRemarks;

    @ConfigItemField(SettingsEnum.POS_PRODUCT_SOURCE)
    @Schema(description = "收银台商品来源：0-全部商品，1-门店自建商品，2-平台分配商品，3-供应商商品（数组，可多选）")
    private List<Integer> posProductSource;
}
