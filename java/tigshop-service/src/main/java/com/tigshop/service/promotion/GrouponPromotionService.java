package com.tigshop.service.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.cart.CartPromotionParsePriceDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.model.promotion.GrouponPromotion;
import com.tigshop.bean.param.promotion.groupon.PromotionPromotionCreateParam;
import com.tigshop.bean.param.promotion.groupon.PromotionPromotionUpdateParam;
import com.tigshop.bean.query.promotion.groupon.GrouponItemPageQuery;
import com.tigshop.bean.query.promotion.groupon.GrouponPromotionClientPageQuery;
import com.tigshop.bean.query.promotion.groupon.GrouponPromotionPageQuery;
import com.tigshop.bean.vo.cart.CartVO;
import com.tigshop.bean.vo.promotion.*;
import com.tigshop.common.core.entity.BasePage;
import com.tigshop.service.common.BaseService;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author tigshop
 */
public interface GrouponPromotionService extends BaseService<GrouponPromotion> {

    void create(PromotionPromotionCreateParam param);

    void update(PromotionPromotionUpdateParam param);

    void finish(OperateDTO param);

    GrouponPromotionDetailVO detail(Integer grouponPromotionId);

    Page<GrouponPromotionPageVO> list(GrouponPromotionPageQuery pageQuery);

    GrouponPostVO post(Integer grouponPromotionId);

    Page<GrouponPromotionClientPageVO> clientList(GrouponPromotionClientPageQuery pageQuery);

    GrouponPromotionClientDetailVO clientDetail(Long grouponPromotionId);

    Page<GrouponItemVO> itemList(GrouponItemPageQuery pageQuery);

    GrouponItemInfoVO getGrouponInfo(int productId);

    /**
     * 商品列表用：批量计算指定商品的最低拼团价，替代逐商品 getGrouponInfo 的 N+1。
     * 仅返回存在有效拼团 item 且能算出价格的商品；其 value 等价于 getGrouponInfo(productId).getMinGrouponPrice()。
     * 命中（key 存在）等价于原 getGrouponInfo 返回非 null（即列表侧应置 isGroupon=true）。
     */
    Map<Integer, BigDecimal> getGrouponMinPriceBatch(Collection<Integer> productIds);

    /**
     * 二期（D1096）：判定指定用户是否可参与某拼团活动（人群强校验）。
     * member_scope 优先（标签/等级人群），回退旧 memberRange/memberMarks；
     * 未登录 / 查无用户 / 活动不存在 / 无人群约束 → true。
     *
     * @param grouponPromotionId 拼团活动ID
     * @param userId             用户ID（兼容代客下单，显式传入）
     * @return 是否可参与
     */
    boolean canUserJoinGroupon(Integer grouponPromotionId, Integer userId);

    /**
     * 二期（D1096）：按商品维度判定用户是否可参与该商品当前进行中的拼团活动（人群强校验）。
     * 用于加购等无法保证携带拼团活动标识的入口（列表/推荐快捷加购），与秒杀 canUserJoinSeckill 同构。
     * 无进行中拼团 / 该商品未参加进行中拼团 / 无人群约束 → true。
     *
     * @param productId 商品ID
     * @param userId    用户ID
     * @return 是否可参与
     */
    boolean canUserJoinGrouponByProduct(Integer productId, Integer userId);

    CartPromotionParsePriceDTO parsePrice(List<CartVO> carts, PromotionVO promotion);

    /**
     * 拼团图片列表
     *
     * @param pageQuery 分页参数
     * @return 拼团图片分页列表
     */
    Page<GrouponPromotionImageVO> grouponPromotionImages(BasePage pageQuery);

}
