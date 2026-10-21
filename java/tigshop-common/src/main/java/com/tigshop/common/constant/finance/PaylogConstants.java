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
 * 交易日志常量
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:56
 */
public class PaylogConstants {

    public static final Integer PAY_STATUS_UNPAID = 0;
    public static final Integer PAY_STATUS_PAID = 1;
    public static final Integer PAY_STATUS_FAIL = 2;

    //ARTICLE_TYPE_MAP 1为普通文章，2为帮助文章
    public static final Map<Integer, String> PAY_STATUS_NAME = Map.of(
            PAY_STATUS_UNPAID, "待支付",
            PAY_STATUS_PAID, "已支付",
            PAY_STATUS_FAIL, "支付失败"
    );

    public static final String PAYLOG_ID_NOT_NULL = "交易日志id不能为空";
}
