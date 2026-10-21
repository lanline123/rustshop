package com.tigshop.bean.query.product;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import static com.tigshop.common.constant.product.ECardConstants.GROUP_ID_NOT_NULL;

/**
 * 电子卡券列表
 *
 * @author kidd
 * @create 2025/7/2
 */
@Setter
@Getter
@Schema(description = "电子卡券列表参数")
public class ECardPageQuery extends BasePage {

    @Schema(description = "卡号")
    private String cardNumber;

    @NotNull(message = GROUP_ID_NOT_NULL)
    @Schema(description = "卡券分组id")
    private Integer groupId;
}
