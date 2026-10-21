/**
 * ---------------------------------------------------------------------+
 * 文件 -- PromotionType
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
import lombok.Getter;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;

/**
 * @author Tigshop团队
 */

@Getter
public enum PromotionType {

    TYPE_SECKILL(1, "秒杀"),
    TYPE_COUPON(2, "优惠券"),
    TYPE_REDUCE(3, "满减"),
    TYPE_DISCOUNT(4, "满折"),
    TYPE_GIVE(5, "满赠"),
    TYPE_LIMIT_DISCOUNT(6, "限时折扣"),
    TYPE_GROUPON(7, "拼团"),
    TYPE_LOTTERY(8, "抽奖活动");

    private final int code;
    private final String description;

    PromotionType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String getStatusName(Integer status) {
        for (PromotionType type : values()) {
            if (type.code == status) {
                return type.getDescription();
            }
        }
        return "";
    }

    public static PromotionType getStatus(Integer status) {
        for (PromotionType type : values()) {
            if (type.code == status) {
                return type;
            }
        }
        throw new GlobalException(DATA_TYPE_ERROR);
    }

}
