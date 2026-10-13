package com.tigshop.bean.query.promotion.buyershow;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author wzh
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "买家秀查询参数-客户端")
public class BuyerShowPageClientQuery extends BasePage {

    @Schema(description = "分组ID")
    private Long goodsId;

    @Schema(description = "商品ID")
    private Integer productId;

}
