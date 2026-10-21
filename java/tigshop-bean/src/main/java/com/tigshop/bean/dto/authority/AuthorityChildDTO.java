// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.authority;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 权限子属性
 * @author Jayce
 * @create 2024年10月25日 10:52:59
 */
@Data
@Schema(description = "权限子属性")
public class AuthorityChildDTO {
    @Schema(description = "权限名称")
    private String authName;

    @Schema(description = "权限编号")
    private String authSn;
}