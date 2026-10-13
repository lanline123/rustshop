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
 * 退款记录常量
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:56
 */
public class RefundLogConstants {

    public static final Integer PAY_STATUS_UNPAID = 0;
    public static final Integer PAY_STATUS_PAID = 1;
    public static final Integer PAY_STATUS_FAIL = 2;

    //ARTICLE_TYPE_MAP 1为普通文章，2为帮助文章
    public static final Map<Integer, String> REFUND_TYPE_MAP = Map.of(
            1, "原路返回",
            2, "余额退回",
            3, "线下退回"
    );

    public static final String REFUND_LOG_ID_NOT_NULL = "退款记录id不能为空";
}
