package com.tigshop.bean.query.promotion.card;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Tigshop项目组
 * @create 2025年10月30日 16:45
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "卡列表查询参数")
public class GiftCardsPageQuery extends BasePage {
    @Schema(description = "卡组ID")
    private Integer cardGroupsId;
}