/**
 * ---------------------------------------------------------------------+
 * 文件 -- PromotionTimeStatus
 * ---------------------------------------------------------------------+
 * 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
 * ---------------------------------------------------------------------+
 * 作者：Tigshop团队，yq@tigshop.com
 * ---------------------------------------------------------------------+
 * 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
 * ---------------------------------------------------------------------+
 */
package com.tigshop.bean.enums.promotion;

import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.StringUtils;
import lombok.Getter;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;

/**
 * @author Tigshop团队
 */
@Getter
public enum PromotionTimeStatusEnum {

    PROMOTION_STATUS_ON(1, "活动进行中"),
    PROMOTION_STATUS_OFF(2, "活动已结束"),
    PROMOTION_STATUS_FORTHCOMING(3, "活动未开始");

    private final int code;
    private final String description;

    PromotionTimeStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static boolean isValidCode(int code) {
        for (PromotionTimeStatusEnum activeState : values()) {
            if (activeState.code == code) {
                return true;
            }
        }
        return false;
    }

    public static String getStatusName(Integer status) {
        for (PromotionTimeStatusEnum activeState : values()) {
            if (activeState.code == status) {
                return activeState.getDescription();
            }
        }
        return "";
    }

    public static PromotionTimeStatusEnum getStatus(Integer status) {
        for (PromotionTimeStatusEnum activeState : values()) {
            if (activeState.code == status) {
                return activeState;
            }
        }
        throw new GlobalException(DATA_TYPE_ERROR);
    }

    public static String handleStatusName(Long startTime, Long endTime) {
        long currentTime = StringUtils.getCurrentTime();
        if (currentTime >= startTime && currentTime < endTime) {
            return PromotionTimeStatusEnum.PROMOTION_STATUS_ON.getDescription();
        } else if (currentTime < startTime) {
            return PromotionTimeStatusEnum.PROMOTION_STATUS_FORTHCOMING.getDescription();
        } else {
            return PromotionTimeStatusEnum.PROMOTION_STATUS_OFF.getDescription();
        }
    }

}
