package com.tigshop.bean.dto.wholesale;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 批发设置保存入参
 *
 * <p>CR-20260602-134800：字段名与 config.biz_code / SettingsEnum.bizCode 严格一致，加 wholesale* 前缀。
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Data
@Schema(description = "批发设置保存入参")
public class WholesaleSettingSaveDTO {

    @NotNull(message = "wholesaleEnabled 不能为空")
    @Min(value = 0, message = "wholesaleEnabled 取值 0/1")
    @Schema(description = "批发功能总开关：0=关闭 1=启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer wholesaleEnabled;

    @NotNull(message = "wholesaleAutoRegister 不能为空")
    @Min(value = 0, message = "wholesaleAutoRegister 取值 0/1")
    @Schema(description = "新会员注册自动转批发商：0=否 1=是", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer wholesaleAutoRegister;

    @NotNull(message = "wholesaleDefaultGradeId 不能为空")
    @Min(value = 0, message = "wholesaleDefaultGradeId 不能为负")
    @Schema(description = "自动转批发的默认等级ID：0=未配置；>0 时必须是启用中的等级", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer wholesaleDefaultGradeId;

    @NotNull(message = "wholesaleGivePoints 不能为空")
    @Min(value = 0, message = "wholesaleGivePoints 取值 0/1")
    @Schema(description = "批发订单是否赠送积分：0=否 1=是", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer wholesaleGivePoints;
}
