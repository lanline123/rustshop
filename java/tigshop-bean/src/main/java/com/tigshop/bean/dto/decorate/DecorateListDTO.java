// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.decorate;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 页面管理列表
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Setter
@Getter
@Schema(description = "页面管理列表参数")
public class DecorateListDTO extends BasePage {
    @Schema(description ="页面类型 1:h5 2:PC 3:自定义页面，支持多值（decorateType=1,2 或 decorateType=1&decorateType=2）")
    private List<Integer> decorateType;

    @Schema(description ="页面类型 1:商城 2:店铺 3:门店 4:自定义 7:导航，支持多值（pageType=1,2 或 pageType=1&pageType=2）")
    private List<Integer> pageType;

    @Schema(description = "父级ID")
    private Integer parentId;

    @Schema(description = "1:发布 ,0:未发布 ")
    private Integer status;
}
