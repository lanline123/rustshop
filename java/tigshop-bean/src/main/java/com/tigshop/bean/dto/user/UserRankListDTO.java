package com.tigshop.bean.dto.user;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 会员等级
 */
@Getter
@Setter
@Schema(description = "会员等级")
public class UserRankListDTO extends BasePage {
    /**
     * 该品牌是否显示;0否1显示
     */
    @Schema(description = "是否显示商品价格")
    private Integer showPrice;

    @Schema(description = "会员等级类型")
    private Integer rankType;

}
