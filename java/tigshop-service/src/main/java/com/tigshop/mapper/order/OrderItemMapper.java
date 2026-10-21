// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.mapper.order;

import com.tigshop.mapper.common.BaseMapper;
import com.tigshop.bean.model.order.OrderItem;
import org.apache.ibatis.annotations.Param;

/**
 * 订单商品映射
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:03
 */
public interface OrderItemMapper extends BaseMapper<OrderItem> {
    Integer getProductSalesVolumeByProductIdAndShopId(@Param("id") Long id, @Param("shopId")String shopId);

    /**
     * 获取指定门店的已售商品数量（仅统计有效且未取消/删除的订单）。
     *
     * @param shopId 门店 ID
     * @return 已售商品数量，无数据时返回 0
     */
    Integer getShopSalesByShopId(@Param("shopId") Integer shopId);
}
