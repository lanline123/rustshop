// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.query.authority.adminuser;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 管理员用户查询参数
 *
 * @author Jayce
 * @create 2024年10月29日 14:33
 */
@Schema(description = "管理员用户查询参数")
@Setter
@Getter
public class AdminUserPageQuery extends BasePage {
    /**
     * 描述：供应商id
     */
    @Schema(description = "供应商id")
    private String suppliersId;

    /**
     * 管理员类型 admin:管理员；shop:店铺管理员；vendor:供应商
     */
    @Schema(description = "管理员类型")
    private String adminUserType;
}
