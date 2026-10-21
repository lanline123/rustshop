package com.tigshop.bean.param.finance.statement;

import com.tigshop.bean.enums.finance.AccountType;
import com.tigshop.bean.enums.finance.EntryType;
import com.tigshop.bean.enums.finance.StatementType;
import com.tigshop.bean.model.shop.ShopWithdraw;
import com.tigshop.bean.model.vendor.VendorWithdraw;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.tigshop.bean.enums.order.PayMethodType.OFFLINE;

/**
 * 对账单保存
 *
 * @author Tigshop项目组
 * @create 2025年08月02日 13:38
 */
@Data
@Schema(description = "对账单保存参数")
@AllArgsConstructor
@NoArgsConstructor
public class StatementSaveParam {
    @Schema(description = "单据id")
    private Integer recordId;

    @Schema(description = "账户类型1.账户余额")
    private Integer accountType;

    @Schema(description = "类型：1.手续费 2.店铺提现收支 3.店铺订单收支等")
    private Integer type;

    @Schema(description = "入账方式：1. 自动 2.手动")
    private String entryType;

    @Schema(description = "支付方式（如微信、支付宝等）")
    private String paymentType;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "供应商id")
    private Integer vendorId;

    // 店铺提现
    public StatementSaveParam(ShopWithdraw shopWithdraw){
        this.setShopId(shopWithdraw.getShopId());
        this.setVendorId(0);
        this.setRecordId(shopWithdraw.getShopWithdrawLogId());
        this.setAccountType(AccountType.ACCOUNT_BALANCE.getCode());
        this.setType(StatementType.SHOP_WITHDRAWAL.getCode());
        this.setEntryType(EntryType.AUTO.getCode());
        this.setPaymentType(OFFLINE.getCode());
    }

    // 供应商提现
    public StatementSaveParam(VendorWithdraw vendorWithdraw){
        this.setShopId(0);
        this.setVendorId(vendorWithdraw.getVendorId());
        this.setRecordId(vendorWithdraw.getVendorWithdrawLogId());
        this.setAccountType(AccountType.ACCOUNT_BALANCE.getCode());
        this.setType(StatementType.SUPPLIER_WITHDRAWAL.getCode());
        this.setEntryType(EntryType.AUTO.getCode());
        this.setPaymentType(OFFLINE.getCode());
    }
}