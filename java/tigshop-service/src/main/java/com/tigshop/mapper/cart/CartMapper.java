package com.tigshop.mapper.cart;

import com.tigshop.bean.model.cart.Cart;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author Tigshop团队
 * @create 2024年12月30日 16:32
 */
public interface CartMapper extends BaseMapper<Cart> {

    /**
     * 汇总仍有关联商品的有效购物车商品数量。
     *
     * @param userId 用户ID
     * @param shopId 店铺ID，null 表示全部店铺
     * @param type   购物车类型
     * @return 商品数量，不会为 null
     */
    Integer sumAvailableCartQuantity(@Param("userId") Integer userId,
                                     @Param("shopId") Integer shopId,
                                     @Param("type") Integer type);
}
