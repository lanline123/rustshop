// **-----------------------------------------------------------------+
// ** 文件 --
// **-----------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **-----------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **-----------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **-----------------------------------------------------------------+

package com.tigshop.mapper.content;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tigshop.bean.model.content.WechatLiveGoods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 微信直播商品关联映射
 *
 * @author Tigshop团队
 * @create 2024年12月27日 17:13
 */
@Mapper
public interface WechatLiveGoodsMapper extends BaseMapper<WechatLiveGoods> {

    /**
     * 分页查询直播商品库，联表商品/规格以返回实时库存并支持按价格、库存排序。
     *
     * <p>{@code orderByClause} 必须由服务层白名单生成，禁止直接透传用户输入，防止 SQL 注入。</p>
     *
     * @param page          分页对象
     * @param shopId        店铺ID
     * @param keyword       商品名称关键字（可空）
     * @param orderByClause 已白名单化的排序子句
     * @return 直播商品分页（含实时库存 stock）
     */
    @Select("<script>"
            + "SELECT g.*, "
            + "CASE WHEN g.product_sku_id &gt; 0 THEN ps.sku_stock ELSE p.product_stock END AS stock "
            + "FROM wechat_live_goods g "
            + "LEFT JOIN product p ON p.product_id = g.product_id "
            + "LEFT JOIN product_sku ps ON ps.sku_id = g.product_sku_id AND g.product_sku_id &gt; 0 "
            + "WHERE g.shop_id = #{shopId} "
            + "<if test='keyword != null and keyword != \"\"'> AND g.goods_name LIKE CONCAT('%', #{keyword}, '%')</if> "
            + "ORDER BY ${orderByClause}"
            + "</script>")
    IPage<WechatLiveGoods> selectGoodsLibraryPage(IPage<WechatLiveGoods> page,
                                                  @Param("shopId") Integer shopId,
                                                  @Param("keyword") String keyword,
                                                  @Param("orderByClause") String orderByClause);
}
