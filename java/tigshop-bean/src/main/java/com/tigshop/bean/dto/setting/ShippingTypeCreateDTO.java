// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static com.tigshop.common.constant.setting.ShippingTypeConstants.*;

/**
 * 配送类型创建数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "配送类型参数")
public class ShippingTypeCreateDTO {
    @Schema(description = "是否支持货到付款")
    private Integer isSupportCod;

    @Schema(description = "默认发货物流公司ID")
    private Integer shippingDefaultId;

    @Schema(description = "发货时间描述")
    private String shippingTimeDesc;

    @Schema(description = "描述")
    private String shippingTypeDesc;

    @Schema(description = "类型名称")
    private String shippingTypeName;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "店铺id")
    private Integer shopId;
}
