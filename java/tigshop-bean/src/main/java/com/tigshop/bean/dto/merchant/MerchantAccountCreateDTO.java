// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商家账户表创建数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "商家账户表参数")
public class MerchantAccountCreateDTO {
    @Schema(description ="账户类型")
    private Integer accountType;

    @Schema(description ="账户名称")
    private String accountName;

    @Schema(description ="银行卡名称")
    private String bankName;

    @Schema(description ="是否展示")
    private String accountNo;

    @Schema(description ="状态")
    private String bankBranch;

    @Schema(description ="商户id")
    private Integer merchantId;

    @Schema(description ="店铺id")
    private Integer shopId;
}
