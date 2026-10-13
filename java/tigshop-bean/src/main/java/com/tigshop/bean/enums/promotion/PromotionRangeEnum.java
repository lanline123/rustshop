/**
 * ---------------------------------------------------------------------+
 * 文件 -- PromotionRange
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
public enum PromotionRangeEnum {
    PROMOTION_RANGE_ALL(0,"全部商品"),
    PROMOTION_RANGE_PRODUCT(3,"指定商品"),
    PROMOTION_RANGE_EXCLUDE_PRODUCT(4,"指定商品不参与");

    private final int code;
    private final String description;

    PromotionRangeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String getStatusName(Integer status) {
        for (PromotionRangeEnum range : values()) {
            if (range.code == status) {
                return range.getDescription();
            }
        }
        return "";
    }

    public static PromotionRangeEnum getStatus(Integer status) {
        for (PromotionRangeEnum range : values()) {
            if (range.code == status) {
                return range;
            }
        }
        throw new GlobalException(DATA_TYPE_ERROR);
    }
}
