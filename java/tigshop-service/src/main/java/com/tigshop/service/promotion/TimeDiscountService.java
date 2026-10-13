package com.tigshop.service.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.cart.CartPromotionParsePriceDTO;
import com.tigshop.bean.param.promotion.timediscount.TimeDiscountSaveParam;
import com.tigshop.bean.query.promotion.timediscount.TimeDiscountPageQuery;
import com.tigshop.bean.param.promotion.timediscount.TimeDiscountEditParam;
import com.tigshop.bean.model.promotion.TimeDiscount;
import com.tigshop.bean.model.promotion.TimeDiscountItem;
import com.tigshop.bean.vo.cart.CartVO;
import com.tigshop.bean.vo.promotion.PromotionVO;
import com.tigshop.bean.vo.promotion.TimeDiscountDetailVO;
import com.tigshop.bean.vo.promotion.TimeDiscountVO;
import com.tigshop.service.common.BaseService;

import java.math.BigDecimal;
import java.util.List;

/**
 * 限时折扣服务
 *
 * @author kidd
 * @create 2025/7/1
 */
public interface TimeDiscountService extends BaseService<TimeDiscount> {

    /**
     * 列表
     */
    Page<TimeDiscountVO> list(TimeDiscountPageQuery pageQuery);

    /**
     * 详情
     */
    TimeDiscountDetailVO detail(Integer id);

    /**
     * 创建
     */
    void create(TimeDiscountSaveParam param);

    /**
     * 更新
     */
    void update(TimeDiscountEditParam param);

    /**
     * 根据sku和折扣获取计算价格
     *
     * @return BigDecimal
     */
    BigDecimal getTimeDiscountPrice(BigDecimal productPrice, Integer skuId, TimeDiscountItem discountItem);

    /**
     * 解析价格
     *
     * @param cartList    cartList
     * @param promotionVO 优惠数据
     */
    CartPromotionParsePriceDTO parsePrice(List<CartVO> cartList, PromotionVO promotionVO);

    /**
     * 判定用户是否可参与该商品当前进行中的限时折扣（加购与下单的服务端拦截入口）。
     * member_scope 人群优先，回退存量 memberRange/memberMarks；未登录、无进行中活动或活动无人群限制均返回 true。
     *
     * @param productId 商品ID
     * @param skuId     规格ID，0 或 null 表示未指定规格
     * @param userId    用户ID
     * @return 是否可参与
     */
    boolean canUserJoinTimeDiscount(Integer productId, Integer skuId, Integer userId);
}
