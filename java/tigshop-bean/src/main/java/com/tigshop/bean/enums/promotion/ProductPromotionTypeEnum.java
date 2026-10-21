/**
 * ---------------------------------------------------------------------+
 * 文件 -- productPromotionType
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
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;

/**
 * @author Tigshop团队
 */
@Getter
@AllArgsConstructor
public enum ProductPromotionTypeEnum {
    PROMOTION_TYPE_FULL_REDUCE(1, "满减"),
    PROMOTION_TYPE_FULL_DISCOUNT(2, "满折"),
    PROMOTION_TYPE_FULL_REDUCE_NAME(3, "赠品");

    private final int code;
    private final String description;

    public static final List<Integer> PROMOTION_GROUP = List.of(PROMOTION_TYPE_FULL_REDUCE.getCode(), PROMOTION_TYPE_FULL_DISCOUNT.getCode());

    public static String getTypeName(Integer status) {
        for (ProductPromotionTypeEnum promotionType : values()) {
            if (promotionType.code == status) {
                return promotionType.getDescription();
            }
        }
        return "";
    }

    public static ProductPromotionTypeEnum getType(Integer status) {
        for (ProductPromotionTypeEnum promotionType : values()) {
            if (promotionType.code == status) {
                return promotionType;
            }
        }
        throw new GlobalException(DATA_TYPE_ERROR);
    }
}
