package com.tigshop.bean.vo.setting.shippingtpl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.tigshop.bean.model.setting.ShippingTplInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 运费模版内容
 *
 * @author kidd
 * @since 2025/4/21 17:40
 */
@Schema(description = "运费模板内容")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShippingTplInfoVO {

    // *** ShippingTplInfo ***

    @Schema(description = "运费模板内容ID")
    private Long id;

    @Schema(description = "配送类型ID")
    private Long shippingTypeId;

    @Schema(description = "运费模板ID")
    private Long shippingTplId;

    @Schema(description = "是否包邮")
    private Integer isFree;

    @Schema(description = "是否默认")
    private Integer isDefault;

    @Schema(description = "起重或起件")
    private BigDecimal startNumber;

    @Schema(description = "起价")
    private BigDecimal startPrice;

    @Schema(description = "每增重或增价")
    private BigDecimal addNumber;

    @Schema(description = "增价")
    private BigDecimal addPrice;

    @Schema(description = "满多少包邮件")
    private BigDecimal freePrice;

    @Schema(description = "按重还是按件，1:按件，2：按重")
    private Integer pricingType;

    // *** Other ***

    @Schema(description = "地区信息")
    private RegionDataVO regionData;

    public ShippingTplInfoVO(ShippingTplInfo shippingTplInfo) {
        this.id = shippingTplInfo.getId();
        this.shippingTypeId = shippingTplInfo.getShippingTypeId();
        this.shippingTplId = shippingTplInfo.getShippingTplId();
        this.isFree = shippingTplInfo.getIsFree();
        this.isDefault = shippingTplInfo.getIsDefault();
        this.startNumber = shippingTplInfo.getStartNumber();
        this.startPrice = shippingTplInfo.getStartPrice();
        this.addNumber = shippingTplInfo.getAddNumber();
        this.addPrice = shippingTplInfo.getAddPrice();
        this.freePrice = shippingTplInfo.getFreePrice();

        if (StrUtil.isNotBlank(shippingTplInfo.getRegionData())) {
            this.regionData = JSON.parseObject(shippingTplInfo.getRegionData(), RegionDataVO.class);
        }

    }
}
