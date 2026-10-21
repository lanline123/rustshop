// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.merchant;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商家账户表model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("merchant_account")
@Schema(description = "商家账户表")
public class MerchantAccount {

    @TableId(value = "account_id", type = IdType.AUTO)
    @Schema(description = "商家账户表ID")
    private Integer accountId;

    @Schema(description ="商户id")
    private Integer merchantId;

    @Schema(description ="提现账号类型，1：银行卡，2：支付宝，3：微信")
    private Integer accountType;

    @Schema(description ="姓名")
    private String accountName;

    @Schema(description ="银行卡号或账号")
    private String accountNo;

    @Schema(description ="银行名称")
    private String bankName;

    @Schema(description ="添加时间")
    private Long addTime;

    @Schema(description ="分行名称")
    private String bankBranch;

    @Schema(description ="店铺ID")
    private Integer shopId;
}