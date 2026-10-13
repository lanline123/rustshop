// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.deposit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 平台端店铺保证金账户详情-基本信息
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "店铺保证金账户详情")
public class DepositAccountDetailVO {

    @Schema(description = "持有者类型 DepositOwnerType：1店铺 2供应商")
    private Integer ownerType;

    @Schema(description = "持有者类型文案（店铺/供应商）")
    private String ownerTypeText;

    @Schema(description = "持有者id（店铺为shopId，供应商为vendorId）")
    private Integer ownerId;

    @Schema(description = "持有者名称（店铺名或供应商名，统一展示）")
    private String ownerName;

    @Schema(description = "店铺id（兼容字段，仅店铺行填充）")
    private Integer shopId;

    @Schema(description = "店铺名称（兼容字段，仅店铺行填充）")
    private String shopTitle;

    @Schema(description = "店铺类型 1店铺 2门店 3自提点（兼容字段，仅店铺行填充）")
    private Integer shopType;

    @Schema(description = "店铺类型文案（兼容字段，仅店铺行填充）")
    private String shopTypeText;

    @Schema(description = "店铺状态 1开业 4暂停运营 10关店（兼容字段，仅店铺行填充）")
    private Integer shopStatus;

    @Schema(description = "店铺状态文案（兼容字段，仅店铺行填充）")
    private String shopStatusText;

    @Schema(description = "供应商状态 1开启 2关闭（兼容字段，仅供应商行填充）")
    private Integer vendorStatus;

    @Schema(description = "供应商状态文案（兼容字段，仅供应商行填充）")
    private String vendorStatusText;

    @Schema(description = "主体类型/商户性质 1个人 2企业（店铺取商户性质，供应商取供应商性质）")
    private Integer subjectType;

    @Schema(description = "主体类型文案（个人/企业）")
    private String subjectTypeText;

    @Schema(description = "经营类目（在售商品类目名称，逗号分隔）")
    private String businessCategory;

    @Schema(description = "开店时间")
    private String openTime;

    @Schema(description = "所属商户名称（兼容字段，仅店铺行填充）")
    private String merchantName;

    @Schema(description = "保证金状态 1正常 2不足 3冻结 4未缴纳")
    private Integer depositStatus;

    @Schema(description = "保证金状态文案")
    private String depositStatusText;

    @Schema(description = "应缴金额")
    private BigDecimal required;

    @Schema(description = "已交金额")
    private BigDecimal paid;

    @Schema(description = "冻结金额 = min(已交, 应缴)（可扣除/抵赔的保证金部分）")
    private BigDecimal frozen;

    @Schema(description = "可用金额 = max(0, 已交 - 应缴)（可提现/退还部分）")
    private BigDecimal available;

    @Schema(description = "待补缴金额")
    private BigDecimal pending;
}
