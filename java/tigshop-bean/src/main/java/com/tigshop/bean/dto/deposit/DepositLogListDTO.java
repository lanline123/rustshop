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
 * 平台端保证金全局交易明细列表查询参数
 *
 * @author Tigshop团队
 */
@Setter
@Getter
@Schema(description = "保证金交易明细查询参数")
public class DepositLogListDTO extends BasePage {

    @Schema(description = "持有者类型（组织类型）DepositOwnerType：null=全部 1店铺 2供应商")
    private Integer ownerType;

    @Schema(description = "主体类型（商户性质）：null=全部 1个人 2企业；店铺按商户性质、供应商按供应商性质筛选")
    private Integer subjectType;

    @Schema(description = "支付方式（收款账户类型）：null=全部 1银行卡 2支付宝 3微信；仅命中充值类流水")
    private Integer payeeAccountType;

    @Schema(description = "持有者名称（组织名称，模糊，统一搜索：店铺行匹配店铺名，供应商行匹配供应商名）")
    private String ownerName;

    @Schema(description = "店铺名称（模糊，兼容字段；为空时回退使用 ownerName 作为统一名称搜索）")
    private String shopTitle;

    @Schema(description = "交易单号（按关联订单id/关联单id精确匹配）")
    private String tradeNo;

    @Schema(description = "商户名称（模糊，匹配企业名称/申请主体）")
    private String merchantName;

    @Schema(description = "交易类型 DepositLogType：1缴纳(充值) 2提现 3退还 4扣除 5调整")
    private Integer type;

    @Schema(description = "交易时间快捷区间 today/yesterday/last7/last30/last180")
    private String dateType;

    @Schema(description = "交易时间起（秒级时间戳，与 dateType 二选一，优先级高于 dateType）")
    private Long startTime;

    @Schema(description = "交易时间止（秒级时间戳）")
    private Long endTime;
}
