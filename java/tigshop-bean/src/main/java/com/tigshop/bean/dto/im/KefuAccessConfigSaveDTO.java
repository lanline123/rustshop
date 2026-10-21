package com.tigshop.bean.dto.im;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 客服接入规则保存数据对象
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "客服接入规则保存")
public class KefuAccessConfigSaveDTO {

    @Schema(description = "店铺ID", hidden = true)
    private Integer shopId;

    @Schema(description = "接入方式：0 全部自动接入 1 全部手动接入")
    private Integer autoAccessMode;

    @Schema(description = "回头客接入：0 关闭 1 优先接入上次客服")
    private Integer returnCustomerAccess;
}
