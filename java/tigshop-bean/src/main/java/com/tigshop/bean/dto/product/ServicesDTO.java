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

import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static com.tigshop.common.constant.product.ServicesConstants.SERVICE_NAME_IS_NULL;
import static com.tigshop.common.constant.product.ServicesConstants.SERVICE_NAME_OVER_LENGTH;

/**
 * 商品服务参数
 *
 * @author Jayce
 * @create 2024年11月06日 10:21
 */
@Data
@Schema(description = "商品服务参数")
public class ServicesDTO {
    @Schema(description = "服务id")
    private Integer productServiceId;

    @Schema(description = "服务名称")
    @NotNull(message = SERVICE_NAME_IS_NULL)
    @Size(max = 100, message = SERVICE_NAME_OVER_LENGTH)
    @JsonTranslate(dataType = 1)
    private String productServiceName;

    @Schema(description = "服务描述")
    @JsonTranslate(dataType = 1)
    private String productServiceDesc;

    @Schema(description = "服务图标")
    private String icoImg;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "默认开启")
    private Integer defaultOn;

    @Schema(description = "店铺id")
    private Integer shopId;
}
