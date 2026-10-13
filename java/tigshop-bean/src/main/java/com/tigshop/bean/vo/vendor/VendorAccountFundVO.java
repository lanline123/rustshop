// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.vo.vendor;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.common.config.TimestampToDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Tigshop团队
 * @create 2025/7/16 15:30
 */
@Data
@Schema(description = "供应商资金变化参数")
public class VendorAccountFundVO {

    @Schema(description = "供应商Id")
    private Integer vendorId;

    @Schema(description = "供应商名称")
    private String vendorName;

    @Schema(description = "供应商logo")
    private String vendorLogo;

    @Schema(description = "供应商状态 1开启 2关闭")
    private Integer status;

    @Schema(description = "供应商资金")
    private BigDecimal vendorMoney;

    @Schema(description = "冻结资金")
    private BigDecimal frozenMoney;

    @Schema(description = "供应商待结算订单")
    private BigDecimal unSettlementMoney;

    @JsonSerialize(using = TimestampToDateSerializer.class)
    @Schema(description = "添加时间")
    private String addTime;

}
