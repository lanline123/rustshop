// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static com.tigshop.common.constant.user.CollectProductConstants.COLLECT_PRODUCT_ID_NOT_NULL;

/**
  * 商品收藏更新参数
  * @author Tigshop团队
  * @create 2024年12月03日 17:17
  */
@Data
@Schema(description = "商品收藏参数")
public class CollectProductUpdateDTO {
    @Schema(description = "商品收藏ID")
    @NotNull(message = COLLECT_PRODUCT_ID_NOT_NULL)
    private Integer collectProductId;

    @Schema(description ="商品收藏名称")
    private String collectProductName;

    @Schema(description ="商品收藏图片")
    private String collectProductPic;

    @Schema(description ="商品收藏排序")
    private Integer sortOrder;

    @Schema(description ="是否展示")
    private Integer isShow;

    @Schema(description ="状态")
    private Integer status;
}
