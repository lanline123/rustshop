package com.tigshop.bean.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 电子卡券VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "电子卡券信息")
public class ECardItemVO {
    @Schema(description = "电子卡券ID")
    private Integer cardId;

    @Schema(description = "电子卡券号")
    private String cardNumber;

    @Schema(description = "电子卡券密钥")
    private String cardPwd;

    @Schema(description = "订单item")
    private Integer orderItemId;

    @Schema(description = "电子卡券组id")
    private Integer groupId;
}

