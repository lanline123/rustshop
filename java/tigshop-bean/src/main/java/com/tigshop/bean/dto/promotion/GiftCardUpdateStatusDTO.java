package com.tigshop.bean.dto.promotion;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GiftCardUpdateStatusDTO {

    @NotNull(message = "卡id不能为空")
    @Schema(description = "卡id")
    private Integer giftCardsId;

    @NotNull(message = "状态值不能为空")
    @Schema(description = "1-可用 0-不可用")
    private Integer isAvailable;
}
