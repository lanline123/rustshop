package com.tigshop.bean.dto.setting;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * @author wzh
 */
@Setter
@Getter
@Schema(description = "物流公司配置列表查询参数")
public class LogisticsCompanyConfigListDTO extends BasePage {
    @Schema(description = "是否启用：1启用，0禁用")
    private Integer isEnabled;

    @Schema(description = "物流服务商类型：1快递鸟，2快递100；不传或非法值不过滤")
    private Integer providerType;
}
