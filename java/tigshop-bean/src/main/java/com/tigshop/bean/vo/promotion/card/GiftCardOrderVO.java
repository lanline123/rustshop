package com.tigshop.bean.vo.promotion.card;

import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Tigshop项目组
 * @create 2025年10月31日 16:36
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "礼品卡订单")
public class GiftCardOrderVO {
    @Schema(description = "数量")
    private Integer num;

    @Schema(description = "订单卡信息")
    private List<GiftCardOrderInfo> giftCardOrderInfo;

    @Data
    @Schema(description = "卡信息")
    public static class GiftCardOrderInfo {
        @Schema(description = "所属卡组ID")
        private Integer cardGroupsId;

        @Schema(description = "礼品卡id")
        private Integer giftCardsId;

        @Schema(description = "礼品卡sn")
        private String giftCardSn;

        @Schema(description = "店铺名称")
        @JsonTranslate(dataType = 1)
        private String shopTitle;

        @Schema(description = "店铺名称")
        private Integer shopId;

        @Schema(description = "店铺头像")
        private String shopLogo;

        @Schema(description = "卡号")
        private String cardNumber;

        @Schema(description = "卡密码")
        private String cardPassword;

        @JsonTranslate(dataType = 17)
        @Schema(description = "卡组名称")
        private String cardGroupName;

        @Schema(description = "过期时间")
        private String expiredTime;

        @Schema(description = "状态")
        private Integer status;

        @JsonTranslate(dataType = 17)
        @Schema(description = "图片")
        private String imageUrl;
    }
}