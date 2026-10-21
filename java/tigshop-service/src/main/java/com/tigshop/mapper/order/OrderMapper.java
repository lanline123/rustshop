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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.query.panel.SalesProductDetailPageQuery;
import com.tigshop.bean.vo.panel.SalesProductOrderItemVO;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单管理映射
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:03
 */
public interface OrderMapper extends BaseMapper<Order> {
    /** 购买条件累计次数：有效已支付且处于确认、发货或完成状态的商城订单数。 */
    @org.apache.ibatis.annotations.Select("SELECT COUNT(*) FROM `order` WHERE user_id = #{userId} AND pay_status = 2 AND is_valid = 1 AND order_status IN (1, 2, 5)")
    Integer countConsumptionOrders(@org.apache.ibatis.annotations.Param("userId") Integer userId);

    /**
     * 获取销售商品明细
     */
    Page<SalesProductOrderItemVO> getSalesProductDetail(@Param("page") Page page, @Param("pageQuery") SalesProductDetailPageQuery pageQuery);

    List<Integer> getHistoryProduct(@Param("userId") Integer userId);

    /** 购买条件累计金额：有效已支付订单的线上支付、余额支付，加成功充值本金。 */
    java.math.BigDecimal sumConsumptionAmount(@Param("userId") Integer userId);
}
