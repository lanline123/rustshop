package com.tigshop.bean.dto.finance.profitsharing;

import lombok.Getter;
import lombok.Setter;

/**
 * 商户进件结果
 *
 * @author Tigshop团队
 */
@Getter
@Setter
public class MerchantApplicationResult {
    private Boolean success;
    private String applymentId;
    private String status;
    private String subMchId;
    private String rejectReason;
    private String signUrl;
    private String errorMessage;
}
