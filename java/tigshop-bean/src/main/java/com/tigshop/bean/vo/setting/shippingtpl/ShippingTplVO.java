// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.setting.shippingtpl;

import com.tigshop.bean.model.setting.ShippingTpl;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 运费模板VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "运费模板参数")
public class ShippingTplVO {

    // *** ShippingTpl ***

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

    // *** Other ***

    @Schema(description = "模板信息")
    List<ShippingTypeVO> shippingTplInfo;

    public ShippingTplVO(ShippingTpl shippingTpl, List<ShippingTypeVO> shippingTplInfo) {
        this.shippingTplId = shippingTpl.getShippingTplId();
        this.shippingTime = shippingTpl.getShippingTime();
        this.shippingTplName = shippingTpl.getShippingTplName();
        this.isFree = shippingTpl.getIsFree();
        this.pricingType = shippingTpl.getPricingType();
        this.isDefault = shippingTpl.getIsDefault();

        this.shippingTplInfo = shippingTplInfo;
    }
}