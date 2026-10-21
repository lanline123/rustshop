package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 买家秀配置
 *
 * @author wzh
 */
@Schema(description = "买家秀配置")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuyerShowSettingsVO {

    @ConfigItemField(SettingsEnum.BUYER_SHOW_ENABLED)
    @Schema(description = "是否开启买家秀；0-否，1-是")
    private Integer buyerShowEnabled;

    @ConfigItemField(SettingsEnum.BUYER_SHOW_MOBILE_SHOW_NAME)
    @Schema(description = "买家秀手机端展示名称")
    private String buyerShowMobileShowName;

    @ConfigItemField(SettingsEnum.BUYER_SHOW_GUIDANCE_CONTENT)
    @Schema(description = "买家秀评论引导文案")
    private String buyerShowGuidanceContent;

    @ConfigItemField(SettingsEnum.BUYER_SHOW_TEMPLATE_STYLE)
    @Schema(description = "买家秀模板样式：0-双栏列表，1-单栏列表")
    private Integer buyerShowTemplateStyle;
}
