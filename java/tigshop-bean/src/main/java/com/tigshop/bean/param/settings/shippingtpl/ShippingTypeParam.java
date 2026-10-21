package com.tigshop.bean.param.settings.shippingtpl;

import com.alibaba.fastjson.JSON;
import com.tigshop.bean.model.setting.ShippingTpl;
import com.tigshop.bean.model.setting.ShippingTplInfo;
import com.tigshop.common.constant.Constants;
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
@Schema(description = "运费模板信息")
public class ShippingTypeParam {

    // *** ShippingType ***

    @Schema(description = "配送类型ID")
    private Long shippingTypeId;

    @Schema(description = "配送类型名称")
    private String shippingTypeName;

    @Schema(description = "默认配送模板ID")
    private Long shippingDefaultId;

    @Schema(description = "配送类型描述")
    private String shippingTypeDesc;

    @Schema(description = "发货时间描述")
    private String shippingTimeDesc;

    @Schema(description = "是否支持货到付款")
    private Integer isSupportCod;

    @Schema(description = "排序顺序")
    private Integer sortOrder;

    @Schema(description = "店铺ID")
    private Long shopId;

    // *** Other ***

    @Schema(description = "是否选中")
    private Integer isChecked;

    @Schema(description = "默认模板信息")
    private ShippingTplInfoParam defaultTplInfo;

    @Schema(description = "区域模板信息列表")
    private List<ShippingTplInfoParam> areaTplInfo;

    public ShippingTplInfo createDefaultShippingTplInfo(ShippingTpl shippingTpl) {
        String regionData = JSON.toJSONString(this.defaultTplInfo.getRegionData());

        return ShippingTplInfo.builder()
                .shippingTypeId(1L)
                .shippingTplId(shippingTpl.getShippingTplId())
                .isFree(shippingTpl.getIsFree())
                .isDefault(Constants.YES)
                .regionData(regionData)
                .startNumber(this.defaultTplInfo.getStartNumber())
                .startPrice(this.defaultTplInfo.getStartPrice())
                .addNumber(this.defaultTplInfo.getAddNumber())
                .addPrice(this.defaultTplInfo.getAddPrice())
                .freePrice(this.defaultTplInfo.getFreePrice())
                .pricingType(shippingTpl.getPricingType())
                .build();
    }

    public List<ShippingTplInfo> createAreaShippingTplInfos(ShippingTpl shippingTpl) {
        return this.areaTplInfo.stream()
                .map(item -> {
                    String regionData = JSON.toJSONString(item.getRegionData());

                    return ShippingTplInfo.builder()
                            .shippingTypeId(1L)
                            .shippingTplId(shippingTpl.getShippingTplId())
                            .isFree(shippingTpl.getIsFree())
                            .isDefault(Constants.NO)
                            .regionData(regionData)
                            .startNumber(item.getStartNumber())
                            .startPrice(item.getStartPrice())
                            .addNumber(item.getAddNumber())
                            .addPrice(item.getAddPrice())
                            .freePrice(item.getFreePrice())
                            .pricingType(shippingTpl.getPricingType())
                            .build();
                })
                .toList();
    }

}
