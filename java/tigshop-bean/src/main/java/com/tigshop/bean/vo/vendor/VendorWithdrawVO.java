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

import cn.hutool.json.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 供应商账户表VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "供应商账户表参数")
public class VendorWithdrawVO {
    @Schema(description = "提现时账户信息数据")
    private JSONObject accountData;

    @Schema(description = "提现时间")
    private String addTime;

    @Schema(description = "提现金额")
    private BigDecimal amount;

    @Schema(description = "手续费")
    private BigDecimal handlingFee;

    @Schema(description = "手续费率")
    private BigDecimal handlingRate;

    @Schema(description = "审核备注")
    private String auditRemark;

    @Schema(description = "提现到")
    private Integer vendorAccountId;

    @Schema(description = "提现备注")
    private String remark;

    @Schema(description = "所属供应商")
    private Integer vendorId;

    @Schema(description = "供应商账户表ID")
    private Integer vendorWithdrawLogId;

    @Schema(description = "状态0待审核2审核不通过3完成")
    private Integer status;

    @Schema(description = "状态文案")
    private String statusText;

    @Schema(description = "打款凭证")
    private String paymentVoucher;
}