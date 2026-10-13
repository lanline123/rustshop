package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 商品配置
 *
 * @author kidd
 * @since 2025/4/3 16:22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSettingsVO {

    // *** 商品货币设置 ***

    @ConfigItemField(SettingsEnum.DOLLAR_SIGN)
    @Schema(description = "货币符号")
    private String dollarSign;

    @ConfigItemField(SettingsEnum.DOLLAR_SIGN_CN)
    @Schema(description = "商品货币中文")
    private String dollarSignCn;

    // *** 商品录入设置 ***

    @ConfigItemField(SettingsEnum.SN_PREFIX)
    @Schema(description = "商品货号前缀")
    private String snPrefix;

    @ConfigItemField(SettingsEnum.MARKET_PRICE_RATE)
    @Schema(description = "市场价换算")
    private BigDecimal marketPriceRate;

    // *** 商品信息显示设置 ***

    @ConfigItemField(SettingsEnum.SHOW_SELLED_COUNT)
    @Schema(description = "显示销量; 0-否，1-是")
    private Integer showSelledCount;

    @ConfigItemField(SettingsEnum.SHOW_MARKETPRICE)
    @Schema(description = "显示市场价; 0-否，1-是")
    private Integer showMarketprice;

    // *** 商品评价设置 ***

    @ConfigItemField(SettingsEnum.PRODUCT_COMMENT_ENABLED)
    @Schema(description = "是否开启商品评价; 0-关闭，1-开启")
    private Integer productCommentEnabled;

    @ConfigItemField(SettingsEnum.ANONYMOUS_COMMENT_ENABLED)
    @Schema(description = "是否开启匿名评价; 0-关闭，1-开启")
    private Integer anonymousCommentEnabled;

    @ConfigItemField(SettingsEnum.IS_ENQUIRY)
    @Schema(description = "是否开启询价功能；0-关闭，1-开启")
    private Integer isEnquiry;

    @ConfigItemField(SettingsEnum.BULK_PURCHASE)
    @Schema(description = "是否批量采购；0-隐藏，1-显示")
    private Integer bulkPurchase;

    @ConfigItemField(SettingsEnum.ENABLE_ATTRIBUTE_FILTER)
    @Schema(description = "属性筛选：0表示关闭属性筛选，1表示开启属性筛选")
    private Integer enableAttributeFilter;

    @ConfigItemField(SettingsEnum.PRODUCT_EDIT_STOCK_MODIFY)
    @Schema(description = "商品编辑库存是否可修改；0-否，1-是")
    private Integer productEditStockModify;

    @ConfigItemField(SettingsEnum.SPEC_IMAGE_MODE)
    @Schema(description = "规格图片上传范围；1-仅第一组规格可上传图片，2-全部规格可上传图片")
    private Integer specImageMode;

    // *** 商品名称长度设置 ***

    @ConfigItemField(SettingsEnum.CUSTOM_PRODUCT_NAME_LENGTH)
    @Schema(description = "自定义商品名称长度")
    private Integer customProductNameLength;

}
