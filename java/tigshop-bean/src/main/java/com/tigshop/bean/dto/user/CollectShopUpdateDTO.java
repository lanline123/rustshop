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

import static com.tigshop.common.constant.user.CollectShopConstants.COLLECT_SHOP_ID_NOT_NULL;

/**
  * 会员收藏店铺的记录列表更新参数
  * @author Tigshop团队
  * @create 2024年12月03日 17:17
  */
@Data
@Schema(description = "会员收藏店铺的记录列表参数")
public class CollectShopUpdateDTO {
    @Schema(description = "会员收藏店铺的记录列表ID")
    @NotNull(message = COLLECT_SHOP_ID_NOT_NULL)
    private Integer collectShopId;

    @Schema(description ="会员收藏店铺的记录列表名称")
    private String collectShopName;

    @Schema(description ="会员收藏店铺的记录列表图片")
    private String collectShopPic;

    @Schema(description ="会员收藏店铺的记录列表排序")
    private Integer sortOrder;

    @Schema(description ="是否展示")
    private Integer isShow;

    @Schema(description ="状态")
    private Integer status;
}
