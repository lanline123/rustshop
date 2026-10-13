// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.shop;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

import static com.tigshop.common.constant.shop.ShopWithdrawConstants._NOT_NULL;
import static com.tigshop.common.constant.shop.ShopWithdrawConstants._OVER_LENGTH;

/**
 * 商家账户表创建数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "商家账户表参数")
public class ShopWithdrawCreateDTO {
    private Integer merchantAccountId;
    private BigDecimal amount;
    private String remark;
    private AccountData accountData;

    /** 提现类型：1-线下提现 2-线上提现 */
    @Schema(description = "提现类型：1-线下提现 2-线上提现")
    private Integer withdrawType;

    /** 线上提现渠道：WECHAT/HUIFU/ALIPAY；线上提现时由渠道端点写入，为空默认 WECHAT（三渠道独立体系 CR-20260721-144356） */
    @Schema(description = "线上提现渠道：WECHAT/HUIFU/ALIPAY，为空默认 WECHAT")
    private String payChannel;

    @Data
    @Schema(description = "商家账户表参数")
    public static class AccountData {
        private String accountTypeText;
        private Integer accountId;
        private Integer merchantId;
        private Integer accountType;
        private String accountName;
        private String accountNo;
        private String bankName;
        private String addTime;
        private String bankBranch;
        private Integer shopId;
    }
}
