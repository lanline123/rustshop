package com.tigshop.bean.model.vendor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 供应商账户表
 * @TableName vendor_account
 */
@TableName(value ="vendor_account")
@Data
public class VendorAccount {
    /**
     * 账号ID
     */
    @TableId(type = IdType.AUTO)
    private Integer accountId;

    /**
     * 供应商ID
     */
    private Integer vendorId;

    /**
     * 提现账号类型，1：银行卡，2：支付宝，3：微信
     */
    private Integer accountType;

    /**
     * 姓名
     */
    private String accountName;

    /**
     * 银行卡号或账号
     */
    private String accountNo;

    /**
     * 银行名称
     */
    private String bankName;

    /**
     * 添加时间
     */
    private Long addTime;

    /**
     * 分行名称
     */
    private String bankBranch;

    /**
     * 开户地区ID列表（JSON）
     */
    private String bankRegionIds;

    /**
     * 付款方式，本期只保存原始值，不参与业务判断
     */
    private String paymentMethod;
}
