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
public class UserBalanceLogConstants {
    public static final Integer CHANGE_TYPE_INCREASE = 1;
    public static final Integer CHANGE_TYPE_DECREASE = 2;
    public static final Integer CHANGE_TYPE_OTHER = 99;

    // 处理状态
    public static final Map<Integer, String> CHANGE_TYPE_NAME = Map.of(
            CHANGE_TYPE_INCREASE, "增加",
            CHANGE_TYPE_DECREASE, "减少",
            CHANGE_TYPE_OTHER, "其他"

    );
}
