package com.tigshop.bean.param.settings.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import com.tigshop.bean.vo.setting.config.KefuSettingsVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Objects;

/**
 * 客服配置新增参数
 *
 * @author kidd
 * @since 2025/4/3 16:28
 */
@Data
public class KefuSaveParam implements ConfigSaveParam {

    // *** 客服设置 ***

    @ConfigItemField(SettingsEnum.KEFU_TYPE)
    @Schema(description = "客服选择；0-无客服，1-腾讯云客服，2-企业微信客服，3-自定义客服，4-TigChart客服")
    private Integer kefuType;

    @ConfigItemField(SettingsEnum.KEFU_YZF_TYPE)
    @Schema(description = "腾讯云客服打开方式；0-打开链接地址，1-小弹窗打开")
    private Integer kefuYzfType;

    @ConfigItemField(SettingsEnum.KEFU_YZF_SIGN)
    @Schema(description = "腾讯云客服sign")
    private String kefuYzfSign;

    @ConfigItemField(SettingsEnum.KEFU_WORKWX_ID)
    @Schema(description = "企业微信客服ID")
    private String kefuWorkwxId;

    @ConfigItemField(SettingsEnum.CORP_ID)
    @Schema(description = "企业微信企业ID")
    private String corpId;

    @ConfigItemField(SettingsEnum.KEFU_CODE)
    @Schema(description = "客服链接")
    private String kefuCode;

    @ConfigItemField(SettingsEnum.KEFU_CODE_BLANK)
    @Schema(description = "客服链接打开方式；0-打开链接地址，1-小弹窗打开")
    private Integer kefuCodeBlank;


    // *** 客服信息 ***

    @ConfigItemField(SettingsEnum.KEFU_PHONE)
    @Schema(description = "客服热线电话")
    private String kefuPhone;

    @ConfigItemField(SettingsEnum.KEFU_TIME)
    @Schema(description = "服务时间")
    private String kefuTime;

    public void noUpdate(KefuSettingsVO settings) {
        if (Objects.equals(this.kefuYzfSign, settings.getKefuYzfSign())) {
            this.kefuYzfSign = null;
        }
        if (Objects.equals(this.kefuWorkwxId, settings.getKefuWorkwxId())) {
            this.kefuWorkwxId = null;
        }
        if (Objects.equals(this.corpId, settings.getCorpId())) {
            this.corpId = null;
        }
    }



}
