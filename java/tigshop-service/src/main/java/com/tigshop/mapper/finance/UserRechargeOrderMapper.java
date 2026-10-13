// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.mapper.finance;

import com.tigshop.mapper.common.BaseMapper;
import com.tigshop.bean.model.finance.UserRechargeOrder;

/**
 * 充值记录映射
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:03
 */
public interface UserRechargeOrderMapper extends BaseMapper<UserRechargeOrder> {
    /** 充值条件仅累计成功充值订单的本金，不包含赠送金额和后台余额调整。 */
    @org.apache.ibatis.annotations.Select("SELECT COALESCE(SUM(amount), 0) FROM user_recharge_order WHERE user_id = #{userId} AND status = 1")
    java.math.BigDecimal sumPaidAmount(@org.apache.ibatis.annotations.Param("userId") Integer userId);
}
