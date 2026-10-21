package com.tigshop.bean.vo.promotion.buyershow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wzh
 */
@Data
@Schema(description = "买家信息")
@AllArgsConstructor
@NoArgsConstructor
public class BuyerVO {

    @Schema(description = "买家头像")
    private String buyerPhoto;

    @Schema(description = "买家名称")
    private String buyerName;

}
