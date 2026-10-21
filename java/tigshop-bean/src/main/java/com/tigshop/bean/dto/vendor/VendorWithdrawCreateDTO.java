// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.vendor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 供应商提现表创建数据对象
 *
 * @author Tigshop团队
 * @create 2025年07月18日 09:32
 */
@Data
@Schema(description = "供应商提现表参数")
public class VendorWithdrawCreateDTO {
    private Integer vendorAccountId;
    private BigDecimal amount;
    private String remark;
    private AccountData accountData;

    @Data
    @Schema(description = "供应商账户表参数")
    public static class AccountData {
        private String accountTypeText;
        private Integer accountId;
        private Integer vendorId;
        private Integer accountType;
        private String accountName;
        private String accountNo;
        private String bankName;
        private String addTime;
        private String bankBranch;
    }
}