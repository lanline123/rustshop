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


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Tigshop团队
 * @create 2025/7/16 16:52
 */
@Data
public class VendorAccountCreateParam {

    @Schema(description ="账户类型")
    private Integer accountType;

    @Schema(description ="账户名称")
    private String accountName;

    @Schema(description ="号码")
    private String accountNo;

    @Schema(description ="分行名称")
    private String bankBranch;

    @Schema(description ="银行卡详情")
    private String bankName;

}
