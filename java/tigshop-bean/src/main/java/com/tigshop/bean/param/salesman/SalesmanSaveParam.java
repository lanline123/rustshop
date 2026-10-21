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

import cn.hutool.core.util.ObjectUtil;
import com.tigshop.bean.model.salesman.Salesman;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 分销员新增参数
 *
 * @author kidd
 * @create 2025/6/21
 */
@Data
@Schema(description = "分销员新增参数")
public class SalesmanSaveParam {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID")
    private Integer userId;

    @NotNull(message = "级别不能为空")
    @Schema(description = "级别")
    private Integer level;

    @NotNull(message = "分组不能为空")
    @Schema(description = "分组")
    private Integer groupId;

    @Schema(description = "pid")
    private Integer pid;

    public Salesman createSalesman() {
        return Salesman.builder()
                .userId(this.userId)
                .level(this.level)
                .groupId(this.groupId)
                .pid(this.pid == null ? 0 : this.pid)
                .addTime(StringUtils.getCurrentTime())
                .shopId(ObjectUtil.isNull(HeaderUtils.getShopId())? 0 : HeaderUtils.getShopId())
                .saleAmount(BigDecimal.ZERO)
                .build();
    }
}
