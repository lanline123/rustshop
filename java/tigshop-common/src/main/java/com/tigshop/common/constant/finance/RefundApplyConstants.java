package com.tigshop.common.constant.finance;

import java.util.Map;

/**
 * 退款申请常量
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:56
 */
public class RefundApplyConstants {

    public static final Integer REFUND_STATUS_WAIT = 0;
    public static final Integer REFUND_STATUS_PROCESSING = 1;
    public static final Integer REFUND_STATUS_PROCESSED = 2;
    public static final Integer REFUND_STATUS_CANCEL = 3;
    public static final Integer REFUND_STATUS_REJECTED = 4;

    //ARTICLE_TYPE_MAP 1为普通文章，2为帮助文章
    public static final Map<Integer, String> REFUND_STATUS_NAME = Map.of(
            REFUND_STATUS_WAIT, "待处理",
            REFUND_STATUS_PROCESSING, "处理中",
            REFUND_STATUS_PROCESSED, "已处理",
            REFUND_STATUS_CANCEL, "已取消",
            REFUND_STATUS_REJECTED, "已拒绝"
    );

    public static final Integer REFUND_TYPE_ORDER = 1;
    public static final Integer REFUND_TYPE_PRODUCT = 2;

    //ARTICLE_TYPE_MAP 1为普通文章，2为帮助文章
    public static final Map<Integer, String> REFUND_TYPE_NAME = Map.of(
            REFUND_TYPE_ORDER, "订单",
            REFUND_TYPE_PRODUCT, "商品"
    );

    public static final String REFUND_APPLY_ID_NOT_NULL = "退款申请id不能为空";
}
