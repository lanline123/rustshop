// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.query.shop;


import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Tigshop团队
 * @create 2025/8/28 15:27
 */
@Data
public class StorePageQuery extends BasePage {

    private String regionId;

    private BigDecimal longitude = BigDecimal.ZERO;

    private BigDecimal latitude = BigDecimal.ZERO;

    private Integer productId;

    private List<Integer> shopIds;

    //o2o-pc 模式下的门店查询
    private String storeRegionId;

    //选中的门店ID：非过滤条件，仅将该门店置顶到列表第一位并标记为已选中
    private Integer selectedShopId;

    //同城配送范围过滤：传 1 时仅返回用户当前位置落在门店同城配送半径内的门店；未传经纬度时该条件无效
    private Integer withinDeliveryRange;

    //配送类型（多选，OR 关系，与商品列表 ProductListDTO.deliveryTypes 编号一致）：
    //1-到店自提 2-上门服务(预约上门) 3-预约到店 4-同城配送 5-快递配送。
    //传了有效经纬度时，含 2 逐店计算 inHomeServiceRange、含 4 逐店计算 inLocalDeliveryRange；非过滤条件。
    private List<Integer> deliveryTypes;

    @Schema(description = "是否排除平台自营门店；1 或不传-排除，0-不排除（纳入自营）。"
            + "显式 shopIds 含 0 时不生效（指定优先）。默认排除，由前端显式传 0 做兼容")
    private Integer excludeSelfOperated;
}
