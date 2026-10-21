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
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Tigshop团队
 * @create 2025/8/28 15:27
 */
@Data
public class StoreDetailQuery {

    private Integer shopId;

    private BigDecimal longitude = BigDecimal.ZERO;

    private BigDecimal latitude = BigDecimal.ZERO;

    //配送类型（多选，OR 关系，与商品列表 ProductListDTO.deliveryTypes 编号一致）：
    //1-到店自提 2-上门服务(预约上门) 3-预约到店 4-同城配送 5-快递配送。
    //传了有效经纬度时，含 2 计算 inHomeServiceRange、含 4 计算 inLocalDeliveryRange，其余码不参与范围计算。
    private List<Integer> deliveryTypes;
}
