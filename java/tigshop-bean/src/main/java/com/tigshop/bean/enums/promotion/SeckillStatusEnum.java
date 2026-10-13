/**
 * ---------------------------------------------------------------------+
 * 文件 -- SeckillStatus
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
public enum SeckillStatusEnum {
    STATUS_NOT_STARTED(0, "未开始"),
    STATUS_STARTED(1, "进行中"),
    STATUS_ENDED(2, "已结束");

    private final int code;
    private final String description;

    SeckillStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static boolean isValidCode(int code) {
        for (SeckillStatusEnum type : values()) {
            if (type.code == code) {
                return true;
            }
        }
        return false;
    }

    public static String getStatusName(Integer status) {
        for (SeckillStatusEnum type : values()) {
            if (type.code == status) {
                return type.getDescription();
            }
        }
        return "";
    }

    public static SeckillStatusEnum getStatus(Integer status) {
        for (SeckillStatusEnum type : values()) {
            if (type.code == status) {
                return type;
            }
        }
        throw new GlobalException(DATA_TYPE_ERROR);
    }

    public static String getStatusName(Long seckillStartTime, Long seckillEndTime) {
        long currentTime = StringUtils.getCurrentTime();
        if (currentTime < seckillStartTime) {
            return SeckillStatusEnum.STATUS_NOT_STARTED.getDescription();
        } else if (currentTime > seckillEndTime) {
            return SeckillStatusEnum.STATUS_ENDED.getDescription();
        } else {
            return SeckillStatusEnum.STATUS_STARTED.getDescription();
        }
    }
}
