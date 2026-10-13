package com.tigshop.bean.param.finance.statement;

import com.tigshop.bean.enums.finance.SettlementStatus;
import com.tigshop.bean.enums.finance.StatementType;
import com.tigshop.bean.model.order.Aftersales;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.shop.ShopWithdraw;
import com.tigshop.bean.model.vendor.VendorWithdraw;
import com.tigshop.common.constant.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 出账单保存参数
 *
 * @author Tigshop项目组
 * @create 2025年08月13日 10:05
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatementOutputSaveParam {
    private Integer shopId;

    private Integer vendorId;

    /**
     * 收入金额
     */
    private BigDecimal income;

    /**
     * 支出金额
     */
    private BigDecimal expenditure;

    private String recordSn;

    private Integer recordType;

    private Integer recordId;

    private Integer settlementStatus;

    private Integer realSettlement;

    private Integer realSettlementTime;

    public StatementOutputSaveParam(VendorWithdraw vendorWithdraw, boolean isServiceFee){
        this.setVendorId(vendorWithdraw.getVendorId());
        this.setShopId(0);
        this.setIncome(BigDecimal.ZERO);
        this.setRecordSn(vendorWithdraw.getWithdrawSn());
        this.setExpenditure(vendorWithdraw.getAmount());
        if (isServiceFee) {
            this.setRecordType(StatementType.HANDLING_FEE.getCode());
        } else {
            this.setRecordType(StatementType.SUPPLIER_WITHDRAWAL.getCode());
        }
        this.setRecordId(vendorWithdraw.getVendorWithdrawLogId());
        this.setSettlementStatus(SettlementStatus.SETTLED.getCode());
    }

    public StatementOutputSaveParam(ShopWithdraw shopWithdraw, boolean isServiceFee){
        this.setVendorId(0);
        this.setShopId(shopWithdraw.getShopId());
        this.setIncome(BigDecimal.ZERO);
        this.setExpenditure(shopWithdraw.getAmount());
        if (isServiceFee) {
            this.setRecordType(StatementType.HANDLING_FEE.getCode());
        } else {
            this.setRecordType(StatementType.SHOP_WITHDRAWAL.getCode());
        }
        this.setRecordId(shopWithdraw.getShopWithdrawLogId());
        this.setRecordSn(shopWithdraw.getWithdrawSn());
        this.setSettlementStatus(SettlementStatus.SETTLED.getCode());
    }

    // 店铺订单收入 / 订单手续费
    public StatementOutputSaveParam(Order order, int recordType, BigDecimal amount, boolean isShop, boolean isIncome){
        // 店铺
        if (isShop) {
            this.setVendorId(0);
            this.setShopId(order.getShopId());
        }
        // 供应商
        if (!isShop) {
            this.setVendorId(order.getVendorId());
            this.setShopId(0);
        }
        if (isIncome) {
            this.setIncome(amount);
            this.setExpenditure(BigDecimal.ZERO);
        } else {
            this.setIncome(BigDecimal.ZERO);
            this.setExpenditure(amount);
        }
        this.setRecordType(recordType);
        this.setRecordId(order.getOrderId());
        this.setRecordSn(order.getOrderSn());
        this.setSettlementStatus(SettlementStatus.SETTLED.getCode());
        this.setRealSettlement(Constants.NO);
    }

    public StatementOutputSaveParam(Aftersales aftersales, int recordType, BigDecimal amount, boolean isShop, boolean isIncome){
        // 店铺
        if (isShop) {
            this.setVendorId(0);
            this.setShopId(aftersales.getShopId());
        }
        // 供应商
        if (!isShop) {
            this.setVendorId(aftersales.getVendorId());
            this.setShopId(0);
        }
        if (isIncome) {
            this.setIncome(amount);
            this.setExpenditure(BigDecimal.ZERO);
        } else {
            this.setIncome(BigDecimal.ZERO);
            this.setExpenditure(amount);
        }
        this.setRecordType(recordType);
        this.setRecordId(aftersales.getAftersaleId());
        this.setRecordSn(aftersales.getAftersalesSn());
        this.setSettlementStatus(SettlementStatus.SETTLED.getCode());
        this.setRealSettlement(Constants.NO);
    }
}