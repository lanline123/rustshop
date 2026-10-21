// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.query.product;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评价管理列表
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "评价管理列表参数")
public class CommentListPageQuery extends BasePage {

    @Schema(description = "是否展示", allowableValues = "-1, 0, 1")
    private Integer isShowed;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "类型")
    private Integer type;

    @Schema(description ="店铺id")
    private Integer shopId;
}
