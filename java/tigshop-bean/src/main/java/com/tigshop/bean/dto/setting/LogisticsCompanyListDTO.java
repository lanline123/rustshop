// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.setting;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 配送方式配置信息列表
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Setter
@Getter
@Schema(description = "配送方式配置信息列表参数")
public class LogisticsCompanyListDTO extends BasePage {
    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "是否启用：1启用，0禁用")
    private Integer isEnabled;

    @Schema(description = "物流服务商类型：1快递鸟，2快递100；不传或非法值不过滤")
    private Integer providerType;
}
