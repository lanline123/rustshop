// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.shop;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 店铺表列表
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Setter
@Getter
@Schema(description = "店铺表列表参数")
public class ShopListDTO extends BasePage {
    @Schema(description = "是否为 myshop 接口请求")
    private Integer isMyShop = 0;

    @Schema(description = "1店铺，2门店，3自提点")
    private Integer shopType = 1;

    @Schema(description = "myShop 接口按需过滤的店铺类型集合（1店铺，2门店，3自提点）")
    private List<Integer> shopTypes;

    @Schema(description = "是否包含平台自营主体（shopId=0，按 shopType 展示为店铺或门店）；1 包含，默认 0 不包含。shopType=1/2 生效")
    private Integer includeSelfOperated = 0;

    @Schema(description = "C 端店铺列表是否排除平台自营门店；1 或不传-排除，0-不排除（纳入自营）。默认排除，由前端显式传 0 做兼容")
    private Integer excludeSelfOperated;

    @Schema(description = "店铺状态")
    private Integer status;

    @Schema(description = "店铺名称")
    private String shopTitle;

    @Schema(description = "主账号名称")
    private String mainAccount;

    @Schema(description = "账号名称")
    private String account;

    @Schema(description = "店铺id集合")
    private List<Integer> shopIds;

    @Schema(description = "门店父id")
    private Integer storeParentId;

    @Schema(description = "创建开始时间")
    private String addStartTime;

    @Schema(description = "创建结束时间")
    private String addEndTime;

    @Schema(description = "区域id")
    private Integer areaStoreManagerId;

    @Schema(description = "省市区id")
    private String shopRegionId;

    @Schema(description = "商品id")
    private Integer productId;

    @Schema(description = "商品个数")
    private Integer productNumber;

    @Schema(description = "下拉框 0 否；1 是")
    private Integer select = 0;

    @Schema(description = "分配商品id（用于筛选分配后的店铺列表）")
    private Integer distributionProductId;

    @Schema(description = "商户名称")
    private String corporateName;
}
