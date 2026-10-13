package com.tigshop.bean.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 软件许可服务实现
 *
 * @author Tigshop团队
 * @create 2024年11月19日 10:19
 */
@Data
@Schema(description = "售后取消参数")
public class AfterSalesCancelDTO {
    @Schema(description = "售后管理ID")
    private Integer aftersaleId;

    @Schema(description = "用户id")
    private Integer userId;
}
