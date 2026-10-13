package com.tigshop.bean.dto.user;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 自提联系人列表查询参数
 *
 * @author Tigshop
 */
@Setter
@Getter
@Schema(description = "自提联系人列表查询参数")
public class PickupContactListDTO extends BasePage {

    @Schema(description = "客户ID")
    private Integer userId;
}
