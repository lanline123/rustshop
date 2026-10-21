package com.tigshop.bean.model.vendor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 供应商提现记录
 * @TableName vendor_withdraw
 */
@TableName(value ="vendor_withdraw")
@Data
public class VendorWithdraw {
    /**
     * 提现记录ID
     */
    @TableId(type = IdType.AUTO)
    private Integer vendorWithdrawLogId;

    /**
     * 所属供应商
     */
    private Integer vendorId;

    /**
     * 提现金额
     */
    private BigDecimal amount;


    /**
     * 状态：0待审核，2审核不通过，3完成，4待打款
     */
    private Integer status;

    /**
     * 提现时间
     */
    private Long addTime;

    /**
     * 提现备注
     */
    private String remark;

    /**
     * 提现到账户ID
     */
    private Integer vendorAccountId;

    /**
     * 提现时账户信息数据
     */
    private String accountData;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 打款凭证
     */
    private String paymentVoucher;

    @Schema(description = "提现单号")
    private String withdrawSn;
}