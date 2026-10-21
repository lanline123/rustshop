package com.tigshop.bean.model.vendor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 供应商表
 *
 * @TableName vendor
 */
@TableName(value = "vendor")
@Data
public class Vendor {
    /**
     * 自增ID，供应商ID
     */
    @TableId(type = IdType.AUTO)
    private Integer vendorId;

    /**
     * 来源类型：1-入驻供应商，2-店铺/门店自建供应商
     */
    private Integer sourceType;

    /**
     * 自建供应商所属组织；入驻供应商不使用该字段做归属判断
     */
    private Integer shopId;

    /**
     * 自建供应商编码
     */
    private String vendorCode;

    /**
     * 采购供应商分类ID
     */
    private Long purchaseCategoryId;

    /**
     * 供应商logo
     */
    private String vendorLogo;

    /**
     * 联系电话
     */
    private String contactMobile;

    /**
     * 供应商创建时间
     */
    private Long addTime;

    /**
     * 供应商信息JSON
     */
    private String vendorData;

    /**
     * 个人信息JSON
     */
    private String personData;

    /**
     * 状态 1开启 2关闭
     */
    private Integer status;

    /**
     * 售后审核 0关闭 1开启
     */
    private Integer afterSalesReview;

    /**
     * 类型 1个人 2企业
     */
    private Integer type;

    /**
     * 供应商名称
     */
    private String vendorName;

    /**
     * 客服电话
     */
    private String kefuPhone;

    /**
     * 发货信息JSON
     */
    private String shipInfo;

    /**
     * 收货信息JSON
     */
    private String receiveInfo;

    /**
     * 供应商余额
     */
    private BigDecimal vendorMoney;

    /**
     * 冻结金额
     */
    private BigDecimal frozenMoney;

    /**
     * 简介
     */
    private String description;

    /**
     * 最后登录时间
     */
    private Long lastLoginTime;

    @Schema(description = "服务费比例")
    private BigDecimal serviceFeeRate;

    @Schema(description = "手续费比例")
    private BigDecimal feeRate;
}
