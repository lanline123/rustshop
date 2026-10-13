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
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.tigshop.common.constant.salesman.SalesmanGroupConstants.SALESMAN_GROUP_ID_NOT_NULL;

/**
 * 分销员分组编辑参数
 *
 * @author kidd
 * @since 2025/06/21
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "分销员分组编辑参数")
public class SalesmanGroupEditParam extends SalesmanGroupSaveParam {

    @NotNull(message = SALESMAN_GROUP_ID_NOT_NULL)
    @Schema(description = "组ID")
    private Integer groupId;

    public SalesmanGroup updateSalesmanGroup(SalesmanGroup salesmanGroup) {
        SalesmanGroup updateSalesmanGroup = super.createSalesmanGroup();
        updateSalesmanGroup.setGroupId(salesmanGroup.getGroupId());

        return updateSalesmanGroup;
    }

}
