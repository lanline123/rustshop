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

import com.tigshop.bean.model.salesman.Salesman;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 分销员编辑参数
 *
 * @author kidd
 * @create 2025/6/21
 */
@Data
@Schema(description = "分销员编辑参数")
public class SalesmanEditParam extends SalesmanSaveParam {

    @NotNull(message = "ID不能为空")
    @Schema(description = "ID")
    private Integer salesmanId;

    public Salesman updateSalesman(Salesman salesman) {
        Salesman updateSalesman = super.createSalesman();
        updateSalesman.setSalesmanId(salesman.getSalesmanId());

        return updateSalesman;
    }

}
