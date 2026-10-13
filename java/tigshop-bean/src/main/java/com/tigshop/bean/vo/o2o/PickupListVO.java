// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.vo.o2o;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tigshop.bean.dto.shop.StoreExtendedDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Tigshop团队
 * @create 2025/8/28 15:35
 */
@Data
public class PickupListVO {

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "店铺标题")
    private String shopTitle;

    @Schema(description = "距离 单位m")
    private BigDecimal distance;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "联系方式(原始json字符串)")
    @JsonIgnore
    private String contactJson;

    @Schema(description = "联系方式")
    private List<StoreExtendedDto.ShopContactConfig> contactConfigs;

    @Schema(description = "运营时间（JSON）")
    @JsonIgnore
    private String shopOpenCloseConfigJson;

    @Schema(description = "运营时间")
    private StoreExtendedDto.ShopOpenCloseConfig shopOpenCloseConfig;

    @Schema(description = "门店regionId （JSON）")
    @JsonIgnore
    private String shopRegionIdsJson;

    @Schema(description = "门店regionId ")
    private List<Integer> shopRegionIds;

    @Schema(description = "门店regionId ")
    private List<String> shopRegionNames;

    @Schema(description = "门店详细地址 ")
    private String address;

}
