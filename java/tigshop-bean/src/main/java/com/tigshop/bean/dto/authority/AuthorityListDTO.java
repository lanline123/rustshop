// **---------------------------------------------------------------------+
// ** 文件 -- ${DESCRIPTION}
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.authority;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 查询数据
 *
 * @author Jayce
 * @create 2024年10月21日 15:31
 */
@Setter
@Getter
@Schema(description = "权限列表查询参数")
public class AuthorityListDTO extends BasePage {

    @Schema(description = "管理员类型")
    private String adminType;

    @Schema(description = "父id")
    private Integer parentId;
}
