package com.tigshop.bean.param.product;

import com.tigshop.bean.model.product.ECard;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.tigshop.common.constant.product.ECardConstants.E_CARD_ID_NOT_NULL;

/**
 * 电子卡券更新参数
 *
 * @author kidd
 * @create 2025/7/2
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "电子卡券参数")
public class ECardEditParam extends ECardSaveParam {

    @NotNull(message = E_CARD_ID_NOT_NULL)
    @Schema(description = "电子卡券id")
    private Integer cardId;

    public ECard createECard() {
        ECard eCard = super.createECard();
        eCard.setAddTime(null);
        eCard.setCardId(this.cardId);
        return eCard;
    }

}
