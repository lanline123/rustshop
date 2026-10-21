// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.shop;

import com.tigshop.bean.vo.merchant.MerchantVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 店铺资金变化VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "店铺资金变化参数")
public class AccountVO {
    @Schema(description = "余额总结余(元)")
    private Integer cardCount;

    @Schema(description ="冻结总资金(元)")
    private BigDecimal frozenMoney;

    @Schema(description ="店铺总资金(元)")
    private BigDecimal shopMoney;

    @Schema(description = "线上与线下可用余额合计(元)")
    private BigDecimal availableBalance;

    @Schema(description ="待结算总额(元)")
    private BigDecimal unSettlementMoney;

    @Schema(description ="商户信息")
    private MerchantVO merchant;

}
