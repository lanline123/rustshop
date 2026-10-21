// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.enums.order;

import com.tigshop.common.exception.GlobalException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;

/**
 * @author Tigshop
 */
@Getter
@AllArgsConstructor
public enum OrderStatusEnum {
    /**
     * 待支付
     */
    PENDING(0, "待支付"),
    /**
     * 已确认，待发货（支付后同步为此状态）
     */
    CONFIRMED(1, "已确认，待发货"),
    /**
     * 处理中，已发货（发货后同步为此状态）
     */
    PROCESSING(2, "处理中，已发货"),
    /**
     * 已取消
     */
    CANCELLED(3, "已取消"),
    /**
     * 无效
     */
    INVALID(4, "无效"),
    /**
     * 已完成
     */
    COMPLETED(5, "已完成");

    private final int code;
    private final String description;

    /**
     * 判断一个值是否为有效的 code
     *
     * @param code 状态码
     * @return 如果 code 有效则返回 true；否则返回 false
     */
    public static boolean isValidCode(int code) {
        for (OrderStatusEnum status : values()) {
            if (status.code == code) {
                return true;
            }
        }
        return false;
    }

    public static String getStatusName(Integer status) {
        for (OrderStatusEnum orderStatusEnum : values()) {
            if (orderStatusEnum.code == status) {
                return orderStatusEnum.getDescription();
            }
        }
        return "";
    }

    /**
     * 后台订单详情状态头展示名称。仅 0/1/2/5 使用后台专用文案，其余（含已取消/无效）返回 null，
     * 表示沿用默认 {@link #getStatusName} 名称。不改动枚举 description，避免影响买家端及全站其他展示。
     */
    public static String getAdminStatusName(Integer status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case 0 -> "待付款，待支付";
            case 1 -> "已付款，待发货";
            case 2 -> "已发货，待收货";
            case 3 -> "订单超时，已取消";
            case 5 -> "交易完成，订单结束";
            default -> null;
        };
    }

    /**
     * 订单状态副文案（详情状态卡副行）。无对应文案时返回 null，前端不展示副行。
     */
    public static String getStatusTip(Integer status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case 0 -> "买家已提交订单，请等待付款";
            case 1 -> "买家已提交订单，请尽快发货";
            case 2 -> "商品已发出，请等待买家签收";
            case 3 -> "买家超时未支付，订单已取消";
            case 4 -> "订单已失效";
            case 5 -> "买家已确认收货，交易已完成";
            default -> null;
        };
    }

    public static OrderStatusEnum getStatus(Integer status) {
        for (OrderStatusEnum orderStatusEnum : values()) {
            if (orderStatusEnum.code == status) {
                return orderStatusEnum;
            }
        }
        throw new GlobalException(DATA_TYPE_ERROR);
    }

    public static OrderStatusEnum getPreStatus(int code) {
        return switch (code) {
            // 待发货 -> 待支付
            case 1 -> CANCELLED;
            // 已发货 -> 待发货
            case 2 -> CONFIRMED;
            default -> null;
        };
    }
}
