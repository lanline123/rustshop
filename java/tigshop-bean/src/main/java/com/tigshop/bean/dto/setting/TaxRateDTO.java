// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 税率管理DTO
 *
 * @author Tigshop团队
 * @create 2026年01月13日
 */
@Data
@Schema(description = "税率管理DTO")
public class TaxRateDTO {

    @Schema(description = "税率ID(更新时必填)")
    private Integer taxRateId;

    @NotNull(message = "国家和地区ID不能为空")
    @Schema(description = "国家和地区ID")
    private Integer regionId;

    @Schema(description = "国家和地区名称")
    private String regionName;

    @NotNull(message = "税率不能为空")
    @DecimalMin(value = "0", message = "税率不能小于0")
    @DecimalMax(value = "100", message = "税率不能大于100")
    @Schema(description = "税率(百分比)")
    private BigDecimal rate;

    @Schema(description = "是否生效:0-否,1-是")
    private Integer isActive;

    @Schema(description = "备注")
    private String remark;
}
