package com.tigshop.bean.dto.im;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 等待客户dto
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "等待客服列表参数")
public class WaitServantListDTO extends BasePage {
    @Schema(description = "用户来源")
    private String userFrom;

    @Schema(description = "店铺id")
    private Integer shopId;

}
