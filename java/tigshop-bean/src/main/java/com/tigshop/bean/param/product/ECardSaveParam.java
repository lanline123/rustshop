package com.tigshop.bean.param.product;

import com.tigshop.bean.model.product.ECard;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static com.tigshop.common.constant.product.ECardConstants.*;

/**
 * 电子卡券创建数据对象
 *
 * @author kidd
 * @create 2025/7/2
 */
@Data
@Schema(description = "电子卡券参数")
public class ECardSaveParam {

    @NotNull(message = CARD_NUMBER_NOT_NULL)
    @Size(max = 255, message = CARD_NUMBER_OVER_LENGTH)
    @Schema(description = "卡号")
    private String cardNumber;

    @NotNull(message = CARD_PWD_NOT_NULL)
    @Size(max = 255, message = CARD_PWD_OVER_LENGTH)
    @Schema(description = "卡号密码")
    private String cardPwd;

    @NotNull(message = GROUP_ID_NOT_NULL)
    @Schema(description = "卡券分组id")
    private Integer groupId;

    @Schema(description = "0:未使用,1:已使用")
    private Integer isUse;

    public ECard createECard() {
        return ECard.builder()
                .groupId(this.groupId)
                .cardNumber(this.cardNumber)
                .cardPwd(this.cardPwd)
                .isUse(this.isUse)
                .addTime(StringUtils.getCurrentTime())
                .upTime(StringUtils.getCurrentTime())
                .build();
    }
}
