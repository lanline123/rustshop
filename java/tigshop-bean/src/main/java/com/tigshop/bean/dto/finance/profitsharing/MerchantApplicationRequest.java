package com.tigshop.bean.dto.finance.profitsharing;

import lombok.Getter;
import lombok.Setter;

/**
 * 商户进件请求
 *
 * @author Tigshop团队
 */
@Getter
@Setter
public class MerchantApplicationRequest {
    private String businessCode;
    private String contactName;
    private String contactIdNumber;
    private String contactPhone;
    private String contactEmail;
    private String merchantName;
    private String merchantShortName;
    private String storeUrl;
    private String businessLicenseNumber;
    private String legalPerson;
    private String bankAccountName;
    private String bankAccountNumber;
    private String bankName;
    private String bankBranchName;

    private String organizationType;
    private Boolean owner;
    private String contactType;

    private String licenseCopy;
    private String licensePeriodBegin;
    private String licensePeriodEnd;

    private String idCardCopy;
    private String idCardNational;
    private String idCardName;
    private String idCardNumber;
    private String cardPeriodBegin;
    private String cardPeriodEnd;

    private String bankAccountType;
    private String settlementId;
    private String qualificationType;

    private String servicePhone;
}
