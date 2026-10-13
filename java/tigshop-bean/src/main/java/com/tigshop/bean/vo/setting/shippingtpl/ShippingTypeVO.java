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

import cn.hutool.core.collection.CollUtil;
import com.tigshop.bean.model.setting.ShippingTplInfo;
import com.tigshop.bean.model.setting.ShippingType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 配送类型VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "配送类型参数")
public class ShippingTypeVO {

    // *** ShippingType ***

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

    // *** other ***

    @Schema(description ="物流公司名称")
    private String logisticsName;

    @Schema(description ="默认运费模板内容")
    private ShippingTplInfoVO defaultTplInfo;

    @Schema(description ="运费模板内容")
    private List<ShippingTplInfoVO> areaTplInfo;

    public ShippingTypeVO(ShippingType shippingType, List<ShippingTplInfo> shippingTplInfos) {
        this.shippingTypeId = shippingType.getShippingTypeId();
        this.shippingTypeName = shippingType.getShippingTypeName();
        this.shippingDefaultId = shippingType.getShippingDefaultId();
        this.shippingTypeDesc = shippingType.getShippingTypeDesc();
        this.shippingTimeDesc = shippingType.getShippingTimeDesc();
        this.isSupportCod = shippingType.getIsSupportCod();
        this.sortOrder = shippingType.getSortOrder();
        this.shopId = shippingType.getShopId();

        if (CollUtil.isNotEmpty(shippingTplInfos)) {
            List<ShippingTplInfoVO> areaTplInfo = new ArrayList<>();
            for (ShippingTplInfo shippingTplInfo : shippingTplInfos) {
                if (shippingTplInfo.getIsDefault() == 1) {
                    this.defaultTplInfo = new ShippingTplInfoVO(shippingTplInfo);
                } else {
                    areaTplInfo.add(new ShippingTplInfoVO(shippingTplInfo));
                }
            }
            this.areaTplInfo = areaTplInfo;
        }

    }
}