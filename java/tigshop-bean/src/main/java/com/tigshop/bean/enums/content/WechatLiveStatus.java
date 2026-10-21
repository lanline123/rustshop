/**
 * ---------------------------------------------------------------------+
 * 文件 -- WechatLiveStatus
 * ---------------------------------------------------------------------+
 * 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
 * ---------------------------------------------------------------------+
 * 作者：Tigshop团队，yq@tigshop.com
 * ---------------------------------------------------------------------+
 * 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
 * ---------------------------------------------------------------------+
 */
package com.tigshop.bean.enums.content;

import com.tigshop.common.exception.GlobalException;
import lombok.Getter;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;

/**
 * @author Tigshop团队
 */
@Getter
public enum WechatLiveStatus {
    LIVE_STATUS_START(101, "直播中"),
    LIVE_STATUS_NOT_START(102, "未开始"),
    LIVE_STATUS_END(103, "已结束"),
    LIVE_STATUS_BAN(104, "禁播"),
    LIVE_STATUS_PAUSE(105, "暂停"),
    LIVE_STATUS_EXCEPTION(106, "异常"),
    LIVE_STATUS_EXPIRED(107, "已过期");

    private final int code;
    private final String description;

    WechatLiveStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static boolean isValidCode(int code) {
        for (WechatLiveStatus liveStatus : values()) {
            if (liveStatus.code == code) {
                return true;
            }
        }
        return false;
    }

    public static String getStatusName(Integer status) {
        if (status == null) {
            return "";
        }
        for (WechatLiveStatus liveStatus : values()) {
            if (liveStatus.code == status) {
                return liveStatus.getDescription();
            }
        }
        return "";
    }

    public static WechatLiveStatus getStatus(Integer status) {
        for (WechatLiveStatus liveStatus : values()) {
            if (liveStatus.code == status) {
                return liveStatus;
            }
        }
        throw new GlobalException(DATA_TYPE_ERROR);
    }
}
