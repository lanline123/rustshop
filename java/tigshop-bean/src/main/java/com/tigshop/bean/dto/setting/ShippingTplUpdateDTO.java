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

import com.tigshop.bean.param.settings.shippingtpl.ShippingTypeParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 运费模板创建数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "运费模板创建数据对象")
public class ShippingTplUpdateDTO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "运费模板信息列表")
    private List<ShippingTypeParam> shippingTplInfo;

    @Schema(description = "是否默认")
    private Integer isDefault;

    @Schema(description = "是否免费")
    private Integer isFree;

    @Schema(description = "定价类型")
    private Integer pricingType;

    @Schema(description = "运费模板id")
    private Long shippingTplId;

    @Schema(description = "发货时间")
    private String shippingTime;

    @Schema(description = "运费模板名称")
    private String shippingTplName;

    @Schema(description = "店铺id")
    private Integer shopId;

}

