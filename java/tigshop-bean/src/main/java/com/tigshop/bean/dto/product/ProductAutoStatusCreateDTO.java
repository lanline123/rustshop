// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 创建商品自动上下架DTO
 *
 * @author Tigshop团队
 * @create 2025年12月2日
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建商品自动上下架DTO")
public class ProductAutoStatusCreateDTO implements Serializable {

    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID", required = true)
    private Integer productId;

    @Schema(description = "门店ID：0-平台商品，>0-门店商品", required = false)
    private Integer shopId;

    @NotNull(message = "操作类型不能为空")
    @Schema(description = "操作类型：0-下架，1-上架", required = true)
    private Integer operationType;

    @NotNull(message = "计划执行时间不能为空")
    @Schema(description = "计划执行时间（时间戳）", required = true)
    private Long scheduledTime;
}

