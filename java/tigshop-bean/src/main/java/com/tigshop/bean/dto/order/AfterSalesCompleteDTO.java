package com.tigshop.bean.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 软件许可服务实现
 *
 * @author Tigshop团队
 * @create 2024年11月19日 10:19
 */
@Data
@Schema(description = "售后完结")
public class AfterSalesCompleteDTO {

    @NotBlank(message = "id不能为空")
    @Schema(description = "id")
    private Integer id;
}
