// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.param.vendor;


import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Tigshop团队
 * @create 2025/7/4 15:17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorListPageParam extends BasePage {
    @Schema(description = "供应商状态")
    private Integer status;

    @Schema(description = "供应商id集合")
    private List<Integer> vendorIds;

    @Schema(description = "主账号名称")
    private String mainAccount;

    @Schema(description = "账号名称")
    private String account;
}
