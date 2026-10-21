// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.cart.CartPromotionParsePriceDTO;
import com.tigshop.bean.dto.promotion.CouponDTO;
import com.tigshop.bean.query.promotion.coupon.CouponPageQuery;
import com.tigshop.bean.model.promotion.Coupon;
import com.tigshop.bean.model.user.UserRank;
import com.tigshop.bean.vo.cart.CartVO;
import com.tigshop.bean.vo.promotion.CouponClientVO;
import com.tigshop.bean.vo.promotion.CouponDetailVO;
import com.tigshop.bean.vo.promotion.CouponVO;
import com.tigshop.bean.vo.promotion.PromotionVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 优惠券服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface CouponService extends BaseService<Coupon> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<CouponVO> list(CouponPageQuery listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    CouponVO detail(Integer id);

    /**
     * 按 ids 批量获取优惠券 VO 列表（装修等场景回显使用）
     *
     * <p>注意命名：避开 {@link com.baomidou.mybatisplus.extension.service.IService#listByIds} 同名方法
     * （IService.listByIds 返回 entity 列表，本方法返回 VO 列表）</p>
     *
     * @param ids 优惠券ID列表
     * @return 优惠券 VO 列表（已删除的券不返回，顺序不保证与入参一致）
     */
    List<CouponVO> getCouponListByIds(List<Integer> ids);

    /**
     * 按 ids 批量获取前台优惠券（mall 端装修等场景渲染使用）
     *
     * <p>与 getCouponListByIds 的差异：</p>
     * <ul>
     *   <li>返回 CouponClientVO（前台精简版，含 isReceive / receiveNum 等字段）</li>
     *   <li>过滤 isShow=1（不返回下架券）</li>
     *   <li>不按 admin shopId 隔离（前台没有 admin 上下文）</li>
     *   <li>会查询当前登录用户的领取记录用于计算 isReceive（未登录时不查）</li>
     * </ul>
     *
     * @param ids 优惠券ID列表
     * @return 前台优惠券列表
     */
    List<CouponClientVO> getClientCouponListByIds(List<Integer> ids);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(CouponDTO createDTO);

    /**
     * 更新
     */
    void update(CouponDTO updateDTO);

    /**
     * 配置
     * @return ItemVO
     */
    List<UserRank> config();

    /**
     * 优惠券列表
     */
    Page<CouponClientVO> getList(CouponPageQuery pageQuery);

    /**
     * 获取商品优惠券列表
     * @param productId 商品ID
     * @param shopId 店铺ID
     * @return List<CouponDetailVO>
     */
    List<CouponDetailVO> getProductCouponList(int productId, int shopId);

    /**
     * 解析价格
     *
     * @param cartList    cartList
     * @param promotionVO 优惠数据
     */
    CartPromotionParsePriceDTO parsePrice(List<CartVO> cartList, PromotionVO promotionVO);

    String getCouponPromotionDesc(Coupon coupon);

    /**
     * 查询平台有效优惠券列表(用于抽奖活动选择)
     *
     * @param page 页码
     * @param size 每页数量
     * @param couponName 优惠券名称(模糊搜索)
     * @return 优惠券列表
     */
    /**
     * 查询平台有效优惠券列表(用于抽奖活动配置等)
     *
     * @param query 查询参数
     * @return 优惠券列表
     */
    Page<CouponVO> getValidList(CouponPageQuery query);
}
