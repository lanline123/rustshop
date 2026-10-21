// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.setting;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 配送类型model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("shipping_type")
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "配送类型")
public class ShippingType {

    @TableId(value = "shipping_type_id", type = IdType.AUTO)
    @Schema(description = "配送类型id")
    private Long shippingTypeId;

    @Schema(description = "类型名称")
    private String shippingTypeName;

    @Schema(description = "默认发货物流公司ID")
    private Integer shippingDefaultId;

    @Schema(description = "描述")
    private String shippingTypeDesc;

    @Schema(description = "发货时间描述")
    private String shippingTimeDesc;

    @Schema(description = "是否支持货到付款")
    private Integer isSupportCod;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "店铺id")
    private Integer shopId;

    public ShippingType(Long shippingTypeId){
        this.shippingTypeId = shippingTypeId;
    }
}
