// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.common.constant.finance;

import java.util.Map;

/**
 * 充值记录常量
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:56
 */
public class UserRechargeOrderConstants {
    public static final Integer STATUS_WAIT = 0;
    public static final Integer STATUS_SUCCESS = 1;
    public static final Integer STATUS_FAIL = 2;

    // 处理状态
    public static final Map<Integer, String> STATUS_TYPE = Map.of(
            STATUS_WAIT, "待确认",
            STATUS_SUCCESS, "已支付",
            STATUS_FAIL, "无效"

    );
    public static final String USER_RECHARGE_ORDER_ID_NOT_NULL = "充值记录id不能为空";
    public static final String USER_RECHARGE_AMOUNT_ERROR = "充值金额需要大于0";
    public static final String USER_RECHARGE_STATUS_ERROR = "当前申请状态不允许修改";

}
