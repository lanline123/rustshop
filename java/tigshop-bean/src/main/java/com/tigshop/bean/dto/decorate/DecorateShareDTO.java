package com.tigshop.bean.dto.decorate;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "售后申请表列表参数")
public class DecorateShareDTO extends BasePage {
    @Schema(description ="主键id")
    private Integer decorateId;
}
