// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.mapper.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.bo.CouponReceiveCountBO;
import com.tigshop.bean.model.user.UserCoupon;
import com.tigshop.bean.vo.order.OrderCheckVO;
import com.tigshop.mapper.common.BaseMapper;
import io.lettuce.core.dynamic.annotation.Param;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

/**
 * 用户优惠券映射
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:03
 */
@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {
    List<OrderCheckVO.Coupon> listUserCouponList(@Param("currentUserId") Integer currentUserId, @Param("shopId") Integer shopId);




    /**
     * 统计指定优惠券的领取数量
     *
     * @param couponIds 优惠券ID列表
     * @return 优惠券领取数量统计结果
     */
    List<CouponReceiveCountBO> countCouponReceive(@Param("couponIds") Collection<Integer> couponIds);

    /**
     * 查询发放总量已领完的优惠券ID
     * <p>
     * {@code coupon.send_num = 0} 表示不限量，不会出现在结果中。
     *
     * @return 已领完的优惠券ID列表
     */
    List<Integer> selectSoldOutCouponIds();
}
