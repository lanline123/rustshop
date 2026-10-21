package com.tigshop.bean.dto.vendor;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "上传打款凭证")
public class VendorWithdrawPayVoucherDTO {
    private Integer id;
    private String paymentVoucher;
}
