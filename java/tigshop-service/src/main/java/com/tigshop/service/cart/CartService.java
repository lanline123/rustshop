package com.tigshop.service.cart;

import com.github.yulichang.base.MPJBaseService;
import com.tigshop.bean.dto.cart.AddToCartDTO;
import com.tigshop.bean.dto.cart.ListDTO;
import com.tigshop.bean.dto.cart.UpdateItemDTO;
import com.tigshop.bean.model.cart.Cart;
import com.tigshop.bean.param.o2o.BaseChangePriceParam;
import com.tigshop.bean.vo.cart.CartListByStoreVO;
import com.tigshop.bean.vo.cart.CartVO;
import com.tigshop.bean.vo.content.CartCouponDiscountVO;

import java.util.List;
import java.util.Map;

/**
 * 购物车
 *
 * @author Tigshop团队
 * @create 2024-09-30 15:16:06
 */
public interface CartService extends MPJBaseService<Cart> {

    List<CartVO> getCartList(boolean isChecked, Integer type, Map<String, Object> filter, Integer storeShopId, Integer userId,Boolean excludeQuickProduct);

    /**
     * 获取购物车数量
     */
    Integer getCartCount(Integer shopId, Integer userId);

    /**
     * 批量更新选中状态
     */
    void updateCheck(List<Map<String, Object>> list);

    /**
     * 批量更新购物车选中状态，并按目标用户校验购物车归属。
     */
    void updateCheck(List<Map<String, Object>> list, Integer userId);

    /**
     * 更新购物车商品
     */
    void updateCartItem(UpdateItemDTO updateItem);

    /**
     * 更新购物车商品；userId 非空时限制只能更新该用户的购物车。
     */
    void updateCartItem(UpdateItemDTO updateItem, Integer userId);

    /**
     * 获取优惠券折扣
     */
    CartCouponDiscountVO getCouponDiscount(Integer couponId);

    /**
     * 获取购物车列表
     */
    CartListByStoreVO getCartList(ListDTO listDTO, Integer userId);


    CartListByStoreVO getCartList(ListDTO listDTO, Integer userId,Integer cartType);

    /**
     * 获取购物车列表
     */
    CartListByStoreVO getCartListByStore(boolean isChecked, Integer type, Map<String, Object> filter, Integer storeShopId, Integer userId,Boolean excludeQuickProduct);

    /**
     * 构建购物车促销信息
     */
    CartListByStoreVO buildCartPromotion(CartListByStoreVO cartListByStoreVO, BaseChangePriceParam baseChangePriceParam, Integer userId, Integer flowType, Integer useDefaultCoupon, List<Integer> useCouponIds);

    /**
     * 再次购买
     */
    boolean buyAgain(Integer id, Integer userId);

    /**
     * 添加购物车商品
     */

    Boolean addProductToCart(Integer userId, AddToCartDTO addToCartDTO);

    /**
     * 检查商品限购数量
     */
    Boolean checkProductLimitNumber(Integer productId, Integer userId, Integer quantity, Integer cartId);

    /**
     * 检查订单商品限购数量
     */
    Boolean checkOrderProductLimitNumber(Integer productId, Integer userId, Integer quantity, Integer cartId);

    /**
     * 清空购物车
     */
    void clearCart(Integer userId, Integer type);

    /**
     * 获取购物车类型
     */
    Integer getCartTypeByProduct(Integer id, Integer type);
}
