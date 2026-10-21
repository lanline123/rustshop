package com.tigshop.common.constant.content;

/**
 * 微信直播订单同步常量
 *
 * @author Tigshop团队
 * @create 2026年03月08日 16:56
 */
public class WechatLiveOrderSyncConstants {

    private WechatLiveOrderSyncConstants() {
    }

    public static final String ORDER_SUB_SOURCE_WECHAT_LIVE = "wechatLive";

    public static final String EXTRA_DATA_WECHAT_LIVE_CONTEXT = "wechatLiveContext";

    public static final String ORDER_EXTENSION_SUB_SOURCE = "subSource";

    public static final String ORDER_EXTENSION_WECHAT_LIVE_CONTEXTS = "wechatLiveContexts";

    public static final String ORDER_EXTENSION_WECHAT_LIVE_LAST_CALLBACK = "wechatLiveLastCallback";

    public static final String DIRECTION_OUTBOUND = "OUTBOUND";

    public static final String DIRECTION_INBOUND = "INBOUND";

    public static final String EVENT_ORDER_CREATED = "ORDER_CREATED";

    public static final String EVENT_ORDER_PAID = "ORDER_PAID";

    public static final String EVENT_ORDER_SHIPPED = "ORDER_SHIPPED";

    public static final String EVENT_AFTERSALE_CREATED = "AFTERSALE_CREATED";

    public static final String EVENT_AFTERSALE_UPDATED = "AFTERSALE_UPDATED";

    public static final String EVENT_REFUND_UPDATED = "REFUND_UPDATED";

    public static final String EVENT_WECHAT_ORDER_CALLBACK = "WECHAT_ORDER_CALLBACK";

    public static final String EVENT_WECHAT_AFTERSALE_CALLBACK = "WECHAT_AFTERSALE_CALLBACK";

    public static final int SYNC_STATUS_PENDING = 0;

    public static final int SYNC_STATUS_SUCCESS = 1;

    public static final int SYNC_STATUS_FAILED = 2;

    public static final int SYNC_STATUS_SKIPPED = 3;

    public static final int SYNC_STATUS_PARTIAL = 4;

    public static String getSyncStatusName(Integer status) {
        if (status == null) {
            return "未同步";
        }
        return switch (status) {
            case SYNC_STATUS_PENDING -> "待同步";
            case SYNC_STATUS_SUCCESS -> "同步成功";
            case SYNC_STATUS_FAILED -> "同步失败";
            case SYNC_STATUS_SKIPPED -> "已跳过";
            case SYNC_STATUS_PARTIAL -> "部分同步";
            default -> "未知状态";
        };
    }
}
