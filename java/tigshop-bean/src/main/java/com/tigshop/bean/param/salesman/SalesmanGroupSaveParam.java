// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.param.salesman;

import com.tigshop.bean.model.salesman.SalesmanGroup;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 分销员分组新增参数
 *
 * @author kidd
 * @since 2025/06/21
 */
@Data
@Schema(description = "分销员分组新增参数")
public class SalesmanGroupSaveParam {

    @NotBlank(message = "分组名称不能为空")
    @Schema(description = "组名称")
    private String groupName;

    @Schema(description = "描述")
    private String describe;

    public SalesmanGroup createSalesmanGroup() {
        return SalesmanGroup.builder()
                .groupName(this.groupName)
                .describe(this.describe)
                .addTime(StringUtils.getCurrentTime())
                .shopId(HeaderUtils.getShopId())
                .build();
    }
}
