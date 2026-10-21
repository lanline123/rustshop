// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.dto.deposit;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 保证金收款账户列表查询参数（平台端，分页）。
 *
 * @author Tigshop团队
 */
@Setter
@Getter
@Schema(description = "保证金收款账户列表查询参数")
public class DepositPayeeAccountListDTO extends BasePage {

    @Schema(description = "账户类型 1银行卡 2支付宝 3微信；不传=全部")
    private Integer accountType;

    @Schema(description = "状态 1启用 0停用；不传=全部")
    private Integer status;

    @Schema(description = "关键词（户名/账号模糊）")
    private String keyword;
}
