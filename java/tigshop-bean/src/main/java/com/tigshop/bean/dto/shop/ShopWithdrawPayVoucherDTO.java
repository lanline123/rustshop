package com.tigshop.bean.dto.shop;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "上传打款凭证")
public class ShopWithdrawPayVoucherDTO {
    private Integer id;
    private String paymentVoucher;
}
