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
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 运费模板model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("shipping_tpl")
@Schema(description = "运费模板")
public class ShippingTpl {

    @TableId(value = "shipping_tpl_id", type = IdType.AUTO)
    @Schema(description = "运费模板ID")
    private Long shippingTplId;

    @Schema(description = "配送时间")
    private String shippingTime;

    @Schema(description = "模板名称")
    private String shippingTplName;

    @Schema(description = "是否包邮")
    private Integer isFree;

    @Schema(description = "1：件数，2：重量")
    private Integer pricingType;

    @Schema(description = "是否默认")
    private Integer isDefault;

    @Schema(description = "店铺id")
    private Integer shopId;
}
