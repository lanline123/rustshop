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
 * 提现申请常量
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:56
 */
public class UserWithdrawApplyConstants {
    public static final String POSTSCRIPT_NOT_NULL = "提现申请名称不能为空";

    public static final String POSTSCRIPT_OVER_LENGTH = "提现申请名称长度不能超过50";

    public static final String USER_WITHDRAW_APPLY_ID_NOT_NULL = "提现申请id不能为空";
    public static final String USER_WITHDRAW_APPLY_STATUS_NOT_ALLOW_UPDATE = "当前提现申请状态不允许修改";

    public static final Integer STATUS_WAIT = 0;
    public static final Integer STATUS_FINISHED = 1;
    public static final Integer STATUS_REJECT = 2;

    public static final String USER_BALANCE_NOT_ENOUGH = "提现金额大于账户的可用余额";

    // 处理状态
    public static final Map<Integer, String> STATUS_TYPE = Map.of(
            STATUS_WAIT, "待处理",
            STATUS_FINISHED, "已完成",
            STATUS_REJECT, "拒绝申请"

    );
}
