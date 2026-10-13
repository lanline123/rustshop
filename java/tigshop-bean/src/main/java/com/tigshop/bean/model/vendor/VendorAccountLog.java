package com.tigshop.bean.model.vendor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 供应商资金变化
 * @TableName vendor_account_log
 */
@TableName(value ="vendor_account_log")
@Data
public class VendorAccountLog {
    /**
     * 自增ID
     */
    @TableId(type = IdType.AUTO)
    private Integer vendorAccountLogId;

    /**
     * 添加时间
     */
    private Long addTime;

    /**
     * 供应商资金
     */
    private BigDecimal vendorMoney;

    /**
     * 供应商冻结资金
     */
    private BigDecimal frozenMoney;

    /**
     * 资金变动类型，例如1-提现
     */
    private Integer type;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 现供应商资金
     */
    private BigDecimal newVendorMoney;

    /**
     * 现供应商冻结资金
     */
    private BigDecimal newFrozenMoney;

    /**
     * 供应商ID
     */
    private Integer vendorId;
}