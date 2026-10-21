// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.param.account;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 账号列表查询DTO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "账号管理查询DTO")
public class AdminUserPageParam extends BasePage {

    @Schema(description = "是否删除：1 是 0 否，不传则查询全部")
    private Integer isDeleted;

}