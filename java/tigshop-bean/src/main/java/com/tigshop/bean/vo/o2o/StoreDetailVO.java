package com.tigshop.bean.vo.o2o;

import com.tigshop.bean.dto.shop.StoreExtendedDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "店铺详情VO")
public class StoreDetailVO extends StoreExtendedDto {

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "店铺表名称")
    private String shopTitle;

    @Schema(description = "店铺logo/门店logo")
    private String shopLogo;

    @Schema(description = "评星")
    private BigDecimal shopStar;

    @Schema(description = "评价数量")
    private Integer shopStarNum;

    @Schema(description = "销量")
    private Integer shopSales;

    @Schema(description = "标签")
    private List<String> tipNames;

    @Schema(description = "是否收藏店铺")
    private Boolean collectShop;

    @Schema(description = "距离 单位m")
    private String distance;

    @Schema(description = "用户传入经纬度是否落在该门店预约上门服务半径内；仅 deliveryTypes 含 2(上门服务) 且传了经纬度时计算，其余场景恒为 false")
    private Boolean inHomeServiceRange = false;

    @Schema(description = "用户传入经纬度是否落在该门店同城配送半径内；仅 deliveryTypes 含 4(同城配送) 且传了经纬度时计算，其余场景恒为 false")
    private Boolean inLocalDeliveryRange = false;

    @Schema(description = "入驻审核通过或平台录入的营业执照图片，无有效资料返回空串")
    private String businessLicensePic;
}
