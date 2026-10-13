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
@Schema(description = "买家秀分组查询参数")
public class BuyerShowGroupPageQuery extends BasePage {
}
