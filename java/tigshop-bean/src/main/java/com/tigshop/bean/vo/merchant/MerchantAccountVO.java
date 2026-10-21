// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商家账户表VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "商家账户表参数")
public class MerchantAccountVO {
    @Schema(description = "账户类型文本")
    private String accountTypeText;

    @Schema(description = "账户ID")
    private Integer accountId;

    @Schema(description = "商家ID")
    private Integer merchantId;

    @Schema(description = "账户类型")
    private Integer accountType;

    @Schema(description = "账户名称")
    private String accountName;

    @Schema(description = "账户号")
    private String accountNo;

    @Schema(description = "银行名称")
    private String bankName;

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "银行支行")
    private String bankBranch;

    @Schema(description = "店铺ID")
    private Integer shopId;

}