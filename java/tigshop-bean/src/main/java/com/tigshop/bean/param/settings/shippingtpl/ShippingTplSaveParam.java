// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.param.settings.shippingtpl;

import com.tigshop.bean.model.setting.ShippingTpl;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ShippingTplSaveParam {

    // *** ShippingTpl ***

    @NotBlank(message = "运费模版名称不能为空")
    @Schema(description = "运费模板名称")
    private String shippingTplName;

    @Schema(description = "发货时间")
    private String shippingTime;

    @NotNull(message = "是否默认不能为空")
    @Schema(description = "是否默认")
    private Integer isDefault;

    @NotNull(message = "是否包邮不能为空")
    @Schema(description = "是否包邮")
    private Integer isFree;

    @NotNull(message = "计价方式不能为空")
    @Schema(description = "计价方式")
    private Integer pricingType;

    // *** Other ***

    @Schema(description = "运费模板信息列表")
    private List<ShippingTypeParam> shippingTplInfo;

    public ShippingTpl createShippingTpl(Integer shopId) {
        return ShippingTpl.builder()
                .shippingTime(this.shippingTime)
                .shippingTplName(this.shippingTplName)
                .isFree(this.isFree)
                .pricingType(this.pricingType)
                .isDefault(this.isDefault)
                .shopId(shopId)
                .build();
    }

}

