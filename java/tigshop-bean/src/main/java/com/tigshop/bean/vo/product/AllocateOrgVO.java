package com.tigshop.bean.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllocateOrgVO {

    @Schema(description = "门店id")
    private Integer shopId;

    @Schema(description = "门店名称")
    private String shopName;
}
