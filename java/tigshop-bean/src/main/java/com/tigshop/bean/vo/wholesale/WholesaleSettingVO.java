package com.tigshop.bean.vo.wholesale;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 批发设置 VO
 *
 * <p>CR-20260602-134800（CR-VOS-4）：字段名与 config.biz_code / SettingsEnum.bizCode 严格一致，
 * 加 wholesale* 前缀；defaultGradeName / isMultiShop 为派生字段不带前缀。
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Data
@Schema(description = "批发设置")
public class WholesaleSettingVO {

    @Schema(description = "批发功能总开关：0=关闭 1=启用")
    private Integer wholesaleEnabled;

    @Schema(description = "新会员注册自动转批发商：0=否 1=是")
    private Integer wholesaleAutoRegister;

    @Schema(description = "自动转批发的默认等级ID：0=未配置")
    private Integer wholesaleDefaultGradeId;

    @Schema(description = "批发订单是否赠送积分：0=否 1=是")
    private Integer wholesaleGivePoints;

    @Schema(description = "默认等级名称（联表回填）")
    private String defaultGradeName;

    @Schema(description = "当前是否多店铺环境（前端禁用开关 + 提示）")
    private Boolean isMultiShop;
}
