package com.tigshop.service.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.cart.CartPromotionParsePriceDTO;
import com.tigshop.bean.dto.promotion.SeckillCreateDTO;
import com.tigshop.bean.dto.promotion.SeckillItemInfoDTO;
import com.tigshop.bean.dto.promotion.SeckillListDTO;
import com.tigshop.bean.dto.promotion.SeckillUpdateDTO;
import com.tigshop.bean.model.promotion.Seckill;
import com.tigshop.bean.vo.cart.CartVO;
import com.tigshop.bean.vo.promotion.PromotionVO;
import com.tigshop.bean.vo.promotion.SeckillListVO;
import com.tigshop.bean.vo.promotion.SeckillPromotionVO;
import com.tigshop.bean.vo.promotion.SeckillVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 限时秒杀服务
 *
 * @author kidd
 * @create 2025/7/2
 */
public interface SeckillService extends BaseService<Seckill> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<SeckillVO> list(SeckillListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    SeckillVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(SeckillCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(SeckillUpdateDTO updateDTO);

    /**
     * 获取秒杀商品信息
     *
     * @param productId 商品ID
     * @param shopId
     * @return List<SeckillItemInfoDTO>
     */
    List<SeckillItemInfoDTO> getSeckillInfo(Integer productId, Integer shopId);

    /**
     * 增加销量
     *
     * @param productId 商品ID
     * @param skuId     skuID
     * @param quantity  数量
     */
    void incSales(Integer productId, Integer skuId, Integer quantity);

    /**
     * 减少库存
     *
     * @param productId 商品ID
     * @param skuId     skuID
     * @param quantity  数量
     */
    void decStock(Integer productId, Integer skuId, Integer quantity);

    /**
     * 解析价格
     *
     * @param cartList    cartList
     * @param promotionVO 优惠数据
     */
    CartPromotionParsePriceDTO parsePrice(List<CartVO> cartList, PromotionVO promotionVO);

    /**
     * 减少销量
     *
     * @param productId 商品ID
     * @param skuId     skuID
     * @param quantity  数量
     */
    void decSales(Integer productId, Integer skuId, Integer quantity);

    /**
     * 增加库存
     *
     * @param productId 商品ID
     * @param skuId     skuID
     * @param quantity  数量
     */
    void incStock(Integer productId, Integer skuId, Integer quantity);

    /**
     * 获取秒杀商品信息
     */
    Page<SeckillListVO> getSeckillProductList(Integer size, Integer page, Integer unStarted);

    /**
     * 获取秒杀商品列表
     */
    List<SeckillPromotionVO> listForDecorate(SeckillListDTO listDTO);

    /**
     * 二期（D1096）：下单/校验时判定指定用户能否参与该商品当前进行中的秒杀活动。
     * member_scope（标签/等级人群）非空时按人群匹配；为空（含存量 memberRange）保持改造前不拦截行为。
     * 无进行中秒杀活动或未登录时返回 true（不拦截）。
     *
     * @param productId 商品ID
     * @param skuId     skuID
     * @param userId    用户ID
     * @return 是否可参与（可下单）
     */
    boolean canUserJoinSeckill(Integer productId, Integer skuId, Integer userId);
}
