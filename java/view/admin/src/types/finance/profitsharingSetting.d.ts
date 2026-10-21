export interface ProfitsharingSetting {
    billingNode: number;
    collectionNode: number;
    collectionTimeSetting: number;
    collectionMethod: number;
    collectionAccountType: number;
    /** 分账总开关：0-关 1-开（默认 1）。关闭则全部分账停用 */
    profitSharingEnabled?: number;
    /** 分账方式：1-线下分账 2-线上分账 */
    splitPaymentMethod: number;
    /** 线上分账渠道（单选）：wechat-微信 huifu-汇付 alipay-支付宝 */
    profitSharingOnlineChannel?: string;
    /** 是否启用自动分账：0-不启用 1-启用（线上分账） */
    autoProfitSharingEnabled?: number;
    /** 自动分账等待天数，结算后多少天自动发起分账（线上分账） */
    autoProfitSharingDays?: number;

    storeGeneralServiceFeeRate: number;
    storeWithdrawalFeeRate: number;

    storefrontGeneralServiceFeeRate: number;
    storefrontWithdrawalFeeRate: number;

    supplierGeneralServiceFeeRate: number;
    supplierWithdrawalFeeRate: number;
}
export interface withdrawalSetting {
    withdrawalReceiptMethod: number[] | string;
    withdrawalEnabled: number;
    minAmount: number;
    maxAmount: number;
    withdrawalFrequencyUnit: number;
    withdrawalFrequencyCount: number;
    withdrawalReviewMethod: number;
    withdrawalDescription: string;

    /** 线上提现开关：0-关 1-开 */
    onlineWithdrawEnabled?: number;
    /** 线上提现限额最小值（元） */
    onlineMinAmount?: number;
    /** 线上提现限额最大值（元） */
    onlineMaxAmount?: number;
    /** 线上提现频次单位：1-每日 2-每月 3-每年 */
    onlineWithdrawalFrequencyUnit?: number;
    /** 线上提现频次数量 */
    onlineWithdrawalFrequencyCount?: number;
    /** 线上提现审核方式：1-人工审核 */
    onlineWithdrawalReviewMethod?: number;
    /** 线上提现说明 */
    onlineWithdrawalDescription?: string;
    /** 线上收款方式（启用渠道，多选）：WECHAT-微信 HUIFU-汇付 ALIPAY-支付宝 */
    onlineWithdrawChannels?: string[];
}
